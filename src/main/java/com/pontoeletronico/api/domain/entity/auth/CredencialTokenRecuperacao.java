package com.pontoeletronico.api.domain.entity.auth;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import lombok.Data;

@Entity
@Data
@Table(name = "credencial_token_recuperacao")
public class CredencialTokenRecuperacao {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "tipo_token_recuperacao_id", nullable = false)
    private Integer tipoTokenRecuperacaoId;

    @Column(name = "token", nullable = false, unique = true, length = 255)
    private String token;

    @Column(name = "data_expiracao", nullable = false)
    private LocalDateTime dataExpiracao;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;

    @Column(name = "data_desativacao")
    private LocalDateTime dataDesativacao;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CredencialTokenRecuperacao that = (CredencialTokenRecuperacao) o;
        return Objects.equals(id, that.id);
    }
    @Override
    public int hashCode() { return Objects.hash(id); }
}
