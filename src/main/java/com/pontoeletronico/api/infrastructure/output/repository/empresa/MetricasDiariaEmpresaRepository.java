package com.pontoeletronico.api.infrastructure.output.repository.empresa;

import com.pontoeletronico.api.domain.entity.empresa.MetricasDiariaEmpresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MetricasDiariaEmpresaRepository extends JpaRepository<MetricasDiariaEmpresa, UUID> {

    Optional<MetricasDiariaEmpresa> findByEmpresaIdAndDataRef(UUID empresaId, LocalDate dataRef);

    List<MetricasDiariaEmpresa> findByEmpresaIdAndAnoRefAndMesRef(UUID empresaId, Integer anoRef, Integer mesRef);

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
