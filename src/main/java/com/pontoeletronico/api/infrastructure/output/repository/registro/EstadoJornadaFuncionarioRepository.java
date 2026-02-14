package com.pontoeletronico.api.infrastructure.output.repository.registro;

import com.pontoeletronico.api.domain.entity.registro.EstadoJornadaFuncionario;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface EstadoJornadaFuncionarioRepository extends JpaRepository<EstadoJornadaFuncionario, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM EstadoJornadaFuncionario e WHERE e.funcionarioId = :funcionarioId")
    Optional<EstadoJornadaFuncionario> findByFuncionarioIdForUpdate(@Param("funcionarioId") UUID funcionarioId);

    Optional<EstadoJornadaFuncionario> findByFuncionarioId(UUID funcionarioId);
}
