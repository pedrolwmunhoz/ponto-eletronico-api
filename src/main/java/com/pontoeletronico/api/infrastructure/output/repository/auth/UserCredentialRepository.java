package com.pontoeletronico.api.infrastructure.output.repository.auth;

import com.pontoeletronico.api.domain.entity.auth.UserCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserCredentialRepository extends JpaRepository<UserCredential, UUID> {

    @Query(value = "SELECT 1 FROM user_credential WHERE valor = :valor AND tipo_credencial_id = :tipoCredencialId LIMIT 1", nativeQuery = true)
    Optional<Integer> existsByValorAndTipoCredencialId(@Param("valor") String valor, @Param("tipoCredencialId") Integer tipoCredencialId);

    @Modifying
    @Query(value = """
            INSERT INTO user_credential (id, usuario_id, tipo_credencial_id, categoria_credential_id, valor)
            VALUES (:id, :usuarioId, :tipoCredencialId, :categoriaCredentialId, :valor)
            """, nativeQuery = true)
    void insert(@Param("id") UUID id, @Param("usuarioId") UUID usuarioId, @Param("tipoCredencialId") Integer tipoCredencialId,
                @Param("categoriaCredentialId") Integer categoriaCredentialId, @Param("valor") String valor);

    @Query(value = """
            SELECT *
            FROM user_credential
            WHERE valor = :valor
              AND tipo_credencial_id = :tipoCredencialId
            LIMIT 1
            """, nativeQuery = true)
    Optional<UserCredential> findByValorAndTipoCredencialIdAndAtivo(@Param("valor") String valor, @Param("tipoCredencialId") Integer tipoCredencialId);

    @Query(value = """
            SELECT id
            FROM user_credential
            WHERE usuario_id = :usuarioId
              AND tipo_credencial_id = :tipoCredencialId
            LIMIT 1
            """, nativeQuery = true)
    Optional<UUID> findCredencialIdByUsuarioAndTipo(@Param("usuarioId") UUID usuarioId, @Param("tipoCredencialId") Integer tipoCredencialId);

    @Query(value = """
            SELECT id
            FROM user_credential
            WHERE usuario_id = :usuarioId
              AND tipo_credencial_id = :tipoCredencialId
              AND categoria_credential_id = :categoriaCredentialId
            LIMIT 1
            """, nativeQuery = true)
    Optional<UUID> findCredencialIdByUsuarioTipoCategoria(@Param("usuarioId") UUID usuarioId, @Param("tipoCredencialId") Integer tipoCredencialId, @Param("categoriaCredentialId") Integer categoriaCredentialId);

    @Query(value = """
            SELECT *
            FROM user_credential
            WHERE usuario_id = :usuarioId
              AND valor = :valor
              AND tipo_credencial_id = :tipoCredencialId
            LIMIT 1
            """, nativeQuery = true)
    Optional<UserCredential> findByUsuarioIdAndValorAndTipoCredencialId(@Param("usuarioId") UUID usuarioId, @Param("valor") String valor, @Param("tipoCredencialId") Integer tipoCredencialId);

    @Modifying
    @Query(value = "UPDATE user_credential SET valor = :valor WHERE id = :id AND usuario_id = :usuarioId", nativeQuery = true)
    int updateValor(@Param("id") UUID id, @Param("usuarioId") UUID usuarioId, @Param("valor") String valor);

    @Modifying
    @Query(value = "DELETE FROM user_credential WHERE id = :id AND usuario_id = :usuarioId", nativeQuery = true)
    int deleteByIdAndUsuarioId(@Param("id") UUID id, @Param("usuarioId") UUID usuarioId);
}
