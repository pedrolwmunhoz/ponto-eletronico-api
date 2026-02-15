package com.pontoeletronico.api.domain.entity.auth;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Objects;
import java.util.UUID;

@Entity
@Data
@Table(name = "dispositivo")
public class Dispositivo {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "nome_dispositivo")
    private String nomeDispositivo;

    @Column(name = "sistema_operacional")
    private String sistemaOperacional;

    @Column(name = "versao_app")
    private String versaoApp;

    @Column(name = "modelo_dispositivo")
    private String modeloDispositivo;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Dispositivo that = (Dispositivo) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
