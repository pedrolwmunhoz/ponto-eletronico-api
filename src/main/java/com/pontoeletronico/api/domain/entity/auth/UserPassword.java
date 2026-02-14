package com.pontoeletronico.api.domain.entity.auth;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDateTime;
import lombok.Data;
import java.util.Objects;
import java.util.UUID;

@Entity
@Data
@Table(name = "user_password")
public class UserPassword {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "senha_hash", nullable = false, length = 255)
    private String senhaHash;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;

    @Column(name = "data_expiracao")
    private Instant dataExpiracao;

    @Column(name = "data_desativacao")
    private LocalDateTime dataDesativacao;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserPassword that = (UserPassword) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
