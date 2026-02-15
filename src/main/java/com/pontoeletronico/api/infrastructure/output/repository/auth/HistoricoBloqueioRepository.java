package com.pontoeletronico.api.infrastructure.output.repository.auth;

import com.pontoeletronico.api.domain.entity.auth.HistoricoBloqueio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.UUID;

public interface HistoricoBloqueioRepository extends JpaRepository<HistoricoBloqueio, UUID> {

    @Modifying
    @Query(value = """
            INSERT INTO historico_bloqueio (id, usuario_id, ativo, data_bloqueio, motivo_bloqueio, data_desativacao, created_at)
            VALUES (:id, :usuarioId, true, :dataBloqueio, :motivoBloqueio, NULL, :createdAt)
            """, nativeQuery = true)
    void insert(@Param("id") UUID id, @Param("usuarioId") UUID usuarioId,
                @Param("dataBloqueio") LocalDateTime dataBloqueio, @Param("motivoBloqueio") String motivoBloqueio,
                @Param("createdAt") LocalDateTime createdAt);

    @Modifying
    @Query(value = """
            UPDATE historico_bloqueio SET ativo = false, data_desativacao = :dataDesativacao
            WHERE usuario_id = :usuarioId AND ativo = true
            """, nativeQuery = true)
    int desativarPorUsuarioId(@Param("usuarioId") UUID usuarioId, @Param("dataDesativacao") LocalDateTime dataDesativacao);
}
