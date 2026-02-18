package com.pontoeletronico.api.infrastructure.output.repository.empresa;

import com.pontoeletronico.api.domain.entity.empresa.TipoModeloPonto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TipoModeloPontoRepository extends JpaRepository<TipoModeloPonto, Integer> {

    Optional<TipoModeloPonto> findByDescricao(String descricao);
}
