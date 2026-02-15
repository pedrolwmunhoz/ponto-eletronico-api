package com.pontoeletronico.api.domain.entity.empresa;

import jakarta.persistence.*;
import java.util.Objects;
import java.util.UUID;

import lombok.Data;

@Entity
@Data
@Table(name = "funcionario_registro_lock")
public class FuncionarioRegistroLock {

    @Id
    @Column(name = "funcionario_id")
    private UUID funcionarioId;

    @Column(name = "empresa_id", nullable = false)
    private UUID empresaId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FuncionarioRegistroLock that = (FuncionarioRegistroLock) o;
        return Objects.equals(funcionarioId, that.funcionarioId);
    }
    @Override
    public int hashCode() { return Objects.hash(funcionarioId); }
}
