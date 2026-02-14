package com.pontoeletronico.api.infrastructure.output.repository.auth;

import com.pontoeletronico.api.domain.entity.auth.UserPassword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface UserPasswordRepository extends JpaRepository<UserPassword, UUID> {

    @Modifying
    @Query(value = """
            INSERT INTO user_password (id, usuario_id, senha_hash, ativo, data_expiracao, data_desativacao, created_at)
            VALUES (:id, :usuarioId, :senhaHash, true, NULL, NULL, :createdAt)
            """, nativeQuery = true)
    void insert(@Param("id") UUID id, @Param("usuarioId") UUID usuarioId, @Param("senhaHash") String senhaHash, @Param("createdAt") LocalDateTime createdAt);

    @Query(value = """
            SELECT *
            FROM user_password
            WHERE usuario_id = :usuarioId
              AND ativo = true
              AND data_desativacao IS NULL
            LIMIT 1
            """, nativeQuery = true)
    Optional<UserPassword> findByUsuarioIdAndAtivo(@Param("usuarioId") UUID usuarioId);

    @Modifying
    @Query(value = """
            UPDATE user_password SET ativo = false, data_desativacao = :dataDesativacao
            WHERE usuario_id = :usuarioId AND ativo = true AND data_desativacao IS NULL
            """, nativeQuery = true)
    int desativarByUsuarioId(@Param("usuarioId") UUID usuarioId, @Param("dataDesativacao") LocalDateTime dataDesativacao);
}
