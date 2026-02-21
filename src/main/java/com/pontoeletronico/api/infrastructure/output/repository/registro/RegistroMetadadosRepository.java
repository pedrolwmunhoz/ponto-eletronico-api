package com.pontoeletronico.api.infrastructure.output.repository.registro;

import com.pontoeletronico.api.domain.entity.registro.RegistroMetadados;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RegistroMetadadosRepository extends JpaRepository<RegistroMetadados, UUID> {
}
