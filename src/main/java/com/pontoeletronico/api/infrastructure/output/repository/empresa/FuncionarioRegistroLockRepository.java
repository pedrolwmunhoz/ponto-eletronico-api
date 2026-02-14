package com.pontoeletronico.api.infrastructure.output.repository.empresa;

import com.pontoeletronico.api.domain.entity.empresa.FuncionarioRegistroLock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface FuncionarioRegistroLockRepository extends JpaRepository<FuncionarioRegistroLock, UUID> {

    /** Adquire lock pessimista na linha do funcionário para serializar registro de ponto. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT f FROM FuncionarioRegistroLock f WHERE f.funcionarioId = :funcionarioId")
    Optional<FuncionarioRegistroLock> findByFuncionarioIdForUpdate(@Param("funcionarioId") UUID funcionarioId);

    @Modifying
    @Query(value = """
            INSERT INTO funcionario_registro_lock (funcionario_id, empresa_id)
            VALUES (:funcionarioId, :empresaId)
            ON CONFLICT (funcionario_id) DO NOTHING
            """, nativeQuery = true)
    void insert(@Param("funcionarioId") UUID funcionarioId, @Param("empresaId") UUID empresaId);
}
