package com.pontoeletronico.api.infrastructure.output.repository.empresa;

import com.pontoeletronico.api.domain.entity.empresa.XrefGeofenceFuncionario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Funcionário não cadastra geofence; apenas associa-se aos IDs dos geofences já cadastrados pela empresa (tabela xref_geofence_funcionarios).
 */
@Repository
public interface XrefGeofenceFuncionariosRepository extends JpaRepository<XrefGeofenceFuncionario, UUID> {

    long countByGeofenceId(UUID geofenceId);

    @Modifying
    @Query(value = "DELETE FROM xref_geofence_funcionarios WHERE funcionario_id = :funcionarioId", nativeQuery = true)
    void deleteByFuncionarioId(@Param("funcionarioId") UUID funcionarioId);

    @Modifying
    @Query(value = """
            INSERT INTO xref_geofence_funcionarios (id, geofence_id, funcionario_id)
            VALUES (:id, :geofenceId, :funcionarioId)
            """, nativeQuery = true)
    void insert(@Param("id") UUID id, @Param("geofenceId") UUID geofenceId, @Param("funcionarioId") UUID funcionarioId);
}