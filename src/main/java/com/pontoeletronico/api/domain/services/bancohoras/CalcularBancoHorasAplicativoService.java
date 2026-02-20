package com.pontoeletronico.api.domain.services.bancohoras;

import com.pontoeletronico.api.domain.entity.registro.EstadoJornadaFuncionario;
import com.pontoeletronico.api.domain.entity.registro.ResumoPontoDia;
import com.pontoeletronico.api.domain.entity.registro.XrefPontoResumo;
import com.pontoeletronico.api.domain.services.bancohoras.record.JornadaConfig;
import com.pontoeletronico.api.domain.services.bancohoras.utils.CalcularResumoDiaUtils;
import com.pontoeletronico.api.domain.services.empresa.MetricasDiariaEmpresaContadorService;
import com.pontoeletronico.api.domain.services.util.ObterJornadaConfigUtils;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.EmpresaJornadaConfigRepository;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.JornadaFuncionarioConfigRepository;
import com.pontoeletronico.api.infrastructure.output.repository.registro.EstadoJornadaFuncionarioRepository;
import com.pontoeletronico.api.infrastructure.output.repository.registro.RegistroPontoRepository;
import com.pontoeletronico.api.infrastructure.output.repository.registro.ResumoPontoDiaRepository;
import com.pontoeletronico.api.infrastructure.output.repository.registro.XrefPontoResumoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
/**
 * Lógica de jornada para registro via aplicativo (tablet público ou app funcionário).
 * Usa EstadoJornadaFuncionario (ultimaBatida, tipoUltimaBatida, ultimaJornadaId) e tempoDescansoEntreJornada:
 * - Se o novo registro e o último registro estão no mesmo range (diff &lt; tempoDescansoEntreJornada) → mesma jornada.
 * - Caso contrário → nova jornada com tipo do ponto ENTRADA.
 */
@Service
@AllArgsConstructor
public class CalcularBancoHorasAplicativoService {

    private final EstadoJornadaFuncionarioRepository estadoJornadaFuncionarioRepository;
    private final RegistroPontoRepository registroPontoRepository;
    private final ResumoPontoDiaRepository resumoPontoDiaRepository;
    private final XrefPontoResumoRepository xrefPontoResumoRepository;
    private final BancoHorasMensalService bancoHorasMensalService;
    private final MetricasDiariaEmpresaContadorService metricasDiariaEmpresaContadorService;
    private final CalcularResumoDiaUtils calcularResumoDiaUtils;
    private final ObterJornadaConfigUtils obterJornadaConfigUtils;


    /**
     * Processa o registro de ponto feito via aplicativo: decide mesma jornada ou nova (ENTRADA) e atualiza estado.
     */
    @Transactional
    public void processarRegistroAplicativo(UUID funcionarioId, UUID empresaId, UUID idRegistro, LocalDateTime dataRegistro) {
        var estadoOpt = estadoJornadaFuncionarioRepository.findByFuncionarioIdForUpdate(funcionarioId);
        var jornadaConfig = obterJornadaConfigUtils.obterJornadaConfig(empresaId, funcionarioId);
        var tempoDescanso = jornadaConfig.tempoDescansoEntreJornada();

        boolean mesmaJornada = false;
        ResumoPontoDia jornadaAtual = null;
        Duration diff = null;

        Optional<ResumoPontoDia> resumoDiaOpt = xrefPontoResumoRepository.findbyFuncionarioIdAndDataRef(funcionarioId, dataRegistro);
        Optional<ResumoPontoDia> resumoDiaAnteriorOpt = xrefPontoResumoRepository.findbyFuncionarioIdAndDataRef(funcionarioId, dataRegistro.minus(1, ChronoUnit.DAYS));

        if (resumoDiaOpt.isPresent()) {
            jornadaAtual = resumoDiaOpt.get();
            diff = Duration.between(jornadaAtual.getUltimaBatida(), dataRegistro);
            if (!diff.isNegative() && diff.compareTo(tempoDescanso) <= 0) mesmaJornada = true;
            
        }else if (resumoDiaAnteriorOpt.isPresent()) {
            jornadaAtual = resumoDiaAnteriorOpt.get();
            diff = Duration.between(jornadaAtual.getUltimaBatida(), dataRegistro);
            if (!diff.isNegative() && diff.compareTo(tempoDescanso) <= 0) mesmaJornada = true;
            
        }

        var tipoNovoEntrada = false;
        if (jornadaAtual != null && jornadaAtual.getQuantidadeRegistros() > 0) {
            tipoNovoEntrada = jornadaAtual.getQuantidadeRegistros() % 2 != 0 ? true : false;
        }

        if (mesmaJornada) {
            vincularAResumoERecalcular(funcionarioId, empresaId, idRegistro, dataRegistro, tipoNovoEntrada, jornadaAtual, estadoOpt.get(), diff, jornadaConfig);
        } else {
            criarNovaJornadaEAtualizarEstado(funcionarioId, empresaId, idRegistro, dataRegistro, estadoOpt.orElse(null), jornadaConfig);
        }
    }

