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


    @Modifying
    @Query(value = """
            INSERT INTO xref_ponto_resumo (id, funcionario_id, registro_ponto_id, resumo_ponto_dia_id, created_at)
            VALUES (:id, :funcionarioId, :registroPontoId, :resumoPontoDiaId, :createdAt)
            """, nativeQuery = true)
    void insert(@Param("id") UUID id, @Param("funcionarioId") UUID funcionarioId, @Param("registroPontoId") UUID registroPontoId, @Param("resumoPontoDiaId") UUID resumoPontoDiaId, @Param("createdAt") LocalDateTime createdAt);


    List<XrefPontoResumo> findByResumoPontoDiaIdOrderByCreatedAtAsc(UUID resumoPontoDiaId);

    @Query(value = """
            SELECT res.* FROM resumo_ponto_dia res
            INNER JOIN xref_ponto_resumo xref ON res.id = xref.resumo_ponto_dia_id
            INNER JOIN registro_ponto rp ON rp.id = xref.registro_ponto_id
            WHERE rp.usuario_id = :funcionarioId
              AND xref.created_at BETWEEN :dataInicio AND :dataFim
            ORDER BY res.primeira_batida ASC LIMIT 1
            """, nativeQuery = true)
    Optional<ResumoPontoDia> findByFuncionarioIdAndDataBetweenAsc(@Param("funcionarioId") UUID funcionarioId, @Param("dataInicio") LocalDateTime dataInicio, @Param("dataFim") LocalDateTime dataFim);

    @Query(value = """
            SELECT res.* FROM resumo_ponto_dia res
            INNER JOIN xref_ponto_resumo xref ON res.id = xref.resumo_ponto_dia_id
            INNER JOIN registro_ponto rp ON rp.id = xref.registro_ponto_id
            WHERE rp.usuario_id = :funcionarioId
              AND xref.created_at BETWEEN :dataInicio AND :dataFim
            ORDER BY res.primeira_batida DESC LIMIT 1
            """, nativeQuery = true)
    Optional<ResumoPontoDia> findByFuncionarioIdAndDataBetweenDesc(@Param("funcionarioId") UUID funcionarioId, @Param("dataInicio") LocalDateTime dataInicio, @Param("dataFim") LocalDateTime dataFim);


    @Query(value = "SELECT EXISTS(SELECT 1 FROM xref_ponto_resumo WHERE registro_ponto_id = :registroPontoId)", nativeQuery = true)
    boolean existsByRegistroPontoId(@Param("registroPontoId") UUID registroPontoId);

    @Query(value = "SELECT r.* FROM registro_ponto r JOIN xref_ponto_resumo x ON r.id = x.registro_ponto_id WHERE x.resumo_ponto_dia_id = :resumoPontoDiaId ORDER BY r.created_at ASC", nativeQuery = true)
    List<RegistroPonto> listRegistroPontoByResumoPontoDiaIdOrderByCreatedAt(@Param("resumoPontoDiaId") UUID resumoPontoDiaId);

    @Query(value = "SELECT r.* FROM resumo_ponto_dia r INNER JOIN xref_ponto_resumo x ON r.id = x.resumo_ponto_dia_id WHERE x.registro_ponto_id = :registroPontoId", nativeQuery = true)
    Optional<ResumoPontoDia> findResumoPontoDiaByRegistroPontoId(@Param("registroPontoId") UUID registroPontoId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM XrefPontoResumo x WHERE x.registroPontoId = :registroPontoId")
    int deleteByRegistroPontoId(@Param("registroPontoId") UUID registroPontoId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM XrefPontoResumo x WHERE x.resumoPontoDiaId = :resumoPontoDiaId")
    int deleteByResumoPontoDiaId(@Param("resumoPontoDiaId") UUID resumoPontoDiaId);
}
