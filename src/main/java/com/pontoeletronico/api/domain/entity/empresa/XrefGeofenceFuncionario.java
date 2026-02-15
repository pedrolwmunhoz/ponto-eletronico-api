package com.pontoeletronico.api.domain.entity.empresa;

import jakarta.persistence.*;
import java.util.Objects;
import java.util.UUID;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
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
