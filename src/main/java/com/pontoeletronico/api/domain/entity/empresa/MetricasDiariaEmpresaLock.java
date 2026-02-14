package com.pontoeletronico.api.domain.entity.empresa;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "metricas_diaria_empresa_lock")
@IdClass(MetricasDiariaEmpresaLockId.class)
public class MetricasDiariaEmpresaLock {

    @Id
    @Column(name = "empresa_id", nullable = false)
    private UUID empresaId;

    @Id
    @Column(name = "data_ref", nullable = false)
    private LocalDate dataRef;

    public UUID getEmpresaId() { return empresaId; }
    public void setEmpresaId(UUID empresaId) { this.empresaId = empresaId; }
    public LocalDate getDataRef() { return dataRef; }
    public void setDataRef(LocalDate dataRef) { this.dataRef = dataRef; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MetricasDiariaEmpresaLock that = (MetricasDiariaEmpresaLock) o;
        return Objects.equals(empresaId, that.empresaId) && Objects.equals(dataRef, that.dataRef);
    }
    @Override
    public int hashCode() { return Objects.hash(empresaId, dataRef); }
}
