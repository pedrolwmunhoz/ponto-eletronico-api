package com.pontoeletronico.api.domain.entity.empresa;

import jakarta.persistence.*;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "xref_geofence_funcionarios", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "geofence_id", "funcionario_id" })
})
public class XrefGeofenceFuncionario {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "geofence_id", nullable = false)
    private UUID geofenceId;

    @Column(name = "funcionario_id", nullable = false)
    private UUID funcionarioId;

    public XrefGeofenceFuncionario() {}

    public XrefGeofenceFuncionario(UUID id, UUID geofenceId, UUID funcionarioId) {
        this.id = id;
        this.geofenceId = geofenceId;
        this.funcionarioId = funcionarioId;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getGeofenceId() { return geofenceId; }
    public void setGeofenceId(UUID geofenceId) { this.geofenceId = geofenceId; }
    public UUID getFuncionarioId() { return funcionarioId; }
    public void setFuncionarioId(UUID funcionarioId) { this.funcionarioId = funcionarioId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        XrefGeofenceFuncionario that = (XrefGeofenceFuncionario) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
