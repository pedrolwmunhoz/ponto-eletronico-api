package com.pontoeletronico.api.infrastructure.output.repository.empresa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pontoeletronico.api.domain.entity.usuario.TipoUsuario;

public interface TipoUsuarioRepository extends JpaRepository<TipoUsuario, Integer> {

    @Query(value = "SELECT id FROM tipo_usuario WHERE descricao = :descricao AND ativo = true LIMIT 1", nativeQuery = true)
    Integer findIdByDescricao(@Param("descricao") String descricao);

    @Query(value = "SELECT descricao FROM tipo_usuario WHERE id = :id LIMIT 1", nativeQuery = true)
    String findDescricaoById(@Param("id") Integer id);
}
