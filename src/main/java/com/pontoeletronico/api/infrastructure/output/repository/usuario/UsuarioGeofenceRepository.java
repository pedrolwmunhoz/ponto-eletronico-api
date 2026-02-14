package com.pontoeletronico.api.infrastructure.output.repository.usuario;

import com.pontoeletronico.api.domain.entity.usuario.UsuarioGeofence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UsuarioGeofenceRepository extends JpaRepository<UsuarioGeofence, UUID> {

    List<UsuarioGeofence> findByUsuarioIdOrderByCreatedAtDesc(UUID usuarioId);

    @Query(value = """
            SELECT * FROM usuario_geofence
            WHERE usuario_id = :usuarioId
            ORDER BY created_at DESC
            LIMIT :limit OFFSET :offset
            """, nativeQuery = true)
    List<UsuarioGeofence> findPageByUsuarioId(@Param("usuarioId") UUID usuarioId, @Param("limit") int limit, @Param("offset") int offset);

    @Query(value = "SELECT COUNT(*) FROM usuario_geofence WHERE usuario_id = :usuarioId", nativeQuery = true)
    long countByUsuarioId(@Param("usuarioId") UUID usuarioId);

    /** Verifica se o geofence pertence à empresa (usuario_id = empresaId). Apenas a empresa cadastra geofences. */
    boolean existsByIdAndUsuarioId(UUID id, UUID usuarioId);

    @Modifying
    @Query(value = """
            INSERT INTO usuario_geofence (id, usuario_id, descricao, latitude, longitude, raio_metros, ativo, created_at, updated_at)
            VALUES (:id, :usuarioId, :descricao, :latitude, :longitude, :raioMetros, :ativo, :createdAt, :updatedAt)
            """, nativeQuery = true)
    void insert(@Param("id") UUID id, @Param("usuarioId") UUID usuarioId, @Param("descricao") String descricao,
                @Param("latitude") BigDecimal latitude, @Param("longitude") BigDecimal longitude,
                @Param("raioMetros") Integer raioMetros, @Param("ativo") boolean ativo,
                @Param("createdAt") LocalDateTime createdAt, @Param("updatedAt") LocalDateTime updatedAt);

    @Query(value = """
            SELECT 1
            FROM usuario_geofence ug
            INNER JOIN xref_geofence_funcionarios x ON x.geofence_id = ug.id
            WHERE x.funcionario_id = :funcionarioId
              AND ug.ativo = true
              AND (6371000 * acos(LEAST(1.0, GREATEST(-1.0,
                  cos(radians(:lat)) * cos(radians(ug.latitude::double precision)) * cos(radians(ug.longitude::double precision) - radians(:lon))
                  + sin(radians(:lat)) * sin(radians(ug.latitude::double precision)))))) <= ug.raio_metros
            LIMIT 1
            """, nativeQuery = true)
    Optional<Integer> existsFuncionarioDentroDeGeofence(@Param("funcionarioId") UUID funcionarioId, @Param("lat") double lat, @Param("lon") double lon);

    @Modifying
    @Query(value = """
            UPDATE usuario_geofence
            SET descricao = :descricao,
                latitude = :latitude,
                longitude = :longitude,
                raio_metros = :raioMetros,
                ativo = :ativo,
                updated_at = :updatedAt
            WHERE id = (SELECT id FROM usuario_geofence WHERE usuario_id = :usuarioId LIMIT 1)
            """, nativeQuery = true)
    int updateGeofenceByUsuarioId(@Param("usuarioId") UUID usuarioId, @Param("descricao") String descricao,
                          @Param("latitude") BigDecimal latitude, @Param("longitude") BigDecimal longitude,
                          @Param("raioMetros") Integer raioMetros, @Param("ativo") boolean ativo,
                          @Param("updatedAt") LocalDateTime updatedAt);
}
