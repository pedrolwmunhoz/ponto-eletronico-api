package com.pontoeletronico.api.domain.entity.auth;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Data
@Table(name = "user_credential")
public class UserCredential {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "tipo_credencial_id", nullable = false)
    private Integer tipoCredencialId;

    @Column(name = "categoria_credential_id", nullable = false)
    private Integer categoriaCredentialId;

    @Column(name = "valor", nullable = false, unique = true, length = 255)
    private String valor;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;

    @Column(name = "data_desativacao")
    private LocalDateTime dataDesativacao;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserCredential that = (UserCredential) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
