package com.pontoeletronico.api.domain.entity.registro;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/** Metadados do registro de ponto (geo, assinatura) - app e público. */
@Entity
@Data
@Table(name = "registro_metadados")
public class RegistroMetadados {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "registro_id", nullable = false, unique = true)
    private UUID registroId;

    @Column(name = "geo_latitude")
    private Double geoLatitude;

    @Column(name = "geo_longitude")
    private Double geoLongitude;

    @Column(name = "assinatura_digital", columnDefinition = "TEXT")
    private String assinaturaDigital;

    @Column(name = "certificado_serial", length = 255)
    private String certificadoSerial;

    @Column(name = "timestamp_assinatura")
    private LocalDateTime timestampAssinatura;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegistroMetadados that = (RegistroMetadados) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
