package com.pontoeletronico.api.domain.entity.empresa;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public class MetricasDiariaEmpresaLockId implements Serializable {

    private UUID empresaId;
    private LocalDate dataRef;

    public MetricasDiariaEmpresaLockId() {}

    public MetricasDiariaEmpresaLockId(UUID empresaId, LocalDate dataRef) {
        this.empresaId = empresaId;
        this.dataRef = dataRef;
    }

    public UUID getEmpresaId() { return empresaId; }
    public void setEmpresaId(UUID empresaId) { this.empresaId = empresaId; }
    public LocalDate getDataRef() { return dataRef; }
    public void setDataRef(LocalDate dataRef) { this.dataRef = dataRef; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MetricasDiariaEmpresaLockId that = (MetricasDiariaEmpresaLockId) o;
        return Objects.equals(empresaId, that.empresaId) && Objects.equals(dataRef, that.dataRef);
    }
    @Override
    public int hashCode() { return Objects.hash(empresaId, dataRef); }
}
