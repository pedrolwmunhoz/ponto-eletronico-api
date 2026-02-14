package com.pontoeletronico.api.infrastructure.output.repository.audit;

import com.pontoeletronico.api.domain.entity.audit.AuditoriaLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AuditoriaLogRepository extends JpaRepository<AuditoriaLog, UUID> {

    /** Listagem de auditoria da empresa: logs da própria empresa e dos seus funcionários. Doc id 48. */
    @Query(value = """
            SELECT
                a.acao AS "acao",
                a.descricao AS "descricao",
                a.created_at AS "data",
                u.username AS "nomeUsuario",
                a.sucesso AS "sucesso"
            FROM auditoria_log a
            INNER JOIN users u ON u.id = a.usuario_id
            WHERE (a.usuario_id = :empresaId OR a.usuario_id IN (SELECT funcionario_id FROM identificacao_funcionario WHERE empresa_id = :empresaId))
            ORDER BY a.created_at DESC
            LIMIT :limit OFFSET :offset
            """, nativeQuery = true)
    List<AuditoriaLogListagemProjection> findPageByEmpresaId(
            @Param("empresaId") UUID empresaId,
            @Param("limit") int limit,
            @Param("offset") int offset);

    @Query(value = """
            SELECT COUNT(*) FROM auditoria_log a
            WHERE (a.usuario_id = :empresaId OR a.usuario_id IN (SELECT funcionario_id FROM identificacao_funcionario WHERE empresa_id = :empresaId))
            """, nativeQuery = true)
    long countByEmpresaId(@Param("empresaId") UUID empresaId);

    /** Verifica se o log pertence à empresa (própria ou funcionário) para autorizar detalhe. */
    @Query(value = """
            SELECT 1 FROM auditoria_log a
            WHERE a.id = :logId
              AND (a.usuario_id = :empresaId OR a.usuario_id IN (SELECT funcionario_id FROM identificacao_funcionario WHERE empresa_id = :empresaId))
            LIMIT 1
            """, nativeQuery = true)
    Optional<Integer> existsByIdAndEmpresaId(@Param("logId") UUID logId, @Param("empresaId") UUID empresaId);

    @Modifying
    @Query(value = """
            INSERT INTO auditoria_log (id, usuario_id, acao, descricao, dados_antigos, dados_novos, dispositivo_id, ip_address, user_agent, sucesso, mensagem_erro, created_at)
            VALUES (:id, :usuarioId, :acao, :descricao, CAST(:dadosAntigos AS jsonb), CAST(:dadosNovos AS jsonb), :dispositivoId, NULL, NULL, :sucesso, :mensagemErro, :createdAt)
            """, nativeQuery = true)
    void insert(@Param("id") UUID id, @Param("usuarioId") UUID usuarioId, @Param("acao") String acao,
                @Param("descricao") String descricao, @Param("dadosAntigos") String dadosAntigos,
                @Param("dadosNovos") String dadosNovos, @Param("dispositivoId") UUID dispositivoId,
                @Param("sucesso") boolean sucesso, @Param("mensagemErro") String mensagemErro,
                @Param("createdAt") LocalDateTime createdAt);
}
