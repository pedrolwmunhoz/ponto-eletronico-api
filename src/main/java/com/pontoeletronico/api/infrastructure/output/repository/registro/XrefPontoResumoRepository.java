package com.pontoeletronico.api.infrastructure.output.repository.registro;

import com.pontoeletronico.api.domain.entity.registro.RegistroPonto;
import com.pontoeletronico.api.domain.entity.registro.ResumoPontoDia;
import com.pontoeletronico.api.domain.entity.registro.XrefPontoResumo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface XrefPontoResumoRepository extends JpaRepository<XrefPontoResumo, UUID> {

    List<XrefPontoResumo> findByResumoPontoDiaIdOrderByCreatedAtAsc(UUID resumoPontoDiaId);

    @Query(value = """
            SELECT res.* FROM resumo_ponto_dia res INNER JOIN xref_ponto_resumo xref ON res.id = xref.resumo_ponto_dia_id
            WHERE xref.funcionario_id = :funcionarioId
              AND xref.created_at BETWEEN :dataInicio AND :dataFim
            ORDER BY res.primeira_batida ASC LIMIT 1
            """, nativeQuery = true)
    Optional<ResumoPontoDia> findByFuncionarioIdAndDataBetweenAsc(@Param("funcionarioId") UUID funcionarioId, @Param("dataInicio") LocalDateTime dataInicio, @Param("dataFim") LocalDateTime dataFim);
    
    @Query(value = """
            SELECT res.* FROM resumo_ponto_dia res INNER JOIN xref_ponto_resumo xref ON res.id = xref.resumo_ponto_dia_id
            WHERE xref.funcionario_id = :funcionarioId
              AND xref.created_at BETWEEN :dataInicio AND :dataFim
            ORDER BY res.primeira_batida DESC LIMIT 1
            """, nativeQuery = true)
    Optional<ResumoPontoDia> findByFuncionarioIdAndDataBetweenDesc(@Param("funcionarioId") UUID funcionarioId, @Param("dataInicio") LocalDateTime dataInicio, @Param("dataFim") LocalDateTime dataFim);


    @Query(value = "SELECT xref.* FROM xref_ponto_resumo xref WHERE xref.registro_ponto_id = :registroPontoId", nativeQuery = true)
    Optional<XrefPontoResumo> findByRegistroPontoId(@Param("registroPontoId") UUID registroPontoId);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM xref_ponto_resumo WHERE registro_ponto_id = :registroPontoId)", nativeQuery = true)
    boolean existsByRegistroPontoId(@Param("registroPontoId") UUID registroPontoId);

    @Query(value = "SELECT r.* FROM registro_ponto r JOIN xref_ponto_resumo x ON r.id = x.registro_ponto_id WHERE x.resumo_ponto_dia_id = :resumoPontoDiaId ORDER BY r.created_at ASC", nativeQuery = true)
    List<RegistroPonto> listRegistroPontoByResumoPontoDiaIdOrderByCreatedAt(@Param("resumoPontoDiaId") UUID resumoPontoDiaId, @Param("direction") String direction);

    @Query(value = "SELECT r.* FROM resumo_ponto_dia r INNER JOIN xref_ponto_resumo x ON r.id = x.resumo_ponto_dia_id WHERE x.registro_ponto_id = :registroPontoId", nativeQuery = true)
    Optional<ResumoPontoDia> findResumoPontoDiaByRegistroPontoId(@Param("registroPontoId") UUID registroPontoId);

    /** Jornada anterior = a que foi criada antes (created_at do resumo). A exata anterior = 1, por created_at. */
    @Query(value = """
            SELECT r.id FROM resumo_ponto_dia r
            WHERE r.funcionario_id = :funcionarioId
              AND r.created_at < (SELECT r2.created_at FROM resumo_ponto_dia r2 WHERE r2.id = :resumoPontoDiaId LIMIT 1)
            ORDER BY r.created_at DESC LIMIT 1
            """, nativeQuery = true)
    Optional<UUID> findJornadaAnteriorByResumoPontoDiaId(@Param("funcionarioId") UUID funcionarioId, @Param("resumoPontoDiaId") UUID resumoPontoDiaId);

    /** Jornada posterior = a que foi criada depois (created_at do resumo). A exata posterior = 1, por created_at. */
    @Query(value = """
            SELECT r.id FROM resumo_ponto_dia r
            WHERE r.funcionario_id = :funcionarioId
              AND r.created_at > (SELECT r2.created_at FROM resumo_ponto_dia r2 WHERE r2.id = :resumoPontoDiaId LIMIT 1)
            ORDER BY r.created_at ASC LIMIT 1
            """, nativeQuery = true)
    Optional<UUID> findJornadaPosteriorByResumoPontoDiaId(@Param("funcionarioId") UUID funcionarioId, @Param("resumoPontoDiaId") UUID resumoPontoDiaId);

    @Modifying
    @Query(value = "DELETE FROM xref_ponto_resumo x WHERE x.registro_ponto_id = :registroPontoId", nativeQuery = true)
    void deleteByRegistroPontoId(@Param("registroPontoId") UUID registroPontoId);

    @Modifying
    @Query(value = "DELETE FROM xref_ponto_resumo x WHERE x.resumo_ponto_dia_id = :resumoPontoDiaId", nativeQuery = true)
    void deleteByResumoPontoDiaId(@Param("resumoPontoDiaId") UUID resumoPontoDiaId);
}
