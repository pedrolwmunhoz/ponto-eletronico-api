package com.pontoeletronico.api.domain.services.bancohoras;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.pontoeletronico.api.domain.entity.registro.ResumoPontoDia;
import com.pontoeletronico.api.domain.entity.empresa.MetricasDiariaEmpresa;
import com.pontoeletronico.api.domain.entity.registro.RegistroPonto;
import com.pontoeletronico.api.domain.services.bancohoras.record.JornadaConfig;
import com.pontoeletronico.api.domain.services.bancohoras.utils.CalcularResumoDiaUtils;
import com.pontoeletronico.api.infrastructure.output.repository.bancohoras.BancoHorasMensalRepository;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.MetricasDiariaEmpresaRepository;
import com.pontoeletronico.api.infrastructure.output.repository.registro.ResumoPontoDiaRepository;
import com.pontoeletronico.api.infrastructure.output.repository.registro.RegistroPontoRepository;
import com.pontoeletronico.api.infrastructure.output.repository.registro.XrefPontoResumoRepository;

import jakarta.transaction.Transactional;

import com.pontoeletronico.api.domain.services.empresa.MetricasDiariaEmpresaContadorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class CalcularHorasMetricasService {


    private final Logger LOGGER_TECNICO = LoggerFactory.getLogger(CalcularHorasMetricasService.class);
    private final ResumoPontoDiaRepository resumoPontoDiaRepository;
    private final RegistroPontoRepository registroPontoRepository;
    private final XrefPontoResumoRepository xrefPontoResumoRepository;
    private final BancoHorasMensalService calcularBancoHorasMensal;
    private final CalcularResumoDiaUtils calcularResumoDiaUtils;
    private final MetricasDiariaEmpresaContadorService metricasDiariaEmpresaContadorService;
    private final BancoHorasMensalRepository bancoHorasMensalRepository;
    private final MetricasDiariaEmpresaRepository metricasDiariaEmpresaRepository;

    
    public void calcularHorasAposEntradaManual(UUID empresaId, RegistroPonto registroPonto, JornadaConfig jornadaConfig) {
         
        DeltaJornadasAfetadas deltaJornadasAfetadas = new DeltaJornadasAfetadas();

        Duration totalAfetadaDepois = Duration.ZERO;
        Duration totalAfetadaAntes = Duration.ZERO;
        var tempoDescansoEntreJornada = jornadaConfig.tempoDescansoEntreJornada();
        LocalDateTime dataAtualInicio = registroPonto.getCreatedAt();
        LocalDateTime dataAtualFim = registroPonto.getCreatedAt();
        
        LocalDateTime inicioRangeJornada = dataAtualInicio.minus(tempoDescansoEntreJornada).plus(1, ChronoUnit.MILLIS);
        LocalDateTime fimRangeJornada = dataAtualFim.plus(tempoDescansoEntreJornada).minus(1, ChronoUnit.MILLIS);
        
        RangeJornadasAfetadas range = new RangeJornadasAfetadas();

        Optional<ResumoPontoDia> jornadaAfetadaoOpt = xrefPontoResumoRepository.findByFuncionarioIdAndDataBetweenAsc(registroPonto.getUsuarioId(), inicioRangeJornada, dataAtualInicio);

        UUID jornadaAfetadaId = null;
        if(jornadaAfetadaoOpt.isPresent()) {

            ResumoPontoDia jornadaAfetada = jornadaAfetadaoOpt.get();
            totalAfetadaAntes = jornadaAfetada.getTotalHorasTrabalhadas();
            
            jornadaAfetadaId = jornadaAfetada.getId();
            handleJornadaAfetada(empresaId, jornadaAfetada, registroPonto, jornadaConfig, range, deltaJornadasAfetadas);
            totalAfetadaDepois = jornadaAfetada.getTotalHorasTrabalhadas(); 
            deltaJornadasAfetadas.setDataRefAfetada(jornadaAfetada.getPrimeiraBatida().toLocalDate());

        }else{

            jornadaAfetadaoOpt = xrefPontoResumoRepository.findByFuncionarioIdAndDataBetweenAsc(registroPonto.getUsuarioId(), dataAtualFim, fimRangeJornada);
            if(jornadaAfetadaoOpt.isPresent()) {
                ResumoPontoDia jornadaAfetada = jornadaAfetadaoOpt.get();
                totalAfetadaAntes = jornadaAfetada.getTotalHorasTrabalhadas();
                jornadaAfetadaId = jornadaAfetada.getId();
                handleJornadaAfetada(empresaId, jornadaAfetada, registroPonto, jornadaConfig, range, deltaJornadasAfetadas);
                totalAfetadaDepois = jornadaAfetada.getTotalHorasTrabalhadas();
                deltaJornadasAfetadas.setDataRefAfetada(jornadaAfetada.getPrimeiraBatida().toLocalDate());
            }else{
                //nao existe jornada afetada - criar nova jornada
                var idNovoResumo = UUID.randomUUID();
                jornadaAfetadaId = idNovoResumo;
                resumoPontoDiaRepository.insert(idNovoResumo, registroPonto.getUsuarioId(), empresaId, registroPonto.getCreatedAt(), registroPonto.getCreatedAt(), Duration.ZERO, Duration.ZERO, false, null, 1L, registroPonto.getCreatedAt());
                deltaJornadasAfetadas.setDataRefAfetada(registroPonto.getCreatedAt().toLocalDate());
            }
        }
        salvarXrefPontoResumo(registroPonto.getUsuarioId(), jornadaAfetadaId, registroPonto.getId(), registroPonto.getCreatedAt());
        
        Duration deltaAfetada = totalAfetadaDepois.minus(totalAfetadaAntes);
        deltaJornadasAfetadas.setDeltaHorasAfetada(deltaAfetada);
        deltaJornadasAfetadas.setDeltaRegistrosPontoAfetada(deltaJornadasAfetadas.getDeltaRegistrosPontoAfetada() + 1);
        
        
        range.setJornadaAfetadaAno(registroPonto.getCreatedAt().getYear());
        range.setJornadaAfetadaMes(registroPonto.getCreatedAt().getMonthValue());

        calcularBancoHorasMensal.recalcularMensal(registroPonto.getUsuarioId(), empresaId, range);       
        metricasDiariaEmpresaContadorService.ajustarMetricasAposRecalculoDelta(empresaId, deltaJornadasAfetadas);
    }

    @Transactional
    private void handleJornadaAfetada(UUID empresaId, ResumoPontoDia jornadaAfetada, RegistroPonto novoRegistro, JornadaConfig jornadaConfig, RangeJornadasAfetadas range, DeltaJornadasAfetadas deltaJornadasAfetadas) {
        
        var tempoDescansoEntreJornada = jornadaConfig.tempoDescansoEntreJornada();
        List<RegistroPonto> listaRegistrosJornadaAfetada = xrefPontoResumoRepository
        .listRegistroPontoByResumoPontoDiaIdOrderByCreatedAt(jornadaAfetada.getId());
        
        listaRegistrosJornadaAfetada.add(novoRegistro);
        recalcularJornadaAfetada(jornadaAfetada, listaRegistrosJornadaAfetada, jornadaConfig); 
        salvarXrefPontoResumo(novoRegistro.getUsuarioId(), jornadaAfetada.getId(), novoRegistro.getId(), novoRegistro.getCreatedAt());
        
        LocalDateTime inicioJornadaAfetada = jornadaAfetada.getPrimeiraBatida().minus(1, ChronoUnit.MILLIS);
        LocalDateTime fimJornadaAfetada = jornadaAfetada.getUltimaBatida().plus(1, ChronoUnit.MILLIS);

        LocalDateTime inicioRangeJornadaAfetada = inicioJornadaAfetada.minus(tempoDescansoEntreJornada);
        LocalDateTime fimRangeJornadaAfetada = fimJornadaAfetada.plus(tempoDescansoEntreJornada);

        Optional<ResumoPontoDia> jornadaAfetadaAnterior = xrefPontoResumoRepository.findByFuncionarioIdAndDataBetweenDesc(
                novoRegistro.getUsuarioId(), inicioRangeJornadaAfetada, inicioJornadaAfetada);

        Optional<ResumoPontoDia> jornadaAfetadaPosterior = xrefPontoResumoRepository.findByFuncionarioIdAndDataBetweenAsc(
                novoRegistro.getUsuarioId(), fimJornadaAfetada, fimRangeJornadaAfetada);
        
        if (jornadaAfetadaAnterior.isPresent()) {
            ResumoPontoDia jornadaAnterior = jornadaAfetadaAnterior.get();
            if (jornadaAfetada.getPrimeiraBatida() != null && jornadaAnterior.getUltimaBatida() != null) {
                Duration durationBetween = Duration.between(jornadaAnterior.getUltimaBatida(), jornadaAfetada.getPrimeiraBatida());

                if (durationBetween.compareTo(jornadaConfig.tempoDescansoEntreJornada()) < 0) {
                    // juntar lista de registros da jornada anterior e jornada afetada
                    
                    range.setJornadaAnteriorAno(jornadaAnterior.getPrimeiraBatida().getYear());
                    range.setJornadaAnteriorMes(jornadaAnterior.getPrimeiraBatida().getMonthValue());
                    
                    LocalDate dataRegistroAfetada = jornadaAfetada.getPrimeiraBatida().toLocalDate();
                    LocalDate dataRegistroAnterior = jornadaAnterior.getPrimeiraBatida().toLocalDate();

                    if(dataRegistroAfetada != dataRegistroAnterior) {
                        var quantidade = jornadaAnterior.getQuantidadeRegistros() + jornadaAfetada.getQuantidadeRegistros();
                        deltaJornadasAfetadas.setDeltaRegistrosPontoAfetada(quantidade);
        
                        Optional<MetricasDiariaEmpresa> metricasDiariaEmpresa = metricasDiariaEmpresaRepository.findByEmpresaIdAndDataRef(jornadaAfetada.getEmpresaId(), dataRegistroAnterior);
                        LOGGER_TECNICO.info("Métrica diária empresa encontrada com id: {}", metricasDiariaEmpresa.get().getId());
                        deletarMetricasDiariaEmpresaById(metricasDiariaEmpresa.get().getId());

                        juntarJornadas(jornadaAfetada, jornadaAnterior, listaRegistrosJornadaAfetada, jornadaConfig);
                        
                    }else{
                        juntarJornadas(jornadaAfetada, jornadaAnterior, listaRegistrosJornadaAfetada, jornadaConfig);
                    
                    }
                }
            }
        }
        if(jornadaAfetadaPosterior.isPresent()) {
            ResumoPontoDia jornadaPosterior = jornadaAfetadaPosterior.get();
            if (jornadaAfetada.getUltimaBatida() != null && jornadaPosterior.getPrimeiraBatida() != null) {
                Duration durationBetween = Duration.between(jornadaAfetada.getUltimaBatida(), jornadaPosterior.getPrimeiraBatida());

                if (durationBetween.compareTo(jornadaConfig.tempoDescansoEntreJornada()) < 0) {
                    // juntar lista de registros da jornada anterior e jornada afetada

                    range.setJornadaPosteriorAno(jornadaPosterior.getPrimeiraBatida().getYear());
                    range.setJornadaPosteriorMes(jornadaPosterior.getPrimeiraBatida().getMonthValue());
                    
                    LocalDate dataRegistroAfetada = jornadaAfetada.getPrimeiraBatida().toLocalDate();
                    LocalDate dataRegistroPosterior = jornadaPosterior.getPrimeiraBatida().toLocalDate();

                    if(dataRegistroAfetada != dataRegistroPosterior) {
                        var quantidade = jornadaAfetada.getQuantidadeRegistros() + jornadaPosterior.getQuantidadeRegistros();
                        deltaJornadasAfetadas.setDeltaRegistrosPontoAfetada(quantidade);
                        
                        Optional<MetricasDiariaEmpresa> metricasDiariaEmpresa = metricasDiariaEmpresaRepository.findByEmpresaIdAndDataRef(jornadaPosterior.getEmpresaId(), dataRegistroPosterior);
                        LOGGER_TECNICO.info("Métrica diária empresa encontrada com id: {}", metricasDiariaEmpresa.get().getId());
                        deletarMetricasDiariaEmpresaById(metricasDiariaEmpresa.get().getId());
                        juntarJornadas(jornadaAfetada, jornadaPosterior, listaRegistrosJornadaAfetada, jornadaConfig);
                    }else{
                        juntarJornadas(jornadaAfetada, jornadaPosterior, listaRegistrosJornadaAfetada, jornadaConfig);
                    }
                }
            }
        }
    }

    private void juntarJornadas(ResumoPontoDia jornadaAfetada, ResumoPontoDia jornadaAdd, List<RegistroPonto> listaRegistrosJornadaAfetada, JornadaConfig jornadaConfig) {
        List<RegistroPonto> listaRegistrosJornadaAdd = xrefPontoResumoRepository
        .listRegistroPontoByResumoPontoDiaIdOrderByCreatedAt(jornadaAdd.getId());

        listaRegistrosJornadaAfetada.addAll(listaRegistrosJornadaAdd);
        apagarJornadaAndXref(jornadaAdd.getId());
        recalcularXrefPontoResumo(jornadaAfetada, jornadaAdd, listaRegistrosJornadaAdd);

        recalcularJornadaAfetada(jornadaAfetada, listaRegistrosJornadaAfetada, jornadaConfig);
    }

    @Transactional
    private void deletarMetricasDiariaEmpresaById(UUID metricasDiariaEmpresaId) {
        LOGGER_TECNICO.info("Deletando métrica diária empresa com id: {}", metricasDiariaEmpresaId);
        
        Optional<MetricasDiariaEmpresa> metricasDiariaEmpresa = metricasDiariaEmpresaRepository.findById(metricasDiariaEmpresaId);
        
        if(metricasDiariaEmpresa.isPresent()) {

            MetricasDiariaEmpresa metricasDiariaEmpresaObj = metricasDiariaEmpresa.get();
        }
        metricasDiariaEmpresaRepository.deleteMetricasDiariaEmpresaById(metricasDiariaEmpresaId);
        LOGGER_TECNICO.info("Métrica diária empresa deletada com sucesso");
    }

    private void recalcularJornadaAfetada(ResumoPontoDia jornadaAfetada, List<RegistroPonto> listaRegistrosJornadaAfetada, JornadaConfig jornadaConfig) {
        calcularResumoDiaUtils.recalcularResumoDoDia(jornadaAfetada, listaRegistrosJornadaAfetada, jornadaConfig);
        reordenarTipos(listaRegistrosJornadaAfetada);
        salvarJornadaAfetada(jornadaAfetada);
    }
    
    private void reordenarTipos(List<RegistroPonto> listaRegistrosJornadaAfetada) {
        // Reordena tipoEntrada: primeira batida = entrada (true), segunda = saída (false), alternando
        boolean entrada = true;
        for (RegistroPonto registro : listaRegistrosJornadaAfetada) {
            registro.setTipoEntrada(entrada);
            entrada = !entrada;
            salvarRegistroPonto(registro);
        }
    }

    private void salvarXrefPontoResumo(UUID funcionarioId, UUID idJornada, UUID idRegistroPonto, LocalDateTime dataRegistro) {
        if (xrefPontoResumoRepository.existsByRegistroPontoId(idRegistroPonto)) {
            return;
        }
        var novoIdXref = UUID.randomUUID();
        xrefPontoResumoRepository.insert(novoIdXref, funcionarioId, idRegistroPonto, idJornada, dataRegistro);
    }

    private void salvarRegistroPonto(RegistroPonto registro) {
        registroPontoRepository.save(registro);
    }

    private void salvarJornadaAfetada(ResumoPontoDia jornadaAfetada) {
        resumoPontoDiaRepository.save(jornadaAfetada);
    }

    private void apagarJornadaAndXref(UUID idJornada) {
        xrefPontoResumoRepository.deleteByResumoPontoDiaId(idJornada);
        resumoPontoDiaRepository.deleteById(idJornada);
    }

    private void recalcularXrefPontoResumo(ResumoPontoDia jornadaAfetada, ResumoPontoDia jornadaAdd, List<RegistroPonto> listaRegistrosJornadaAdd) {
        for (RegistroPonto registro : listaRegistrosJornadaAdd) {
            salvarXrefPontoResumo(jornadaAfetada.getFuncionarioId(), jornadaAfetada.getId(), registro.getId(), registro.getCreatedAt());
        }
    }
}