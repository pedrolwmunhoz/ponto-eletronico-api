package com.pontoeletronico.api.domain.entity.empresa;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Data;
import java.util.Objects;
import java.util.UUID;

@Entity
@Data
@Table(name = "geofence_empresa_config")
public class GeofenceEmpresaConfig {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "geofence_id", nullable = false, unique = true)
    private UUID geofenceId;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GeofenceEmpresaConfig that = (GeofenceEmpresaConfig) o;
        return Objects.equals(id, that.id);
    }
    @Override
    public int hashCode() { return Objects.hash(id); }
}