    /** Registro por app na mesma jornada: só incrementar (uma nova xref), não recriar lista. */
    private void vincularAResumoERecalcular(UUID funcionarioId, UUID empresaId, UUID idRegistro, LocalDateTime dataRegistro,
                                            boolean tipoNovoEntrada, ResumoPontoDia resumo, EstadoJornadaFuncionario estado, Duration diff, JornadaConfig jornadaConfig) {
        if (!xrefPontoResumoRepository.existsByRegistroPontoId(idRegistro)) {
            var xref = new XrefPontoResumo();
            xref.setId(UUID.randomUUID());
            xref.setFuncionarioId(funcionarioId);
            xref.setRegistroPontoId(idRegistro);
            xref.setResumoPontoDiaId(resumo.getId());
            xref.setDataRef(dataRegistro);
            xrefPontoResumoRepository.save(xref);
        }

        var listaXref = xrefPontoResumoRepository.findByResumoPontoDiaIdOrderByDataRefAsc(resumo.getId());
        var idsRegistros = listaXref.stream().map(XrefPontoResumo::getRegistroPontoId).collect(Collectors.toSet());
        var registros = registroPontoRepository.findByIdInOrderByCreatedAtAsc(idsRegistros);

        calcularResumoDiaUtils.recalcularResumoDoDia(resumo, registros, jornadaConfig);
        resumoPontoDiaRepository.save(resumo);

        if (!tipoNovoEntrada) {
            bancoHorasMensalService.acumularNoMensal(funcionarioId, empresaId, resumo.getPrimeiraBatida().getYear(), resumo.getPrimeiraBatida().getMonthValue(), null, diff);
            metricasDiariaEmpresaContadorService.ajustarMetricasAposRecalculo(empresaId, resumo.getPrimeiraBatida().toLocalDate(), 1, diff);
        }else{
            metricasDiariaEmpresaContadorService.incrementarRegistrosPonto(empresaId, resumo.getPrimeiraBatida().toLocalDate());
        }
        estado.setUltimaBatida(dataRegistro);
        estado.setUltimaJornadaId(resumo.getId());
        estado.setTipoUltimaBatida(tipoNovoEntrada ? EstadoJornadaFuncionario.TIPO_ENTRADA : EstadoJornadaFuncionario.TIPO_SAIDA);
        estado.setUpdatedAt(LocalDateTime.now());
        estadoJornadaFuncionarioRepository.save(estado);
    }

    private void criarNovaJornadaEAtualizarEstado(UUID funcionarioId, UUID empresaId, UUID idRegistro, LocalDateTime dataRegistro,
                                                 EstadoJornadaFuncionario estadoExistente, JornadaConfig jornadaConfig) {
        var registro = registroPontoRepository.findByIdAndUsuarioId(idRegistro, funcionarioId).orElse(null);
        if (registro != null) {
            registro.setTipoEntrada(true);
            registroPontoRepository.save(registro);
        }

        var now = LocalDateTime.now();
        var resumo = new ResumoPontoDia();
        resumo.setId(UUID.randomUUID());
        resumo.setFuncionarioId(funcionarioId);
        resumo.setEmpresaId(empresaId);
        resumo.setPrimeiraBatida(dataRegistro);
        resumo.setUltimaBatida(dataRegistro);
        resumo.setTotalHorasTrabalhadas(Duration.ZERO);
        resumo.setTotalHorasEsperadas(jornadaConfig.cargaDiaria());
        resumo.setInconsistente(true);
        resumo.setMotivoInconsistencia("IMPAR");
        resumo.setDataRef(dataRegistro); // dataRef do resumo = data da primeira batida
        resumoPontoDiaRepository.save(resumo);

        if (!xrefPontoResumoRepository.existsByRegistroPontoId(idRegistro)) {
            var xref = new XrefPontoResumo();
            xref.setId(UUID.randomUUID());
            xref.setFuncionarioId(funcionarioId);
            xref.setRegistroPontoId(idRegistro);
            xref.setResumoPontoDiaId(resumo.getId());
            xref.setDataRef(dataRegistro);
            xrefPontoResumoRepository.save(xref);
        }

        EstadoJornadaFuncionario estado = estadoExistente;
        if (estado == null) {
            estado = new EstadoJornadaFuncionario();
            estado.setFuncionarioId(funcionarioId);
            estado.setEmpresaId(empresaId);
        }
        estado.setUltimaBatida(dataRegistro);
        estado.setTipoUltimaBatida(EstadoJornadaFuncionario.TIPO_ENTRADA);
        estado.setUltimaJornadaId(resumo.getId());
        estado.setUpdatedAt(now);
        estadoJornadaFuncionarioRepository.save(estado);

        bancoHorasMensalService.acumularNoMensal(funcionarioId, empresaId, dataRegistro.getYear(), dataRegistro.getMonthValue(), jornadaConfig.cargaDiaria(), null);
        metricasDiariaEmpresaContadorService.incrementarRegistrosPonto(empresaId, dataRegistro.toLocalDate());
    }

}
