package com.pontoeletronico.api.domain.entity.auth;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Data;
import java.util.Objects;
import java.util.UUID;

@Entity
@Data
@Table(name = "sessao_ativa")
public class SessaoAtiva {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "credencial_id", nullable = false)
    private UUID credencialId;

    @Column(name = "token", nullable = false, unique = true, length = 512)
    private String token;

    @Column(name = "dispositivo_id")
    private UUID dispositivoId;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;

    @Column(name = "data_expiracao", nullable = false)
    private LocalDateTime dataExpiracao;

    @Column(name = "data_desativacao")
    private LocalDateTime dataDesativacao;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SessaoAtiva that = (SessaoAtiva) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
