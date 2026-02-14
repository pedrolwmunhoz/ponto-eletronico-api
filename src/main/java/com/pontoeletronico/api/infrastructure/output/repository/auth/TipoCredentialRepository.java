package com.pontoeletronico.api.infrastructure.output.repository.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pontoeletronico.api.domain.entity.auth.TipoCredential;

import java.util.Optional;

public interface TipoCredentialRepository extends JpaRepository<TipoCredential, Integer> {

    @Query(value = "SELECT id FROM tipo_credential WHERE descricao = :descricao AND ativo = true LIMIT 1", nativeQuery = true)
    Integer findIdByDescricao(@Param("descricao") String descricao);

    @Query(value = "SELECT * FROM tipo_credential WHERE descricao = :descricao AND ativo = true LIMIT 1", nativeQuery = true)
    Optional<TipoCredential> findByDescricaoAndAtivo(@Param("descricao") String descricao);
}
