package com.pontoeletronico.api.infrastructure.output.repository.usuario;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pontoeletronico.api.domain.entity.usuario.Users;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UsersRepository extends JpaRepository<Users, UUID> {

    @Query(value = """
            SELECT
                u.id                               AS usuarioId,
                u.username                         AS username,
                tu.descricao                       AS tipo,

                emails.emails                      AS emails,
                tels.telefones                     AS telefones

            FROM users u

            INNER JOIN tipo_usuario tu
                    ON tu.id = u.tipo_usuario_id

            LEFT JOIN LATERAL (
                SELECT COALESCE(json_agg(uc.valor)::text, '[]') AS emails
                FROM user_credential uc
                JOIN tipo_credential tc ON tc.id = uc.tipo_credencial_id AND tc.descricao = 'EMAIL'
                WHERE uc.usuario_id = u.id
            ) emails ON true

            LEFT JOIN LATERAL (
                SELECT COALESCE(json_agg(json_build_object('codigoPais', ut.codigo_pais, 'ddd', ut.ddd, 'numero', ut.numero))::text, '[]') AS telefones
                FROM usuario_telefone ut
                WHERE ut.usuario_id = u.id
            ) tels ON true

            ORDER BY u.username
            LIMIT :limit OFFSET :offset
            """, nativeQuery = true)
    List<UsuarioListagemProjection> findAllUsuarioListagem(@Param("limit") int limit, @Param("offset") int offset);

    @Query(value = """
            SELECT COUNT(*)
            FROM users u
            INNER JOIN tipo_usuario tu ON tu.id = u.tipo_usuario_id
            """, nativeQuery = true)
    long countAllUsuarioListagem();

    @Query(value = "SELECT * FROM users WHERE id = :usuarioId LIMIT 1", nativeQuery = true)
    Optional<Users> findByIdQuery(@Param("usuarioId") UUID usuarioId);

    @Query(value = "SELECT 1 FROM users WHERE username = :username LIMIT 1", nativeQuery = true)
    Optional<Integer> existsByUsername(@Param("username") String username);

    @Query(value = "SELECT 1 FROM users WHERE username = :username AND id != :excludeId LIMIT 1", nativeQuery = true)
    Optional<Integer> existsByUsernameAndIdNot(@Param("username") String username, @Param("excludeId") UUID excludeId);

    @Modifying
    @Query(value = """
            INSERT INTO users (id, username, ativo, data_desativacao, tipo_usuario_id, created_at)
            VALUES (:id, :username, true, NULL, :tipoUsuarioId, :createdAt)
            """, nativeQuery = true)
    void insert(@Param("id") UUID id, @Param("username") String username, @Param("tipoUsuarioId") Integer tipoUsuarioId, @Param("createdAt") LocalDateTime createdAt);

    @Modifying
    @Query(value = "UPDATE users SET username = :username WHERE id = :usuarioId", nativeQuery = true)
    int updateUsername(@Param("usuarioId") UUID usuarioId, @Param("username") String username);

    @Modifying
    @Query(value = """
            UPDATE users SET ativo = false, data_desativacao = :dataDesativacao
            WHERE id = :usuarioId AND ativo = true
            """, nativeQuery = true)
    int desativarUsuario(@Param("usuarioId") UUID usuarioId, @Param("dataDesativacao") LocalDateTime dataDesativacao);
}
