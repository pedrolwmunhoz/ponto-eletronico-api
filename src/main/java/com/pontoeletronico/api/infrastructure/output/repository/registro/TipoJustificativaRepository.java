package com.pontoeletronico.api.infrastructure.output.repository.registro;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pontoeletronico.api.domain.entity.registro.TipoJustificativa;

public interface TipoJustificativaRepository extends JpaRepository<TipoJustificativa, Integer> {

    @Query(value = "SELECT id FROM tipo_justificativa WHERE descricao = :descricao AND ativo = true LIMIT 1", nativeQuery = true)
    Integer findIdByDescricao(@Param("descricao") String descricao);
}
