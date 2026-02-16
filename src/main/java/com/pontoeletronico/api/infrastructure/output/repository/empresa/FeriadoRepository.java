package com.pontoeletronico.api.infrastructure.output.repository.empresa;

import com.pontoeletronico.api.domain.entity.empresa.Feriado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FeriadoRepository extends JpaRepository<Feriado, UUID> {

    /** Feriados que se aplicam à empresa: criados pela empresa OU criados por Admin (join users). Filtro opcional por descricao (observacao). */
    @Query(value = """
            SELECT f.id AS "id", f.data AS "data", f.descricao AS "descricao", f.tipo_feriado_id AS "tipoFeriadoId",
                   tf.descricao AS "tipoFeriadoDescricao", f.ativo AS "ativo", f.created_at AS "createdAt"
            FROM feriado f
            INNER JOIN tipo_feriado tf ON tf.id = f.tipo_feriado_id
            INNER JOIN users u ON f.usuario_id = u.id
            WHERE f.ativo = true AND (f.usuario_id = :empresaId OR u.tipo_usuario_id = (SELECT id FROM tipo_usuario WHERE descricao = 'ADMIN' AND ativo = true LIMIT 1))
              AND unaccent(LOWER(f.descricao)) LIKE unaccent(LOWER(:observacaoPattern))
            ORDER BY f.data ASC
            LIMIT :limit OFFSET :offset
            """, nativeQuery = true)
    List<FeriadoListagemProjection> findPageForEmpresa(@Param("empresaId") UUID empresaId, @Param("observacaoPattern") String observacaoPattern, @Param("limit") int limit, @Param("offset") int offset);

    @Query(value = """
            SELECT COUNT(*) FROM feriado f
            INNER JOIN users u ON f.usuario_id = u.id
            WHERE f.ativo = true AND (f.usuario_id = :empresaId OR u.tipo_usuario_id = (SELECT id FROM tipo_usuario WHERE descricao = 'ADMIN' AND ativo = true LIMIT 1))
              AND unaccent(LOWER(f.descricao)) LIKE unaccent(LOWER(:observacaoPattern))
            """, nativeQuery = true)
    long countForEmpresa(@Param("empresaId") UUID empresaId, @Param("observacaoPattern") String observacaoPattern);

    @Query(value = "SELECT * FROM feriado WHERE id = :id AND usuario_id = :usuarioId AND ativo = true LIMIT 1", nativeQuery = true)
    Optional<Feriado> findByIdAndUsuarioIdAndAtivoTrue(@Param("id") UUID id, @Param("usuarioId") UUID usuarioId);

    @Modifying
    @Query(value = """
            INSERT INTO feriado (id, data, descricao, tipo_feriado_id, usuario_id, ativo, created_at)
            VALUES (:id, :data, :descricao, :tipoFeriadoId, :usuarioId, :ativo, :createdAt)
            """, nativeQuery = true)
    void insert(@Param("id") UUID id, @Param("data") LocalDate data, @Param("descricao") String descricao,
                @Param("tipoFeriadoId") Integer tipoFeriadoId, @Param("usuarioId") UUID usuarioId,
                @Param("ativo") boolean ativo, @Param("createdAt") LocalDateTime createdAt);

    @Modifying
    @Query(value = """
            UPDATE feriado SET data = :data, descricao = :descricao, tipo_feriado_id = :tipoFeriadoId, ativo = :ativo
            WHERE id = :id AND usuario_id = :usuarioId
            """, nativeQuery = true)
    int update(@Param("id") UUID id, @Param("usuarioId") UUID usuarioId, @Param("data") LocalDate data,
               @Param("descricao") String descricao, @Param("tipoFeriadoId") Integer tipoFeriadoId, @Param("ativo") boolean ativo);

    @Modifying
    @Query(value = "UPDATE feriado SET ativo = false WHERE id = :id AND usuario_id = :usuarioId", nativeQuery = true)
    int desativar(@Param("id") UUID id, @Param("usuarioId") UUID usuarioId);

    /** Admin abrangência: feriados criados por Admin (join users). */
    @Query(value = """
            SELECT f.id AS "id", f.data AS "data", f.descricao AS "descricao", f.tipo_feriado_id AS "tipoFeriadoId",
                   tf.descricao AS "tipoFeriadoDescricao", f.ativo AS "ativo", f.created_at AS "createdAt"
            FROM feriado f
            INNER JOIN tipo_feriado tf ON tf.id = f.tipo_feriado_id
            INNER JOIN users u ON f.usuario_id = u.id
            WHERE f.ativo = true AND u.tipo_usuario_id = (SELECT id FROM tipo_usuario WHERE descricao = 'ADMIN' AND ativo = true LIMIT 1)
            ORDER BY f.data ASC
            LIMIT :limit OFFSET :offset
            """, nativeQuery = true)
    List<FeriadoListagemProjection> findPageByUsuarioIdIsAdmin(@Param("limit") int limit, @Param("offset") int offset);

    @Query(value = """
            SELECT COUNT(*) FROM feriado f
            INNER JOIN users u ON f.usuario_id = u.id
            WHERE f.ativo = true AND u.tipo_usuario_id = (SELECT id FROM tipo_usuario WHERE descricao = 'ADMIN' AND ativo = true LIMIT 1)
            """, nativeQuery = true)
    long countByUsuarioIdIsAdmin();

    /** Admin: listar feriados por empresa (usuario_id = empresaId). */
    @Query(value = """
            SELECT f.id AS "id", f.data AS "data", f.descricao AS "descricao", f.tipo_feriado_id AS "tipoFeriadoId",
                   tf.descricao AS "tipoFeriadoDescricao", f.ativo AS "ativo", f.created_at AS "createdAt"
            FROM feriado f
            INNER JOIN tipo_feriado tf ON tf.id = f.tipo_feriado_id
            WHERE f.usuario_id = :usuarioId AND f.ativo = true
            ORDER BY f.data ASC
            LIMIT :limit OFFSET :offset
            """, nativeQuery = true)
    List<FeriadoListagemProjection> findPageByUsuarioId(@Param("usuarioId") UUID usuarioId, @Param("limit") int limit, @Param("offset") int offset);

    @Query(value = "SELECT COUNT(*) FROM feriado WHERE usuario_id = :usuarioId AND ativo = true", nativeQuery = true)
    long countByUsuarioId(@Param("usuarioId") UUID usuarioId);

    /** CalcularResumoDiaUtils: feriados que se aplicam à empresa no período. */
    @Query(value = """
            SELECT f.* FROM feriado f
            INNER JOIN users u ON f.usuario_id = u.id
            WHERE f.data BETWEEN CAST(:dataInicio AS DATE) AND CAST(:dataFim AS DATE)
              AND f.ativo = true
              AND (f.usuario_id = :empresaId OR u.tipo_usuario_id = (SELECT id FROM tipo_usuario WHERE descricao = 'ADMIN' AND ativo = true LIMIT 1))
            """, nativeQuery = true)
    List<Feriado> findByDataBetweenAndAtivoTrueForEmpresa(
            @Param("dataInicio") LocalDateTime dataInicio, @Param("dataFim") LocalDateTime dataFim,
            @Param("empresaId") UUID empresaId);
}
