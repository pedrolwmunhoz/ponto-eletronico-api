package com.pontoeletronico.api.infrastructure.output.repository.empresa;

import com.pontoeletronico.api.domain.entity.empresa.ContratoFuncionario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface ContratoFuncionarioRepository extends JpaRepository<ContratoFuncionario, UUID> {

    Optional<ContratoFuncionario> findByFuncionarioId(UUID funcionarioId);

    @Query(value = "SELECT 1 FROM contrato_funcionario WHERE matricula = :matricula AND matricula IS NOT NULL LIMIT 1", nativeQuery = true)
    Optional<Integer> existsByMatricula(@Param("matricula") String matricula);

    @Query(value = "SELECT 1 FROM contrato_funcionario WHERE pis_pasep = :pisPasep AND pis_pasep IS NOT NULL LIMIT 1", nativeQuery = true)
    Optional<Integer> existsByPisPasep(@Param("pisPasep") String pisPasep);

    @Modifying
    @Query(value = """
            INSERT INTO contrato_funcionario (id, funcionario_id, matricula, pis_pasep, cargo, departamento, tipo_contrato_id, ativo, data_admissao, data_demissao, salario_mensal, salario_hora, updated_at)
            VALUES (:id, :funcionarioId, :matricula, :pisPasep, :cargo, :departamento, :tipoContratoId, :ativo, :dataAdmissao, :dataDemissao, :salarioMensal, :salarioHora, :updatedAt)
            """, nativeQuery = true)
    void insert(@Param("id") UUID id, @Param("funcionarioId") UUID funcionarioId, @Param("matricula") String matricula,
                @Param("pisPasep") String pisPasep, @Param("cargo") String cargo, @Param("departamento") String departamento,
                @Param("tipoContratoId") Integer tipoContratoId, @Param("ativo") boolean ativo,
                @Param("dataAdmissao") LocalDate dataAdmissao, @Param("dataDemissao") LocalDate dataDemissao,
                @Param("salarioMensal") BigDecimal salarioMensal, @Param("salarioHora") BigDecimal salarioHora,
                @Param("updatedAt") LocalDateTime updatedAt);

    @Modifying
    @Query(value = """
            UPDATE contrato_funcionario SET matricula = :matricula, pis_pasep = :pisPasep, cargo = :cargo, departamento = :departamento,
            tipo_contrato_id = :tipoContratoId, ativo = :ativo, data_admissao = :dataAdmissao, data_demissao = :dataDemissao,
            salario_mensal = :salarioMensal, salario_hora = :salarioHora, updated_at = :updatedAt
            WHERE funcionario_id = :funcionarioId
            """, nativeQuery = true)
    int updateByFuncionarioId(@Param("funcionarioId") UUID funcionarioId, @Param("matricula") String matricula, @Param("pisPasep") String pisPasep,
                              @Param("cargo") String cargo, @Param("departamento") String departamento, @Param("tipoContratoId") Integer tipoContratoId,
                              @Param("ativo") boolean ativo, @Param("dataAdmissao") LocalDate dataAdmissao, @Param("dataDemissao") LocalDate dataDemissao,
                              @Param("salarioMensal") BigDecimal salarioMensal, @Param("salarioHora") BigDecimal salarioHora,
                              @Param("updatedAt") LocalDateTime updatedAt);
}
