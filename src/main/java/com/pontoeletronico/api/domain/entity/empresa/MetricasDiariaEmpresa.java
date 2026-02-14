package com.pontoeletronico.api.domain.entity.empresa;

import com.pontoeletronico.api.config.DurationStringConverter;
import jakarta.persistence.*;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "metricas_diaria_empresa", uniqueConstraints = @UniqueConstraint(columnNames = {"empresa_id", "data_ref"}))
public class MetricasDiariaEmpresa {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "empresa_id", nullable = false)
    private UUID empresaId;

    @Column(name = "data_ref", nullable = false)
    private LocalDate dataRef;

    @Column(name = "ano_ref", nullable = false)
    private Integer anoRef;

    @Column(name = "mes_ref", nullable = false)
    private Integer mesRef;

    @Column(name = "quantidade_funcionarios", nullable = false)
    private Integer quantidadeFuncionarios = 0;

    @Column(name = "solicitacoes_pendentes", nullable = false)
    private Integer solicitacoesPendentes = 0;

    @Convert(converter = DurationStringConverter.class)
    @Column(name = "total_do_dia", nullable = false, length = 20)
    private Duration totalDoDia = Duration.ZERO;

    @Column(name = "total_ponto_hoje", nullable = false)
    private Integer totalPontoHoje = 0;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getEmpresaId() { return empresaId; }
    public void setEmpresaId(UUID empresaId) { this.empresaId = empresaId; }
    public LocalDate getDataRef() { return dataRef; }
    public void setDataRef(LocalDate dataRef) { this.dataRef = dataRef; }
    public Integer getAnoRef() { return anoRef; }
    public void setAnoRef(Integer anoRef) { this.anoRef = anoRef; }
    public Integer getMesRef() { return mesRef; }
    public void setMesRef(Integer mesRef) { this.mesRef = mesRef; }
    public Integer getQuantidadeFuncionarios() { return quantidadeFuncionarios; }
    public void setQuantidadeFuncionarios(Integer quantidadeFuncionarios) { this.quantidadeFuncionarios = quantidadeFuncionarios != null ? quantidadeFuncionarios : 0; }
    public Integer getSolicitacoesPendentes() { return solicitacoesPendentes; }
    public void setSolicitacoesPendentes(Integer solicitacoesPendentes) { this.solicitacoesPendentes = solicitacoesPendentes != null ? solicitacoesPendentes : 0; }
    public Duration getTotalDoDia() { return totalDoDia; }
    public void setTotalDoDia(Duration totalDoDia) { this.totalDoDia = totalDoDia != null ? totalDoDia : Duration.ZERO; }
    public Integer getTotalPontoHoje() { return totalPontoHoje; }
    public void setTotalPontoHoje(Integer totalPontoHoje) { this.totalPontoHoje = totalPontoHoje != null ? totalPontoHoje : 0; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MetricasDiariaEmpresa that = (MetricasDiariaEmpresa) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}
