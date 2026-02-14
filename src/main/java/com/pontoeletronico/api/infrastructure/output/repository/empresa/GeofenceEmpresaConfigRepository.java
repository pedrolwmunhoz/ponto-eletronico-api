package com.pontoeletronico.api.infrastructure.output.repository.empresa;

import com.pontoeletronico.api.domain.entity.empresa.GeofenceEmpresaConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.UUID;

public interface GeofenceEmpresaConfigRepository extends JpaRepository<GeofenceEmpresaConfig, UUID> {

    @Modifying
    @Query(value = """
            INSERT INTO geofence_empresa_config (id, geofence_id, updated_at)
            VALUES (:id, :geofenceId, :updatedAt)
            """, nativeQuery = true)
    void insert(@Param("id") UUID id, @Param("geofenceId") UUID geofenceId, @Param("updatedAt") LocalDateTime updatedAt);
}
