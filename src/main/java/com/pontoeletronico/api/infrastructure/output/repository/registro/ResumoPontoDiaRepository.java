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
import java.util.Optional;
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
          )
        FROM resumo_ponto_dia r
        WHERE r.funcionario_id = :funcionarioId
          AND r.primeira_batida::date BETWEEN :inicio AND :fim
        ORDER BY r.primeira_batida
        """, nativeQuery = true)
    List<Object[]> findPontoListagemRowsRaw(@Param("funcionarioId") UUID funcionarioId, @Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Modifying
    @Query(
        value = "INSERT INTO resumo_ponto_dia (id, funcionario_id, empresa_id, primeira_batida, ultima_batida, total_horas_trabalhadas, total_horas_esperadas, inconsistente, motivo_inconsistencia, created_at) " +
                "VALUES (:id, :funcionarioId, :empresaId, :primeiraBatida, :ultimaBatida, :totalHorasTrabalhadas, :totalHorasEsperadas, :inconsistente, :motivoInconsistencia, :createdAt)", 
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
        @Param("createdAt") LocalDateTime createdAt
    );
    @Query(value = "SELECT r.* FROM resumo_ponto_dia r WHERE r.funcionario_id = :funcionarioId AND CAST(r.primeira_batida AS date) = :data", nativeQuery = true)
    Optional<ResumoPontoDia> findByFuncionarioIdAndData(@Param("funcionarioId") UUID funcionarioId, @Param("data") LocalDate data);

    @Query(value = "SELECT r.* FROM resumo_ponto_dia r WHERE r.empresa_id = :empresaId AND CAST(r.primeira_batida AS date) BETWEEN :inicio AND :fim ORDER BY r.funcionario_id, r.primeira_batida", nativeQuery = true)
    List<ResumoPontoDia> findByEmpresaIdAndDataBetweenOrderByFuncionarioIdAscDataAsc(
            @Param("empresaId") UUID empresaId, @Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query(value = "SELECT r.* FROM resumo_ponto_dia r WHERE r.funcionario_id = :funcionarioId AND CAST(r.primeira_batida AS date) BETWEEN :inicio AND :fim ORDER BY r.primeira_batida ASC, r.created_at ASC", nativeQuery = true)
    List<ResumoPontoDia> findByFuncionarioIdAndDataBetweenOrderByPrimeiraBatidaAscCreatedAtAsc(
            @Param("funcionarioId") UUID funcionarioId, @Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Modifying
    @Query(value = "DELETE FROM resumo_ponto_dia r WHERE r.funcionario_id = :funcionarioId AND CAST(r.primeira_batida AS date) >= :data", nativeQuery = true)
    void deleteByFuncionarioIdAndDataGreaterThanEqual(@Param("funcionarioId") UUID funcionarioId, @Param("data") LocalDate data);
}
