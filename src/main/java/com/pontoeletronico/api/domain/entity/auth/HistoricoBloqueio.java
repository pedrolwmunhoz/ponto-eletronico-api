package com.pontoeletronico.api.domain.entity.auth;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import lombok.Data;

@Entity
@Data
@Table(name = "historico_bloqueio")
public class HistoricoBloqueio {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "usuario_id", nullable = false, unique = true)
    private UUID usuarioId;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;

    @Column(name = "data_bloqueio")
    private LocalDateTime dataBloqueio;

    @Column(name = "motivo_bloqueio")
    private String motivoBloqueio;

    @Column(name = "data_desativacao")
    private LocalDateTime dataDesativacao;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HistoricoBloqueio that = (HistoricoBloqueio) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
