package com.pontoeletronico.api.infrastructure.output.repository.bancohoras;

import com.pontoeletronico.api.domain.entity.empresa.BancoHorasHistorico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BancoHorasHistoricoRepository extends JpaRepository<BancoHorasHistorico, UUID> {

    List<BancoHorasHistorico> findByFuncionarioIdOrderByAnoReferenciaDescMesReferenciaDesc(UUID funcionarioId);

    @Query(value = """
            SELECT * FROM banco_horas_historico
            WHERE funcionario_id = :funcionarioId
            ORDER BY ano_referencia DESC, mes_referencia DESC
            LIMIT :limit OFFSET :offset
            """, nativeQuery = true)
    List<BancoHorasHistorico> findPageByFuncionarioId(@Param("funcionarioId") UUID funcionarioId, @Param("limit") int limit, @Param("offset") int offset);

    @Query(value = "SELECT COUNT(*) FROM banco_horas_historico WHERE funcionario_id = :funcionarioId", nativeQuery = true)
    long countByFuncionarioId(@Param("funcionarioId") UUID funcionarioId);

    Optional<BancoHorasHistorico> findByFuncionarioIdAndAnoReferenciaAndMesReferencia(UUID funcionarioId, int ano, int mes);

    List<BancoHorasHistorico> findByAnoReferenciaAndMesReferenciaAndFuncionarioIdIn(int ano, int mes, java.util.Collection<UUID> funcionarioIds);
}
