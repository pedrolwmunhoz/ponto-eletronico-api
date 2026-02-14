package com.pontoeletronico.api.domain.entity.empresa;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "empresa_compliance")
public class EmpresaCompliance {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "empresa_id", nullable = false, unique = true)
    private UUID empresaId;

    @Column(name = "controle_ponto_obrigatorio", nullable = false)
    private Boolean controlePontoObrigatorio = true;

    @Column(name = "tipo_modelo_ponto_id", nullable = false)
    private Integer tipoModeloPontoId;

    @Column(name = "tempo_retencao_anos", nullable = false)
    private Integer tempoRetencaoAnos;

    @Column(name = "auditoria_ativa", nullable = false)
    private Boolean auditoriaAtiva = true;

    @Column(name = "assinatura_digital_obrigatoria", nullable = false)
    private Boolean assinaturaDigitalObrigatoria = true;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getEmpresaId() { return empresaId; }
    public void setEmpresaId(UUID empresaId) { this.empresaId = empresaId; }
    public Boolean getControlePontoObrigatorio() { return controlePontoObrigatorio; }
    public void setControlePontoObrigatorio(Boolean controlePontoObrigatorio) { this.controlePontoObrigatorio = controlePontoObrigatorio; }
    public Integer getTipoModeloPontoId() { return tipoModeloPontoId; }
    public void setTipoModeloPontoId(Integer tipoModeloPontoId) { this.tipoModeloPontoId = tipoModeloPontoId; }
    public Integer getTempoRetencaoAnos() { return tempoRetencaoAnos; }
    public void setTempoRetencaoAnos(Integer tempoRetencaoAnos) { this.tempoRetencaoAnos = tempoRetencaoAnos; }
    public Boolean getAuditoriaAtiva() { return auditoriaAtiva; }
    public void setAuditoriaAtiva(Boolean auditoriaAtiva) { this.auditoriaAtiva = auditoriaAtiva; }
    public Boolean getAssinaturaDigitalObrigatoria() { return assinaturaDigitalObrigatoria; }
    public void setAssinaturaDigitalObrigatoria(Boolean assinaturaDigitalObrigatoria) { this.assinaturaDigitalObrigatoria = assinaturaDigitalObrigatoria; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmpresaCompliance that = (EmpresaCompliance) o;
        return Objects.equals(id, that.id);
    }
    @Override
    public int hashCode() { return Objects.hash(id); }
}
