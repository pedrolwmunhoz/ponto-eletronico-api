package com.pontoeletronico.api.domain.entity.auth;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import lombok.Data;

@Entity
@Data
@Table(name = "historico_login")
public class HistoricoLogin {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "credencial_id", nullable = false)
    private UUID credencialId;

    @Column(name = "data_login", nullable = false)
    private LocalDateTime dataLogin;

    @Column(name = "dispositivo_id")
    private UUID dispositivoId;

    @Column(name = "sucesso", nullable = false)
    private Boolean sucesso;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HistoricoLogin that = (HistoricoLogin) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
