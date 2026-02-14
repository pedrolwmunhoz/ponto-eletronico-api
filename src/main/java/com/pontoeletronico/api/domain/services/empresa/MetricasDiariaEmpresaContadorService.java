package com.pontoeletronico.api.domain.services.empresa;

import com.pontoeletronico.api.domain.entity.empresa.MetricasDiariaEmpresa;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.MetricasDiariaEmpresaRepository;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.MetricasDiariaEmpresaLockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

@Service
public class MetricasDiariaEmpresaContadorService {

    private static final ZoneId ZONE = ZoneId.of("America/Sao_Paulo");
    private static final int LOCK_MAX_RETRIES = 30;
    private static final long LOCK_RETRY_SLEEP_MS = 50;

    private final MetricasDiariaEmpresaRepository metricasDiariaEmpresaRepository;
    private final MetricasDiariaEmpresaLockRepository metricasDiariaEmpresaLockRepository;

    public MetricasDiariaEmpresaContadorService(MetricasDiariaEmpresaRepository metricasDiariaEmpresaRepository,
                                                MetricasDiariaEmpresaLockRepository metricasDiariaEmpresaLockRepository) {
        this.metricasDiariaEmpresaRepository = metricasDiariaEmpresaRepository;
        this.metricasDiariaEmpresaLockRepository = metricasDiariaEmpresaLockRepository;
    }

