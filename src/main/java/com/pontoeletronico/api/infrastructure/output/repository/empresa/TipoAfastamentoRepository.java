package com.pontoeletronico.api.infrastructure.output.repository.empresa;

import com.pontoeletronico.api.domain.entity.empresa.TipoAfastamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TipoAfastamentoRepository extends JpaRepository<TipoAfastamento, Integer> {

    Optional<TipoAfastamento> findByIdAndAtivoTrue(Integer id);
}
