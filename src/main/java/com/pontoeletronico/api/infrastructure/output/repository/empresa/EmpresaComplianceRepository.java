package com.pontoeletronico.api.infrastructure.output.repository.empresa;

import com.pontoeletronico.api.domain.entity.empresa.EmpresaCompliance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EmpresaComplianceRepository extends JpaRepository<EmpresaCompliance, UUID> {

    Optional<EmpresaCompliance> findByEmpresaId(UUID empresaId);
}
