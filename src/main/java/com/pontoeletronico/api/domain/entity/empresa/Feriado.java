package com.pontoeletronico.api.domain.entity.empresa;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import lombok.Data;

@Entity
@Data
@Table(name = "feriado")
public class Feriado {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "data", nullable = false)
    private LocalDate data;

    @Column(name = "descricao", nullable = false, length = 255)
    private String descricao;

    @Column(name = "tipo_feriado_id", nullable = false)
    private Integer tipoFeriadoId;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Feriado that = (Feriado) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