    /** Adquire lock por (empresa_id, data_ref) ANTES de carregar/criar a métrica, para evitar race. */
    private void adquirirLock(UUID empresaId, LocalDate dataRef) {
        for (int i = 0; i < LOCK_MAX_RETRIES; i++) {
            int inserted = metricasDiariaEmpresaLockRepository.tryInsert(empresaId, dataRef);
            if (inserted == 1) return;
            try {
                Thread.sleep(LOCK_RETRY_SLEEP_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Lock de métrica interrompido", e);
            }
        }
        throw new IllegalStateException("Não foi possível adquirir lock (empresa=" + empresaId + ", dataRef=" + dataRef + ") após " + LOCK_MAX_RETRIES + " tentativas");
    }

    private void liberarLock(UUID empresaId, LocalDate dataRef) {
        metricasDiariaEmpresaLockRepository.releaseLock(empresaId, dataRef);
    }

    /** Obtém a métrica do dia de hoje; se não existir, cria copiando o dia anterior (total_ponto_hoje = 0). */
    @Transactional
    public MetricasDiariaEmpresa obterOuCriarMetricaHoje(UUID empresaId) {
        LocalDate hoje = LocalDate.now(ZONE);
        return metricasDiariaEmpresaRepository.findByEmpresaIdAndDataRef(empresaId, hoje)
                .orElseGet(() -> criarMetricaParaData(empresaId, hoje));
    }

    /** Incrementa quantidade de funcionários na métrica do dia (ao criar funcionário). */
    @Transactional
    public void incrementarQuantidadeFuncionarios(UUID empresaId) {
        LocalDate dataRef = LocalDate.now(ZONE);
        try {
            adquirirLock(empresaId, dataRef);
            MetricasDiariaEmpresa m = obterOuCriarMetricaParaData(empresaId, dataRef);
            m.setQuantidadeFuncionarios(m.getQuantidadeFuncionarios() + 1);
            metricasDiariaEmpresaRepository.save(m);
        } finally {
            liberarLock(empresaId, dataRef);
        }
    }

    /** Decrementa quantidade de funcionários na métrica do dia (ao deletar funcionário). */
    @Transactional
    public void decrementarQuantidadeFuncionarios(UUID empresaId) {
        LocalDate dataRef = LocalDate.now(ZONE);
        try {
            adquirirLock(empresaId, dataRef);
            MetricasDiariaEmpresa m = obterOuCriarMetricaParaData(empresaId, dataRef);
            m.setQuantidadeFuncionarios(Math.max(0, m.getQuantidadeFuncionarios() - 1));
            metricasDiariaEmpresaRepository.save(m);
        } finally {
            liberarLock(empresaId, dataRef);
        }
    }

    /** Incrementa total_ponto_hoje ao bater ponto pelo app. */
    @Transactional
    public void incrementarRegistrosPonto(UUID empresaId) {
        LocalDate dataRef = LocalDate.now(ZONE);
        try {
            adquirirLock(empresaId, dataRef);
            MetricasDiariaEmpresa m = obterOuCriarMetricaParaData(empresaId, dataRef);
            m.setTotalPontoHoje(m.getTotalPontoHoje() + 1);
            metricasDiariaEmpresaRepository.save(m);
        } finally {
            liberarLock(empresaId, dataRef);
        }
    }

    /** Incrementa solicitações pendentes (ao criar solicitação de ponto manual). */
    @Transactional
    public void incrementarSolicitacoesPendentes(UUID empresaId) {
        LocalDate dataRef = LocalDate.now(ZONE);
        try {
            adquirirLock(empresaId, dataRef);
            MetricasDiariaEmpresa m = obterOuCriarMetricaParaData(empresaId, dataRef);
            m.setSolicitacoesPendentes(m.getSolicitacoesPendentes() + 1);
            metricasDiariaEmpresaRepository.save(m);
        } finally {
            liberarLock(empresaId, dataRef);
        }
    }

    /** Decrementa solicitações pendentes (ao aprovar ou reprovar solicitação). */
    @Transactional
    public void decrementarSolicitacoesPendentes(UUID empresaId) {
        LocalDate dataRef = LocalDate.now(ZONE);
        try {
            adquirirLock(empresaId, dataRef);
            MetricasDiariaEmpresa m = obterOuCriarMetricaParaData(empresaId, dataRef);
            m.setSolicitacoesPendentes(Math.max(0, m.getSolicitacoesPendentes() - 1));
            metricasDiariaEmpresaRepository.save(m);
        } finally {
            liberarLock(empresaId, dataRef);
        }
    }

    /** Ajusta total_ponto_hoje da métrica do dia: +1 entrada/saída manual, -1 soft delete. Contador é por dia (data_ref). */
    @Transactional
    public void ajustarRegistrosPontoParaData(UUID empresaId, LocalDate dataRegistro, int delta) {
        try {
            adquirirLock(empresaId, dataRegistro);
            MetricasDiariaEmpresa m = obterOuCriarMetricaParaData(empresaId, dataRegistro);
            m.setTotalPontoHoje(Math.max(0, m.getTotalPontoHoje() + delta));
            metricasDiariaEmpresaRepository.save(m);
        } finally {
            liberarLock(empresaId, dataRegistro);
        }
    }

    /** Verifica se já existe métrica para a data (sem criar). */
    public boolean existeMetricaParaData(UUID empresaId, LocalDate dataRef) {
        return metricasDiariaEmpresaRepository.findByEmpresaIdAndDataRef(empresaId, dataRef).isPresent();
    }

    /**
     * Ajuste de métricas após entrada manual ou soft delete. Chamar sempre.
     * Busca métrica pela data do registro; se não existir, cria nova com data_ref = dataRegistro (data do registro).
     * Aplica os deltas (registros e horas) na métrica desse dia específico.
     */
    @Transactional
    public void ajustarMetricasAposRecalculo(UUID empresaId, LocalDate dataRegistro, int deltaRegistrosPonto, Duration deltaHoras) {
        try {
            adquirirLock(empresaId, dataRegistro);
            MetricasDiariaEmpresa m = obterOuCriarMetricaParaData(empresaId, dataRegistro);
            m.setTotalPontoHoje(Math.max(0, m.getTotalPontoHoje() + deltaRegistrosPonto));
            if (deltaHoras != null && !deltaHoras.isZero()) {
                m.setTotalDoDia(m.getTotalDoDia().plus(deltaHoras));
                if (m.getTotalDoDia().isNegative()) {
                    m.setTotalDoDia(Duration.ZERO);
                }
            }
            metricasDiariaEmpresaRepository.save(m);
        } finally {
            liberarLock(empresaId, dataRegistro);
        }
    }

    /** Busca métrica pela data; se não existir, cria nova com data_ref = dataRef (data do registro). Chamar só após adquirir lock. */
    @Transactional
    public MetricasDiariaEmpresa obterOuCriarMetricaParaData(UUID empresaId, LocalDate dataRef) {
        return metricasDiariaEmpresaRepository.findByEmpresaIdAndDataRef(empresaId, dataRef)
                .orElseGet(() -> criarMetricaParaData(empresaId, dataRef));
    }

    /** Cria métrica nova com data_ref = dataRef (data do registro); copia contadores do dia anterior; total_ponto_hoje = 0, total_do_dia = ZERO. */
    private MetricasDiariaEmpresa criarMetricaParaData(UUID empresaId, LocalDate dataRef) {
        var anterior = metricasDiariaEmpresaRepository.findTopByEmpresaIdAndDataRefBeforeOrderByDataRefDesc(empresaId, dataRef);
        MetricasDiariaEmpresa m = new MetricasDiariaEmpresa();
        m.setId(UUID.randomUUID());
        m.setEmpresaId(empresaId);
        m.setDataRef(dataRef);
        m.setAnoRef(dataRef.getYear());
        m.setMesRef(dataRef.getMonthValue());
        m.setQuantidadeFuncionarios(anterior.map(MetricasDiariaEmpresa::getQuantidadeFuncionarios).orElse(0));
        m.setSolicitacoesPendentes(anterior.map(MetricasDiariaEmpresa::getSolicitacoesPendentes).orElse(0));
        m.setTotalDoDia(Duration.ZERO);
        m.setTotalPontoHoje(0);
        return metricasDiariaEmpresaRepository.save(m);
    }
}
