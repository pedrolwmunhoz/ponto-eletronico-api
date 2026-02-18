package com.pontoeletronico.api.infrastructure.output.repository.empresa;

import com.pontoeletronico.api.domain.entity.empresa.MetricasDiariaEmpresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MetricasDiariaEmpresaRepository extends JpaRepository<MetricasDiariaEmpresa, UUID> {

    @Query(value = """
            SELECT * FROM metricas_diaria_empresa
            WHERE empresa_id = :empresaId
              AND data_ref = :dataRef
            """, nativeQuery = true)
    Optional<MetricasDiariaEmpresa> findByEmpresaIdAndDataRef(@Param("empresaId") UUID empresaId, @Param("dataRef") LocalDate dataRef);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "DELETE FROM metricas_diaria_empresa m WHERE m.id = :id", nativeQuery = true)
    void deleteMetricasDiariaEmpresaById(@Param("id") UUID id);
    
    Optional<MetricasDiariaEmpresa> findTopByEmpresaIdAndDataRefBeforeOrderByDataRefDesc(UUID empresaId, LocalDate dataRef);

    Optional<MetricasDiariaEmpresa> findTopByEmpresaIdOrderByDataRefDesc(UUID empresaId);

    @Query(value = """
            SELECT * FROM metricas_diaria_empresa
            WHERE empresa_id = :empresaId
              AND data_ref >= :dataInicio
              AND data_ref <= :dataFim
            ORDER BY data_ref ASC
            """, nativeQuery = true)
    List<MetricasDiariaEmpresa> findByEmpresaIdAndDataRefBetween(
            @Param("empresaId") UUID empresaId,
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim);
}
