package com.pontoeletronico.api.infrastructure.output.repository.bancohoras;

import com.pontoeletronico.api.domain.entity.empresa.BancoHorasMensal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BancoHorasMensalRepository extends JpaRepository<BancoHorasMensal, UUID> {

    Optional<BancoHorasMensal> findByFuncionarioIdAndAnoRefAndMesRef(UUID funcionarioId, int anoRef, int mesRef);
}
