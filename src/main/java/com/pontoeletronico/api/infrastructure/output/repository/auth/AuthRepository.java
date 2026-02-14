package com.pontoeletronico.api.infrastructure.output.repository.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pontoeletronico.api.domain.entity.usuario.Users;

import java.util.Optional;
import java.util.UUID;

public interface AuthRepository extends JpaRepository<Users, UUID> {

    @Query(value = """
            SELECT
                u.id                                                                   AS usuarioId,
                u.username                                                             AS username,
                up.senha_hash                                                          AS senhaHash,
                uc.id                                                                  AS credencialId,
                u.ativo                                                                AS ativo,
                CASE WHEN hb.id IS NOT NULL AND hb.ativo = true AND hb.data_bloqueio IS NOT NULL THEN true ELSE false END AS bloqueio,
                CASE WHEN up.data_expiracao IS NOT NULL AND up.data_expiracao <= CURRENT_TIMESTAMP THEN true ELSE false END AS senhaExpirada,
                tu.descricao                                                           AS tipoDescricao

            FROM user_credential uc

            INNER JOIN tipo_credential tc
                    ON tc.id = uc.tipo_credencial_id AND tc.descricao = :tipoCredencial AND tc.ativo = true

            INNER JOIN user_password up
                    ON up.usuario_id = uc.usuario_id AND up.ativo = true AND up.data_desativacao IS NULL

            INNER JOIN users u
                    ON u.id = uc.usuario_id

            INNER JOIN tipo_usuario tu
                    ON tu.id = u.tipo_usuario_id

            LEFT JOIN historico_bloqueio hb
                   ON hb.usuario_id = u.id AND hb.ativo = true AND hb.data_bloqueio IS NOT NULL

            WHERE uc.valor = :valor
              AND uc.ativo = true
              AND uc.data_desativacao IS NULL
            LIMIT 1
            """, nativeQuery = true)
    Optional<AuthLoginProjection> findCredencialParaLogin(@Param("valor") String valor, @Param("tipoCredencial") String tipoCredencial);
}
