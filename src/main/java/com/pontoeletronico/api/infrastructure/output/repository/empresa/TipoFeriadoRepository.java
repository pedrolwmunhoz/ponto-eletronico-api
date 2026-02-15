package com.pontoeletronico.api.infrastructure.output.repository.empresa;

import com.pontoeletronico.api.domain.entity.empresa.TipoFeriado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TipoFeriadoRepository extends JpaRepository<TipoFeriado, Integer> {

    Optional<TipoFeriado> findByIdAndAtivoTrue(Integer id);
}
