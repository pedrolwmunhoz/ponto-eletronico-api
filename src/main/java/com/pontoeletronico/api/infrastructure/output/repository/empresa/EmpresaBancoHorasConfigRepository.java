package com.pontoeletronico.api.infrastructure.output.repository.empresa;

import com.pontoeletronico.api.domain.entity.empresa.EmpresaBancoHorasConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface EmpresaBancoHorasConfigRepository extends JpaRepository<EmpresaBancoHorasConfig, UUID> {

    Optional<EmpresaBancoHorasConfig> findByEmpresaId(UUID empresaId);

    @Modifying
    @Query(value = """
            INSERT INTO empresa_banco_horas_config (id, empresa_id, ativo, total_dias_vencimento, updated_at)
            VALUES (:id, :empresaId, :ativo, :totalDiasVencimento, :updatedAt)
            """, nativeQuery = true)
    void insert(@Param("id") UUID id, @Param("empresaId") UUID empresaId, @Param("ativo") boolean ativo,
                @Param("totalDiasVencimento") Integer totalDiasVencimento, @Param("updatedAt") LocalDateTime updatedAt);

    @Modifying
    @Query(value = """
            UPDATE empresa_banco_horas_config SET ativo = :ativo, total_dias_vencimento = :totalDiasVencimento, updated_at = :updatedAt
            WHERE empresa_id = :empresaId
            """, nativeQuery = true)
    int updateByEmpresaId(@Param("empresaId") UUID empresaId, @Param("ativo") boolean ativo,
                          @Param("totalDiasVencimento") Integer totalDiasVencimento, @Param("updatedAt") LocalDateTime updatedAt);
}
