package com.pontoeletronico.api.infrastructure.output.repository.empresa;

import com.pontoeletronico.api.domain.entity.empresa.TipoContrato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TipoContratoRepository extends JpaRepository<TipoContrato, Integer> {

    Optional<TipoContrato> findByDescricaoAndAtivoTrue(String descricao);

    @Query(value = "SELECT 1 FROM tipo_contrato WHERE id = :id AND ativo = true LIMIT 1", nativeQuery = true)
    Optional<Integer> existsByIdAndAtivo(@Param("id") Integer id);
}
