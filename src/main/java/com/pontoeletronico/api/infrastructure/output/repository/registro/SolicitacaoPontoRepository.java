package com.pontoeletronico.api.infrastructure.output.repository.registro;

import com.pontoeletronico.api.domain.entity.registro.SolicitacaoPonto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface SolicitacaoPontoRepository extends JpaRepository<SolicitacaoPonto, UUID> {

    @Query(value = "SELECT * FROM solicitacao_ponto WHERE idempotency_key = :idempotencyKey AND usuario_id = :usuarioId LIMIT 1", nativeQuery = true)
    Optional<SolicitacaoPonto> findByIdempotencyKeyAndUsuarioId(@Param("idempotencyKey") UUID idempotencyKey, @Param("usuarioId") UUID usuarioId);


    Optional<SolicitacaoPonto> findById(UUID id);

    @Modifying
    @Query(value = """
            INSERT INTO solicitacao_ponto (id, idempotency_key, usuario_id, tipo_solicitacao_id, data_hora_registro, registro_ponto_id, tipo_justificativa_id, aprovado, created_at)
            VALUES (:id, :idempotencyKey, :usuarioId, :tipoSolicitacaoId, :dataHoraRegistro, :registroPontoId, :tipoJustificativaId, NULL, :createdAt)
            """, nativeQuery = true)
    void insert(@Param("id") UUID id, @Param("idempotencyKey") UUID idempotencyKey, @Param("usuarioId") UUID usuarioId, @Param("tipoSolicitacaoId") Integer tipoSolicitacaoId,
                @Param("dataHoraRegistro") LocalDateTime dataHoraRegistro, @Param("registroPontoId") UUID registroPontoId,
                @Param("tipoJustificativaId") Integer tipoJustificativaId, @Param("createdAt") LocalDateTime createdAt);

    @Modifying
    @Query(value = """
            UPDATE solicitacao_ponto SET aprovado = :aprovado, empresa_aprovacao_id = :empresaAprovacaoId, observacao_aprovacao = :observacao, data_aprovacao = :dataAprovacao
            WHERE id = :id AND aprovado IS NULL
            """, nativeQuery = true)
    int updateAprovacao(@Param("id") UUID id, @Param("aprovado") Boolean aprovado, @Param("empresaAprovacaoId") UUID empresaAprovacaoId,
                        @Param("observacao") String observacao, @Param("dataAprovacao") LocalDateTime dataAprovacao);
}
