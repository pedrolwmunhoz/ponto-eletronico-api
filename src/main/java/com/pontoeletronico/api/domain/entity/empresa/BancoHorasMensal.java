package com.pontoeletronico.api.domain.entity.empresa;

import com.pontoeletronico.api.config.DurationStringConverter;
import jakarta.persistence.*;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "banco_horas_mensal", uniqueConstraints = @UniqueConstraint(columnNames = {"funcionario_id", "ano_ref", "mes_ref"}))
public class BancoHorasMensal {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "funcionario_id", nullable = false)
    private UUID funcionarioId;

    @Column(name = "empresa_id", nullable = false)
    private UUID empresaId;

    @Column(name = "mes_ref", nullable = false)
    private Integer mesRef;

    @Column(name = "ano_ref", nullable = false)
    private Integer anoRef;

    @Convert(converter = DurationStringConverter.class)
    @Column(name = "total_horas_esperadas", nullable = false, length = 20)
    private Duration totalHorasEsperadas = Duration.ZERO;

    @Convert(converter = DurationStringConverter.class)
    @Column(name = "total_horas_trabalhadas", nullable = false, length = 20)
    private Duration totalHorasTrabalhadas = Duration.ZERO;

    @Column(name = "inconsistente", nullable = false)
    private Boolean inconsistente = false;

    @Column(name = "motivo_inconsistencia", length = 255)
    private String motivoInconsistencia;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;

    @Column(name = "data_desativacao")
    private Instant dataDesativacao;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getFuncionarioId() { return funcionarioId; }
    public void setFuncionarioId(UUID funcionarioId) { this.funcionarioId = funcionarioId; }
    public UUID getEmpresaId() { return empresaId; }
    public void setEmpresaId(UUID empresaId) { this.empresaId = empresaId; }
    public Integer getMesRef() { return mesRef; }
    public void setMesRef(Integer mesRef) { this.mesRef = mesRef; }
    public Integer getAnoRef() { return anoRef; }
    public void setAnoRef(Integer anoRef) { this.anoRef = anoRef; }
    public Duration getTotalHorasEsperadas() { return totalHorasEsperadas; }
    public void setTotalHorasEsperadas(Duration totalHorasEsperadas) { this.totalHorasEsperadas = totalHorasEsperadas != null ? totalHorasEsperadas : Duration.ZERO; }
    public Duration getTotalHorasTrabalhadas() { return totalHorasTrabalhadas; }
    public void setTotalHorasTrabalhadas(Duration totalHorasTrabalhadas) { this.totalHorasTrabalhadas = totalHorasTrabalhadas != null ? totalHorasTrabalhadas : Duration.ZERO; }
    public Boolean getInconsistente() { return inconsistente; }
    public void setInconsistente(Boolean inconsistente) { this.inconsistente = inconsistente; }
    public String getMotivoInconsistencia() { return motivoInconsistencia; }
    public void setMotivoInconsistencia(String motivoInconsistencia) { this.motivoInconsistencia = motivoInconsistencia; }
    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }
    public Instant getDataDesativacao() { return dataDesativacao; }
    public void setDataDesativacao(Instant dataDesativacao) { this.dataDesativacao = dataDesativacao; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BancoHorasMensal that = (BancoHorasMensal) o;
        return Objects.equals(id, that.id);
    }
    @Override
    public int hashCode() { return Objects.hash(id); }
}
