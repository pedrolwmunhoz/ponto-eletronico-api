package com.pontoeletronico.api.infrastructure.output.repository.empresa;

import com.pontoeletronico.api.domain.entity.empresa.EmpresaDadosFiscal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface EmpresaDadosFiscalRepository extends JpaRepository<EmpresaDadosFiscal, UUID> {

    @Query(value = "SELECT 1 FROM empresa_dados_fiscal WHERE cnpj = :cnpj LIMIT 1", nativeQuery = true)
    Optional<Integer> existsByCnpj(@Param("cnpj") String cnpj);

    @Query(value = "SELECT 1 FROM empresa_dados_fiscal WHERE empresa_id = :empresaId LIMIT 1", nativeQuery = true)
    Optional<Integer> existsByEmpresaId(@Param("empresaId") UUID empresaId);

    Optional<EmpresaDadosFiscal> findByEmpresaId(UUID empresaId);

    @Modifying
    @Query(value = """
            INSERT INTO empresa_dados_fiscal (id, empresa_id, razao_social, cnpj, updated_at)
            VALUES (:id, :empresaId, :razaoSocial, :cnpj, :updatedAt)
            """, nativeQuery = true)
    void insert(@Param("id") UUID id, @Param("empresaId") UUID empresaId, @Param("razaoSocial") String razaoSocial,
                @Param("cnpj") String cnpj, @Param("updatedAt") LocalDateTime updatedAt);
}
