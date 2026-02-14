package com.pontoeletronico.api.infrastructure.output.repository.usuario;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pontoeletronico.api.domain.entity.usuario.UsuarioTelefone;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface UsuarioTelefoneRepository extends JpaRepository<UsuarioTelefone, UUID> {

    @Modifying
    @Query(value = """
            INSERT INTO usuario_telefone (id, usuario_id, codigo_pais, ddd, numero, ativo, data_desativacao)
            VALUES (:id, :usuarioId, :codigoPais, :ddd, :numero, true, NULL)
            """, nativeQuery = true)
    void insert(@Param("id") UUID id, @Param("usuarioId") UUID usuarioId, @Param("codigoPais") String codigoPais,
                @Param("ddd") String ddd, @Param("numero") String numero);

    @Query(value = "SELECT 1 FROM usuario_telefone WHERE id = :id AND usuario_id = :usuarioId AND ativo = true AND data_desativacao IS NULL LIMIT 1", nativeQuery = true)
    Optional<Integer> existsByIdAndUsuarioIdAndAtivo(@Param("id") UUID id, @Param("usuarioId") UUID usuarioId);

    @Query(value = "SELECT 1 FROM usuario_telefone WHERE codigo_pais = :codigoPais AND ddd = :ddd AND numero = :numero AND ativo = true AND data_desativacao IS NULL LIMIT 1", nativeQuery = true)
    Optional<Integer> existsByCodigoPaisAndDddAndNumeroAndAtivo(@Param("codigoPais") String codigoPais, @Param("ddd") String ddd, @Param("numero") String numero);

    @Modifying
    @Query(value = """
            UPDATE usuario_telefone SET ativo = false, data_desativacao = :dataDesativacao
            WHERE id = :id AND usuario_id = :usuarioId
            """, nativeQuery = true)
    int desativar(@Param("id") UUID id, @Param("usuarioId") UUID usuarioId, @Param("dataDesativacao") LocalDateTime dataDesativacao);

    @Modifying
    @Query(value = """
            UPDATE usuario_telefone SET ativo = false, data_desativacao = :dataDesativacao
            WHERE usuario_id = :usuarioId AND ativo = true AND data_desativacao IS NULL
            """, nativeQuery = true)
    int desativarAllByUsuarioId(@Param("usuarioId") UUID usuarioId, @Param("dataDesativacao") LocalDateTime dataDesativacao);

    @Query(value = """
            SELECT *
            FROM usuario_telefone
            WHERE usuario_id = :usuarioId
              AND ativo = true
              AND data_desativacao IS NULL
            ORDER BY id
            LIMIT 1
            """, nativeQuery = true)
    Optional<UsuarioTelefone> findFirstByUsuarioId(@Param("usuarioId") UUID usuarioId);
}
