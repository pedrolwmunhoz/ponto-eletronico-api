package com.pontoeletronico.api.infrastructure.output.repository.auth;

import com.pontoeletronico.api.domain.entity.auth.SessaoAtiva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface SessaoAtivaRepository extends JpaRepository<SessaoAtiva, UUID> {

    @Modifying
    @Query(value = """
            INSERT INTO sessao_ativa (id, usuario_id, credencial_id, token, dispositivo_id, ativo, data_expiracao, data_desativacao, created_at)
            VALUES (:id, :usuarioId, :credencialId, :token, :dispositivoId, :ativo, :dataExpiracao, :dataDesativacao, :createdAt)
            """, nativeQuery = true)
    void insert(@Param("id") UUID id, @Param("usuarioId") UUID usuarioId, @Param("credencialId") UUID credencialId,
                @Param("token") String token, @Param("dispositivoId") UUID dispositivoId, @Param("ativo") boolean ativo,
                @Param("dataExpiracao") LocalDateTime dataExpiracao, @Param("dataDesativacao") LocalDateTime dataDesativacao,
                @Param("createdAt") LocalDateTime createdAt);

    @Query(value = """
            SELECT id, usuario_id, credencial_id, token, dispositivo_id, ativo, data_expiracao, data_desativacao, created_at
            FROM sessao_ativa
            WHERE token = :token
              AND ativo = true
              AND data_desativacao IS NULL
              AND data_expiracao > :dataReferencia
            LIMIT 1
            """, nativeQuery = true)
    Optional<SessaoAtiva> findByTokenAndAtivoAndNaoExpirado(@Param("token") String token, @Param("dataReferencia") LocalDateTime dataReferencia);

    @Modifying
    @Query(value = """
            UPDATE sessao_ativa SET ativo = false, data_desativacao = :dataDesativacao WHERE usuario_id = :usuarioId
            """, nativeQuery = true)
    void desativarPorUsuarioId(@Param("usuarioId") UUID usuarioId, @Param("dataDesativacao") LocalDateTime dataDesativacao);

    @Modifying
    @Query(value = """
            UPDATE sessao_ativa SET ativo = false, data_desativacao = :dataDesativacao WHERE id = :id
            """, nativeQuery = true)
    void desativarPorId(@Param("id") UUID id, @Param("dataDesativacao") LocalDateTime dataDesativacao);
}
