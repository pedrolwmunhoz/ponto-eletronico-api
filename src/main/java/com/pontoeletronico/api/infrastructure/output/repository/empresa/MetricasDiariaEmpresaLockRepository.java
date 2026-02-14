package com.pontoeletronico.api.infrastructure.output.repository.empresa;

import com.pontoeletronico.api.domain.entity.empresa.MetricasDiariaEmpresaLock;
import com.pontoeletronico.api.domain.entity.empresa.MetricasDiariaEmpresaLockId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.UUID;

public interface MetricasDiariaEmpresaLockRepository extends JpaRepository<MetricasDiariaEmpresaLock, MetricasDiariaEmpresaLockId> {

    /** Tenta adquirir o lock por (empresa_id, data_ref). Retorna 1 se inseriu, 0 se já existia (conflito). */
    @Modifying
    @Query(value = """
            INSERT INTO metricas_diaria_empresa_lock (empresa_id, data_ref)
            VALUES (:empresaId, :dataRef)
            ON CONFLICT (empresa_id, data_ref) DO NOTHING
            """, nativeQuery = true)
    int tryInsert(@Param("empresaId") UUID empresaId, @Param("dataRef") LocalDate dataRef);

    @Modifying
    @Query(value = "DELETE FROM metricas_diaria_empresa_lock l WHERE l.empresa_id = :empresaId AND l.data_ref = :dataRef", nativeQuery = true)
    void releaseLock(@Param("empresaId") UUID empresaId, @Param("dataRef") LocalDate dataRef);
}
