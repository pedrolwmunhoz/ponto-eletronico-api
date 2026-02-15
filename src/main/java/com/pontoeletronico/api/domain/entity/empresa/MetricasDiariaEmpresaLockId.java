package com.pontoeletronico.api.domain.entity.empresa;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MetricasDiariaEmpresaLockId implements Serializable {

    private UUID empresaId;
    private LocalDate dataRef;

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
