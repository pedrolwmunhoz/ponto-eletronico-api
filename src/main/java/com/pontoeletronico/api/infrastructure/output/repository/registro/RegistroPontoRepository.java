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

    @Query(value = "SELECT * FROM registro_ponto WHERE idempotency_key = :idempotencyKey AND usuario_id = :usuarioId AND ativo = true LIMIT 1", nativeQuery = true)
    Optional<RegistroPonto> findByIdempotencyKeyAndUsuarioIdAndAtivoTrue(@Param("idempotencyKey") UUID idempotencyKey, @Param("usuarioId") UUID usuarioId);
     
    @Query(value = "SELECT r.* FROM registro_ponto r WHERE r.usuario_id = :usuarioId AND r.ativo = true AND r.created_at >= :inicio AND r.created_at < :fim ORDER BY r.created_at ASC", nativeQuery = true)
    List<RegistroPonto> findByUsuarioIdAndAtivoTrueAndCreatedAtBetweenOrderByCreatedAtAsc(UUID usuarioId, LocalDateTime inicio, LocalDateTime fim);

    @Query(value = "SELECT r.* FROM registro_ponto r WHERE r.usuario_id = :usuarioId AND r.ativo = true AND r.created_at >= :inicio AND r.created_at < :fim ORDER BY r.created_at DESC", nativeQuery = true)
    List<RegistroPonto> findByUsuarioIdAndAtivoTrueAndCreatedAtBetweenOrderByCreatedAtDesc(UUID usuarioId, LocalDateTime inicio, LocalDateTime fim);

    @Query(value = "SELECT r.* FROM registro_ponto r WHERE r.id IN :listId AND r.ativo = true ORDER BY r.created_at ASC", nativeQuery = true)
    List<RegistroPonto> findByListIdInAndAtivoTrueOrderByCreatedAtAsc(@Param("listId") List<UUID> listId);


    @Query(value = """
            SELECT * FROM registro_ponto
            WHERE usuario_id = :usuarioId AND ativo = true
              AND created_at >= :inicio AND created_at < :fim
              AND created_at < :createdAt
            ORDER BY created_at DESC LIMIT 1
            """, nativeQuery = true)
    Optional<RegistroPonto> findPreviousRegistro(@Param("usuarioId") UUID usuarioId, @Param("inicio") LocalDateTime inicio,
                                                  @Param("fim") LocalDateTime fim, @Param("createdAt") LocalDateTime createdAt);

    @Query(value = """
            SELECT * FROM registro_ponto
            WHERE usuario_id = :usuarioId AND ativo = true
              AND created_at >= :inicio AND created_at < :fim
              AND created_at > :createdAt
            ORDER BY created_at ASC LIMIT 1
            """, nativeQuery = true)
    Optional<RegistroPonto> findNextRegistro(@Param("usuarioId") UUID usuarioId, @Param("inicio") LocalDateTime inicio,
                                              @Param("fim") LocalDateTime fim, @Param("createdAt") LocalDateTime createdAt);

    @Query(value = """
            SELECT * FROM registro_ponto
            WHERE usuario_id = :usuarioId AND ativo = true
              AND created_at >= :inicio AND created_at < :fim
            ORDER BY created_at DESC LIMIT 1
            """, nativeQuery = true)
    Optional<RegistroPonto> findLastRegistroOfDay(@Param("usuarioId") UUID usuarioId, @Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    Optional<RegistroPonto> findByIdAndUsuarioIdAndAtivoTrue(UUID id, UUID usuarioId);

    @Query(value = "SELECT r FROM RegistroPonto r WHERE r.id IN :ids AND r.ativo = true ORDER BY r.createdAt ASC", nativeQuery = true)
    List<RegistroPonto> findByIdInAndAtivoTrueOrderByCreatedAtAsc(@Param("ids") Set<UUID> ids);

    @Query(value = """
            SELECT * FROM registro_ponto
            WHERE usuario_id = :usuarioId AND ativo = true AND created_at < :createdAt
            ORDER BY created_at DESC LIMIT 1
            """, nativeQuery = true)
    Optional<RegistroPonto> findRegistroAnterior(@Param("usuarioId") UUID usuarioId, @Param("createdAt") LocalDateTime createdAt);

    /** Registros ativos a partir de uma data (inclusive), ordenados cronologicamente (para reprocessamento). */
    @Query(value = """
            SELECT r.* FROM registro_ponto r
            WHERE r.usuario_id = :usuarioId AND r.ativo = true AND r.created_at >= :aPartirDe
            ORDER BY r.created_at ASC
            """, nativeQuery = true)
    List<RegistroPonto> findByUsuarioIdAndAtivoTrueAndCreatedAtGreaterThanEqualOrderByCreatedAtAsc(
            @Param("usuarioId") UUID usuarioId, @Param("aPartirDe") LocalDateTime aPartirDe);

    @Modifying
    @Query(value = """
            INSERT INTO registro_ponto (id, idempotency_key, usuario_id, dia_semana, dispositivo_id, tipo_marcacao_id, tipo_entrada, descricao, ativo, created_at)
            VALUES (:id, :idempotencyKey, :usuarioId, :diaSemana, :dispositivoId, :tipoMarcacaoId, true, :descricao, true, :createdAt)
            """, nativeQuery = true)
    void insert(@Param("id") UUID id, @Param("idempotencyKey") UUID idempotencyKey, @Param("usuarioId") UUID usuarioId,
                @Param("diaSemana") String diaSemana, @Param("dispositivoId") UUID dispositivoId, @Param("tipoMarcacaoId") Integer tipoMarcacaoId,
                @Param("descricao") String descricao, @Param("createdAt") LocalDateTime createdAt);

    @Modifying
    @Query(value = """
            UPDATE registro_ponto SET ativo = false
            WHERE id = :id AND usuario_id = :usuarioId AND ativo = true
            """, nativeQuery = true)
    int desativar(@Param("id") UUID id, @Param("usuarioId") UUID usuarioId);

    /** Inverte tipo_entrada de todos os registros posteriores ao informado (reordena após inserção manual). */
    @Modifying
    @Query(value = """
            UPDATE registro_ponto SET tipo_entrada = NOT tipo_entrada
            WHERE usuario_id = :usuarioId AND created_at > :createdAt AND ativo = true
            """, nativeQuery = true)
    void inverterTipoEntradaRegistrosPosteriores(@Param("usuarioId") UUID usuarioId, @Param("createdAt") LocalDateTime createdAt);
}
