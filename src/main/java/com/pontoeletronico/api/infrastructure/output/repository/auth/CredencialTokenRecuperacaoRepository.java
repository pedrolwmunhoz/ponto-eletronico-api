package com.pontoeletronico.api.infrastructure.output.repository.auth;

import com.pontoeletronico.api.domain.entity.auth.CredencialTokenRecuperacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface CredencialTokenRecuperacaoRepository extends JpaRepository<CredencialTokenRecuperacao, UUID> {

    @Modifying
    @Query(value = """
            INSERT INTO credencial_token_recuperacao (id, usuario_id, tipo_token_recuperacao_id, token, data_expiracao, ativo, data_desativacao, created_at)
            VALUES (:id, :usuarioId, :tipoTokenRecuperacaoId, :token, :dataExpiracao, true, NULL, :createdAt)
            """, nativeQuery = true)
    void insert(@Param("id") UUID id, @Param("usuarioId") UUID usuarioId, @Param("tipoTokenRecuperacaoId") Integer tipoTokenRecuperacaoId,
                @Param("token") String token, @Param("dataExpiracao") LocalDateTime dataExpiracao, @Param("createdAt") LocalDateTime createdAt);

    @Query(value = """
            SELECT id, usuario_id, tipo_token_recuperacao_id, token, data_expiracao, ativo, data_desativacao, created_at
            FROM credencial_token_recuperacao
            WHERE token = :token
              AND tipo_token_recuperacao_id = :tipoTokenRecuperacaoId
              AND ativo = true
              AND data_desativacao IS NULL
              AND data_expiracao > :dataReferencia
            LIMIT 1
            """, nativeQuery = true)
    Optional<CredencialTokenRecuperacao> findByTokenAndTipoAndAtivoAndNaoExpirado(
            @Param("token") String token, @Param("tipoTokenRecuperacaoId") Integer tipoTokenRecuperacaoId, @Param("dataReferencia") LocalDateTime dataReferencia);

    @Modifying
    @Query(value = """
            UPDATE credencial_token_recuperacao SET ativo = false, data_desativacao = :dataDesativacao WHERE id = :id
            """, nativeQuery = true)
    void desativar(@Param("id") UUID id, @Param("dataDesativacao") LocalDateTime dataDesativacao);
}
