package com.pontoeletronico.api.infrastructure.output.repository.empresa;

import com.pontoeletronico.api.domain.entity.empresa.Afastamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AfastamentoRepository extends JpaRepository<Afastamento, UUID> {


    @Query(value = """
            SELECT * FROM afastamento WHERE funcionario_id = :funcionarioId AND data_inicio <= :dataFim AND data_fim >= :dataInicio
            ORDER BY data_inicio ASC
            """, nativeQuery = true)
    List<Afastamento> findByFuncionarioIdAndDataBetween(UUID funcionarioId, LocalDateTime dataInicio, LocalDateTime dataFim);

    @Modifying
    @Query(value = """
            INSERT INTO afastamento (id, funcionario_id, tipo_afastamento_id, data_inicio, data_fim, observacao, ativo, created_at, updated_at)
            VALUES (:id, :funcionarioId, :tipoAfastamentoId, :dataInicio, :dataFim, :observacao, :ativo, :createdAt, :updatedAt)
            """, nativeQuery = true)
    void insert(@Param("id") UUID id, @Param("funcionarioId") UUID funcionarioId, @Param("tipoAfastamentoId") Integer tipoAfastamentoId,
                @Param("dataInicio") LocalDate dataInicio, @Param("dataFim") LocalDate dataFim, @Param("observacao") String observacao,
                @Param("ativo") boolean ativo, @Param("createdAt") LocalDateTime createdAt, @Param("updatedAt") LocalDateTime updatedAt);
}
