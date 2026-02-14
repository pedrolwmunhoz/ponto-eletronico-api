package com.pontoeletronico.api.infrastructure.output.repository.auth;

import com.pontoeletronico.api.domain.entity.auth.TipoTokenRecuperacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TipoTokenRecuperacaoRepository extends JpaRepository<TipoTokenRecuperacao, Integer> {

    @Query(value = "SELECT id FROM tipo_token_recuperacao WHERE descricao = :descricao AND ativo = true LIMIT 1", nativeQuery = true)
    Integer findIdByDescricao(@Param("descricao") String descricao);
}
