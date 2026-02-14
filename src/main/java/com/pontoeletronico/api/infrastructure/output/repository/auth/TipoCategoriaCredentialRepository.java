package com.pontoeletronico.api.infrastructure.output.repository.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TipoCategoriaCredentialRepository extends JpaRepository<com.pontoeletronico.api.domain.entity.auth.TipoCategoriaCredential, Integer> {

    @Query(value = "SELECT id FROM tipo_categoria_credential WHERE descricao = :descricao AND ativo = true LIMIT 1", nativeQuery = true)
    Integer findIdByDescricao(@Param("descricao") String descricao);
}
