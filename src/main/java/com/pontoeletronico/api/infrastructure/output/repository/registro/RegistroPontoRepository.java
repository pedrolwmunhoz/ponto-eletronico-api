package com.pontoeletronico.api.infrastructure.output.repository.registro;

import com.pontoeletronico.api.domain.entity.registro.RegistroPonto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface RegistroPontoRepository extends JpaRepository<RegistroPonto, UUID> {


    @Query(value = "SELECT * FROM registro_ponto WHERE usuario_id = :usuarioId AND created_at = :createdAt LIMIT 1", nativeQuery = true)
    Optional<RegistroPonto> findByUsuarioIdAndCreatedAt(@Param("usuarioId") UUID usuarioId, @Param("createdAt") LocalDateTime createdAt);

    @Query(value = "SELECT * FROM registro_ponto WHERE idempotency_key = :idempotencyKey AND usuario_id = :usuarioId LIMIT 1", nativeQuery = true)
    Optional<RegistroPonto> findByIdempotencyKeyAndUsuarioId(@Param("idempotencyKey") UUID idempotencyKey, @Param("usuarioId") UUID usuarioId);

    @Query(value = "SELECT r.* FROM registro_ponto r WHERE r.usuario_id = :usuarioId AND r.created_at >= :inicio AND r.created_at < :fim ORDER BY r.created_at ASC", nativeQuery = true)
    List<RegistroPonto> findByUsuarioIdAndCreatedAtBetweenOrderByCreatedAtAsc(@Param("usuarioId") UUID usuarioId, @Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Query(value = "SELECT COUNT(*) FROM registro_ponto r WHERE r.usuario_id = :usuarioId AND r.created_at >= :inicio AND r.created_at < :fim", nativeQuery = true)
    long countByUsuarioIdAndCreatedAtBetween(@Param("usuarioId") UUID usuarioId, @Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Query(value = "SELECT r.* FROM registro_ponto r WHERE r.usuario_id = :usuarioId AND r.created_at >= :inicio AND r.created_at < :fim ORDER BY r.created_at DESC LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<RegistroPonto> findByUsuarioIdAndCreatedAtBetweenOrderByCreatedAtDescLimitOffset(@Param("usuarioId") UUID usuarioId, @Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim, @Param("limit") int limit, @Param("offset") int offset);

    @Query(value = "SELECT * FROM registro_ponto WHERE id = :id AND usuario_id = :usuarioId LIMIT 1", nativeQuery = true)
    Optional<RegistroPonto> findByIdAndUsuarioId(@Param("id") UUID id, @Param("usuarioId") UUID usuarioId);

    @Query(value = "SELECT * FROM registro_ponto r WHERE r.id IN :ids ORDER BY r.created_at ASC", nativeQuery = true)
    List<RegistroPonto> findByIdInOrderByCreatedAtAsc(@Param("ids") Set<UUID> ids);

    @Modifying
    @Query(value = """
            INSERT INTO registro_ponto (id, idempotency_key, usuario_id, dia_semana, dispositivo_id, tipo_marcacao_id, tipo_entrada, descricao, created_at)
            VALUES (:id, :idempotencyKey, :usuarioId, :diaSemana, :dispositivoId, :tipoMarcacaoId, true, :descricao, :createdAt)
            """, nativeQuery = true)
    void insert(@Param("id") UUID id, @Param("idempotencyKey") UUID idempotencyKey, @Param("usuarioId") UUID usuarioId,
                @Param("diaSemana") String diaSemana, @Param("dispositivoId") UUID dispositivoId, @Param("tipoMarcacaoId") Integer tipoMarcacaoId,
                @Param("descricao") String descricao, @Param("createdAt") LocalDateTime createdAt);

    @Modifying
    @Query(value = "DELETE FROM registro_ponto WHERE id = :id AND usuario_id = :usuarioId", nativeQuery = true)
    int deleteByIdAndUsuarioId(@Param("id") UUID id, @Param("usuarioId") UUID usuarioId);

}
