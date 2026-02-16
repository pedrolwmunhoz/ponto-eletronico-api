package com.pontoeletronico.api.infrastructure.output.repository.usuario;

import com.pontoeletronico.api.domain.entity.usuario.UsuarioTelefone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UsuarioTelefoneRepository extends JpaRepository<UsuarioTelefone, UUID> {

    @Modifying
    @Query(value = """
            INSERT INTO usuario_telefone (id, usuario_id, codigo_pais, ddd, numero)
            VALUES (:id, :usuarioId, :codigoPais, :ddd, :numero)
            """, nativeQuery = true)
    void insert(@Param("id") UUID id, @Param("usuarioId") UUID usuarioId, @Param("codigoPais") String codigoPais,
                @Param("ddd") String ddd, @Param("numero") String numero);

    @Query(value = "SELECT 1 FROM usuario_telefone WHERE id = :id AND usuario_id = :usuarioId LIMIT 1", nativeQuery = true)
    Optional<Integer> existsByIdAndUsuarioId(@Param("id") UUID id, @Param("usuarioId") UUID usuarioId);

    @Query(value = "SELECT 1 FROM usuario_telefone WHERE codigo_pais = :codigoPais AND ddd = :ddd AND numero = :numero LIMIT 1", nativeQuery = true)
    Optional<Integer> existsByCodigoPaisAndDddAndNumero(@Param("codigoPais") String codigoPais, @Param("ddd") String ddd, @Param("numero") String numero);

    @Modifying
    @Query(value = "DELETE FROM usuario_telefone WHERE id = :id AND usuario_id = :usuarioId", nativeQuery = true)
    int deleteByIdAndUsuarioId(@Param("id") UUID id, @Param("usuarioId") UUID usuarioId);

    @Modifying
    @Query(value = "DELETE FROM usuario_telefone WHERE usuario_id = :usuarioId", nativeQuery = true)
    int deleteAllByUsuarioId(@Param("usuarioId") UUID usuarioId);
}
