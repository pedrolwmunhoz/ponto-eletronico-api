package com.pontoeletronico.api.domain.services.bancohoras;

import com.pontoeletronico.api.domain.entity.registro.EstadoJornadaFuncionario;
import com.pontoeletronico.api.domain.entity.registro.ResumoPontoDia;
import com.pontoeletronico.api.domain.entity.registro.XrefPontoResumo;
import com.pontoeletronico.api.domain.services.bancohoras.record.JornadaConfig;
import com.pontoeletronico.api.domain.services.bancohoras.utils.CalcularResumoDiaUtils;
import com.pontoeletronico.api.domain.services.empresa.MetricasDiariaEmpresaContadorService;
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
    private final EmpresaJornadaConfigRepository empresaJornadaConfigRepository;
    private final JornadaFuncionarioConfigRepository jornadaFuncionarioConfigRepository;
    private final RegistroPontoRepository registroPontoRepository;
    private final ResumoPontoDiaRepository resumoPontoDiaRepository;
    private final XrefPontoResumoRepository xrefPontoResumoRepository;
    private final BancoHorasMensalService bancoHorasMensalService;
    private final MetricasDiariaEmpresaContadorService metricasDiariaEmpresaContadorService;
    private final CalcularResumoDiaUtils calcularResumoDiaUtils;


    /**
     * Processa o registro de ponto feito via aplicativo: decide mesma jornada ou nova (ENTRADA) e atualiza estado.
     */
    @Transactional
    public void processarRegistroAplicativo(UUID funcionarioId, UUID empresaId, UUID idRegistro, LocalDateTime dataRegistro, boolean tipoEntradaAtual, JornadaConfig jornadaConfig) {
        var estadoOpt = estadoJornadaFuncionarioRepository.findByFuncionarioIdForUpdate(funcionarioId);
        var tempoDescanso = jornadaConfig.tempoDescansoEntreJornada();

        boolean mesmaJornada = false;
        ResumoPontoDia jornadaAtual = null;
        Duration diff = null;

        if (estadoOpt.isPresent()) {
            var estado = estadoOpt.get();
            diff = Duration.between(estado.getUltimaBatida(), dataRegistro);
            UUID ultimaJornadaId = estado.getUltimaJornadaId();
            if (ultimaJornadaId != null) {
                jornadaAtual = resumoPontoDiaRepository.findById(ultimaJornadaId).orElse(null);
                if (jornadaAtual != null && !diff.isNegative() && diff.compareTo(tempoDescanso) <= 0) mesmaJornada = true;
            }
        }

        if (mesmaJornada && jornadaAtual != null) {
            vincularAResumoERecalcular(funcionarioId, empresaId, idRegistro, dataRegistro, tipoEntradaAtual, jornadaAtual, estadoOpt.get(), diff, jornadaConfig);
        } else {
            criarNovaJornadaEAtualizarEstado(funcionarioId, empresaId, idRegistro, dataRegistro, estadoOpt.orElse(null), jornadaConfig);
        }
    }

    /** Registro por app na mesma jornada: só incrementar (uma nova xref), não recriar lista. */
    private void vincularAResumoERecalcular(UUID funcionarioId, UUID empresaId, UUID idRegistro, LocalDateTime dataRegistro,
                                            boolean tipoEntradaNovo, ResumoPontoDia resumo, EstadoJornadaFuncionario estado, Duration diff, JornadaConfig jornadaConfig) {
        if (!xrefPontoResumoRepository.existsByRegistroPontoId(idRegistro)) {
            var xref = new XrefPontoResumo();
            xref.setId(UUID.randomUUID());
            xref.setRegistroPontoId(idRegistro);
            xref.setResumoPontoDiaId(resumo.getId());
            xrefPontoResumoRepository.save(xref);
        }

        var listaXref = xrefPontoResumoRepository.findByResumoPontoDiaIdOrderByCreatedAtAsc(resumo.getId());
        var idsRegistros = listaXref.stream().map(XrefPontoResumo::getRegistroPontoId).collect(Collectors.toSet());
        var registros = registroPontoRepository.findByIdInOrderByCreatedAtAsc(idsRegistros);

        calcularResumoDiaUtils.recalcularResumoDoDia(resumo, registros, jornadaConfig);
        resumoPontoDiaRepository.save(resumo);

        if (!tipoEntradaNovo) {
            bancoHorasMensalService.acumularNoMensal(funcionarioId, empresaId, dataRegistro.getYear(), dataRegistro.getMonthValue(), null, diff);
            metricasDiariaEmpresaContadorService.ajustarMetricasAposRecalculo(empresaId, dataRegistro.toLocalDate(), 0, diff);
        }else{
            metricasDiariaEmpresaContadorService.incrementarRegistrosPonto(empresaId);
        }
        estado.setUltimaBatida(dataRegistro);
        estado.setUltimaJornadaId(resumo.getId());
        estado.setTipoUltimaBatida(tipoEntradaNovo ? EstadoJornadaFuncionario.TIPO_ENTRADA : EstadoJornadaFuncionario.TIPO_SAIDA);
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
        resumo.setCreatedAt(now);
        resumoPontoDiaRepository.save(resumo);

        if (!xrefPontoResumoRepository.existsByRegistroPontoId(idRegistro)) {
            var xref = new XrefPontoResumo();
            xref.setId(UUID.randomUUID());
            xref.setRegistroPontoId(idRegistro);
            xref.setResumoPontoDiaId(resumo.getId());
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
    }

}
