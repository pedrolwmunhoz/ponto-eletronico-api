package com.pontoeletronico.api.domain.services.empresa;

import com.pontoeletronico.api.domain.entity.empresa.MetricasDiariaEmpresa;
import com.pontoeletronico.api.infrastructure.input.dto.empresa.MetricasDiariaEmpresaResponse;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.MetricasDiariaEmpresaRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class MetricasDiariaEmpresaConsultaService {

    private static final ZoneId ZONE = ZoneId.of("America/Sao_Paulo");

    private final MetricasDiariaEmpresaRepository metricasDiariaEmpresaRepository;

    public MetricasDiariaEmpresaConsultaService(MetricasDiariaEmpresaRepository metricasDiariaEmpresaRepository) {
        this.metricasDiariaEmpresaRepository = metricasDiariaEmpresaRepository;
    }

    /**
     * Lista métricas diárias da empresa no período (data_ref entre dataInicio e dataFim, inclusive).
     */
    public List<MetricasDiariaEmpresaResponse> listarPorDataInicioFim(UUID empresaId, LocalDate dataInicio, LocalDate dataFim) {
        List<MetricasDiariaEmpresa> lista = metricasDiariaEmpresaRepository.findByEmpresaIdAndDataRefBetween(empresaId, dataInicio, dataFim);
        return lista.stream().map(this::toResponse).toList();
    }

    /**
     * Retorna somente a métrica do dia de hoje (Total do dia / Registros Hoje).
     * Se não existir métrica para hoje, retorna resposta com data_ref=hoje e totais zerados.
     */
    public Optional<MetricasDiariaEmpresaResponse> buscarMetricaHojeOuUltima(UUID empresaId) {
        LocalDate hoje = LocalDate.now(ZONE);
        Optional<MetricasDiariaEmpresa> opt = metricasDiariaEmpresaRepository.findByEmpresaIdAndDataRef(empresaId, hoje);
        if (opt.isPresent()) {
            return opt.map(this::toResponse);
        }
        return Optional.of(metricasZeradasParaData(empresaId, hoje));
    }

    /** Resposta com total_do_dia e total_ponto_hoje zerados para a data (usado quando não existe métrica para hoje). */
    private MetricasDiariaEmpresaResponse metricasZeradasParaData(UUID empresaId, LocalDate dataRef) {
        Optional<MetricasDiariaEmpresa> ultima = metricasDiariaEmpresaRepository.findTopByEmpresaIdOrderByDataRefDesc(empresaId);
        int qtdFunc = ultima.map(MetricasDiariaEmpresa::getQuantidadeFuncionarios).orElse(0);
        int solicitPend = ultima.map(MetricasDiariaEmpresa::getSolicitacoesPendentes).orElse(0);
        return new MetricasDiariaEmpresaResponse(
                null,
                dataRef,
                dataRef.getYear(),
                dataRef.getMonthValue(),
                qtdFunc,
                solicitPend,
                Duration.ZERO,
                0
        );
    }

    private MetricasDiariaEmpresaResponse toResponse(MetricasDiariaEmpresa m) {
        return new MetricasDiariaEmpresaResponse(
                m.getId(),
                m.getDataRef(),
                m.getAnoRef(),
                m.getMesRef(),
                m.getQuantidadeFuncionarios(),
                m.getSolicitacoesPendentes(),
                m.getTotalDoDia(),
                m.getTotalPontoHoje()
        );
    }
}
