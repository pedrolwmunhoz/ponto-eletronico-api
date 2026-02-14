package com.pontoeletronico.api.infrastructure.output.repository.auth;

import com.pontoeletronico.api.domain.entity.auth.HistoricoLogin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.UUID;

public interface HistoricoLoginRepository extends JpaRepository<HistoricoLogin, UUID> {

    @Query(value = """
            SELECT COUNT(*)
            FROM historico_login
            WHERE credencial_id = :credencialId
              AND sucesso = false
              AND data_login >= :desde
            """, nativeQuery = true)
    long countFalhasRecentes(@Param("credencialId") UUID credencialId, @Param("desde") LocalDateTime desde);

    @Modifying
    @Query(value = """
            INSERT INTO historico_login (id, usuario_id, credencial_id, data_login, dispositivo_id, sucesso, created_at)
            VALUES (:id, :usuarioId, :credencialId, :dataLogin, :dispositivoId, :sucesso, :createdAt)
            """, nativeQuery = true)
    void insert(@Param("id") UUID id, @Param("usuarioId") UUID usuarioId, @Param("credencialId") UUID credencialId,
                @Param("dataLogin") LocalDateTime dataLogin, @Param("dispositivoId") UUID dispositivoId,
                @Param("sucesso") boolean sucesso, @Param("createdAt") LocalDateTime createdAt);
}
