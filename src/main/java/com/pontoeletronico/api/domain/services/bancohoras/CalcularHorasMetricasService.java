package com.pontoeletronico.api.domain.services.bancohoras;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;

import com.pontoeletronico.api.domain.entity.registro.ResumoPontoDia;
import com.pontoeletronico.api.domain.entity.registro.RegistroPonto;
import com.pontoeletronico.api.domain.services.bancohoras.record.JornadaConfig;
import com.pontoeletronico.api.domain.services.bancohoras.utils.CalcularResumoDiaUtils;
import com.pontoeletronico.api.infrastructure.output.repository.registro.ResumoPontoDiaRepository;
import com.pontoeletronico.api.infrastructure.output.repository.registro.RegistroPontoRepository;
import com.pontoeletronico.api.infrastructure.output.repository.registro.XrefPontoResumoRepository;

@Service
@AllArgsConstructor
public class CalcularHorasMetricasService {

    private final ResumoPontoDiaRepository resumoPontoDiaRepository;
    private final RegistroPontoRepository registroPontoRepository;
    private final XrefPontoResumoRepository xrefPontoResumoRepository;
    private final BancoHorasMensalService calcularBancoHorasMensal;
    private final CalcularResumoDiaUtils calcularResumoDiaUtils;
    
    public void calcularHorasAposEntradaManual(UUID empresaId, RegistroPonto registroPonto, JornadaConfig jornadaConfig) {
         
        var tempoDescansoEntreJornada = jornadaConfig.tempoDescansoEntreJornada();
        LocalDateTime dataAtualInicio = registroPonto.getCreatedAt();
        LocalDateTime dataAtualFim = registroPonto.getCreatedAt();
        
        LocalDateTime inicioRangeJornada = dataAtualInicio.minus(tempoDescansoEntreJornada).plus(1, ChronoUnit.MILLIS);
        LocalDateTime fimRangeJornada = dataAtualFim.plus(tempoDescansoEntreJornada).minus(1, ChronoUnit.MILLIS);
        
        Optional<ResumoPontoDia> jornadaAfetadaDataAnterior = xrefPontoResumoRepository.findByFuncionarioIdAndDataBetweenAsc(registroPonto.getUsuarioId(), inicioRangeJornada, dataAtualInicio);
        Optional<ResumoPontoDia> jornadaAfetadaDataPosterior = null;

        UUID jornadaAfetadaId = null;
        if(jornadaAfetadaDataAnterior.isPresent()) {
            //existe jornada afetada - tratar
            handleJornadaAfetada(jornadaAfetadaDataAnterior.get(), registroPonto, jornadaConfig);
            jornadaAfetadaId = jornadaAfetadaDataAnterior.get().getId();
        }else{

            jornadaAfetadaDataPosterior = xrefPontoResumoRepository.findByFuncionarioIdAndDataBetweenAsc(registroPonto.getUsuarioId(), dataAtualFim, fimRangeJornada);
            if(jornadaAfetadaDataPosterior.isPresent()) {
                //existe jornada afetada - tratar
                handleJornadaAfetada(jornadaAfetadaDataPosterior.get(), registroPonto, jornadaConfig);
                jornadaAfetadaId = jornadaAfetadaDataPosterior.get().getId();
            }else{
                //nao existe jornada afetada - criar nova jornada
                var idNovoResumo = UUID.randomUUID();
                resumoPontoDiaRepository.insert(idNovoResumo, registroPonto.getUsuarioId(), empresaId, registroPonto.getCreatedAt(), registroPonto.getCreatedAt(), Duration.ZERO, Duration.ZERO, false, null, registroPonto.getCreatedAt());
                jornadaAfetadaId = idNovoResumo;
            }
        }
        salvarXrefPontoResumo(registroPonto.getUsuarioId(), jornadaAfetadaId, registroPonto.getId(), registroPonto.getCreatedAt());
        calcularBancoHorasMensal.recalcularMensal(registroPonto.getUsuarioId(), empresaId, registroPonto.getCreatedAt().getYear(), registroPonto.getCreatedAt().getMonthValue());
    }

    private void handleJornadaAfetada(ResumoPontoDia jornadaAfetada, RegistroPonto novoRegistro, JornadaConfig jornadaConfig) {
        
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
            // pega o primeira batida da jornadaAfetada e compara com a ultima batida da jornada anterior usando Duration
            
            ResumoPontoDia jornadaAnterior = jornadaAfetadaAnterior.get();

            if (jornadaAfetada.getPrimeiraBatida() != null && jornadaAnterior.    getUltimaBatida() != null) {
                Duration durationBetween = Duration.between(jornadaAnterior.getUltimaBatida(), jornadaAfetada.getPrimeiraBatida());

                if (durationBetween.compareTo(jornadaConfig.tempoDescansoEntreJornada()) < 0) {
                    // juntar lista de registros da jornada anterior e jornada afetada
                    juntarJornadas(jornadaAfetada, jornadaAnterior, listaRegistrosJornadaAfetada, jornadaConfig);
                }
            }
        }
        if(jornadaAfetadaPosterior.isPresent()) {
            ResumoPontoDia jornadaPosterior = jornadaAfetadaPosterior.get();

            if (jornadaAfetada.getUltimaBatida() != null && jornadaPosterior.getPrimeiraBatida() != null) {
                Duration durationBetween = Duration.between(jornadaAfetada.getUltimaBatida(), jornadaPosterior.getPrimeiraBatida());

                if (durationBetween.compareTo(jornadaConfig.tempoDescansoEntreJornada()) < 0) {
                    // juntar lista de registros da jornada anterior e jornada afetada
                    juntarJornadas(jornadaAfetada, jornadaPosterior, listaRegistrosJornadaAfetada, jornadaConfig);
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