package com.pontoeletronico.api.infrastructure.output.repository.registro;

import com.pontoeletronico.api.domain.entity.registro.ResumoPontoDia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ResumoPontoDiaRepository extends JpaRepository<ResumoPontoDia, UUID> {

    /** Query nativa: uma linha por jornada; colunas na ordem: jornada, data, diaSemana, status, totalHorasRaw, marcacoesJson. Retorno Object[] para evitar problema de mapeamento da projeção. */
    @Query(value = """
        SELECT
          'Jornada: ' || LPAD(ROW_NUMBER() OVER (ORDER BY r.primeira_batida)::text, 2, '0'),
          r.primeira_batida::date,
          NULL::text,
          CASE WHEN r.inconsistente THEN 'inconsistente' ELSE 'normal' END,
          COALESCE(r.total_horas_trabalhadas, 'PT0S'),
          (SELECT json_agg(json_build_object('registroId', rp.id, 'horario', rp.created_at, 'tipo', CASE WHEN rp.tipo_entrada THEN 'ENTRADA' ELSE 'SAIDA' END) ORDER BY rp.created_at)
           FROM registro_ponto rp
           INNER JOIN xref_ponto_resumo xref ON rp.id = xref.registro_ponto_id
           WHERE xref.resumo_ponto_dia_id = r.id
          ),
          COALESCE(r.quantidade_registros, 0)
        FROM resumo_ponto_dia r
        WHERE r.funcionario_id = :funcionarioId
          AND r.primeira_batida::date BETWEEN :inicio AND :fim
        ORDER BY r.primeira_batida
        """, nativeQuery = true)
    List<Object[]> findPontoListagemRowsRaw(@Param("funcionarioId") UUID funcionarioId, @Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Modifying
    @Query(
        value = "INSERT INTO resumo_ponto_dia (id, funcionario_id, empresa_id, primeira_batida, ultima_batida, total_horas_trabalhadas, total_horas_esperadas, quantidade_registros, inconsistente, motivo_inconsistencia, data_ref) " +
                "VALUES (:id, :funcionarioId, :empresaId, :primeiraBatida, :ultimaBatida, :totalHorasTrabalhadas, :totalHorasEsperadas, :quantidadeRegistros, :inconsistente, :motivoInconsistencia, :dataRef)",
        nativeQuery = true
    )
    void insert(
        @Param("id") UUID id,
        @Param("funcionarioId") UUID funcionarioId,
        @Param("empresaId") UUID empresaId,
        @Param("primeiraBatida") LocalDateTime primeiraBatida,
        @Param("ultimaBatida") LocalDateTime ultimaBatida,
        @Param("totalHorasTrabalhadas") Duration totalHorasTrabalhadas,
        @Param("totalHorasEsperadas") Duration totalHorasEsperadas,
        @Param("inconsistente") Boolean inconsistente,
        @Param("motivoInconsistencia") String motivoInconsistencia,
        @Param("quantidadeRegistros") Long quantidadeRegistros,
        @Param("dataRef") LocalDateTime dataRef
    );
    @Query(value = "SELECT r.* FROM resumo_ponto_dia r WHERE r.funcionario_id = :funcionarioId AND CAST(r.primeira_batida AS date) BETWEEN :inicio AND :fim ORDER BY r.primeira_batida ASC, r.data_ref ASC", nativeQuery = true)
    List<ResumoPontoDia> findByFuncionarioIdAndDataBetweenOrderByPrimeiraBatidaAscDataRefAsc(
            @Param("funcionarioId") UUID funcionarioId, @Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

}
