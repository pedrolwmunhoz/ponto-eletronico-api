package com.pontoeletronico.api.infrastructure.output.repository.empresa;

import com.pontoeletronico.api.domain.entity.empresa.Feriado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;

@Repository
public interface FeriadoRepository extends JpaRepository<Feriado, UUID> {

    @Query(value = "SELECT * FROM feriado WHERE data BETWEEN :dataInicio AND :dataFim AND empresa_id = :empresaId AND ativo = true", nativeQuery = true)
    List<Feriado> findByDataBetweenAndEmpresaIdAndAtivoTrue(@Param("dataInicio") LocalDateTime dataInicio, @Param("dataFim") LocalDateTime dataFim, @Param("empresaId") UUID empresaId);

    @Query(value = "SELECT * FROM feriado WHERE data BETWEEN :dataInicio AND :dataFim AND tipo_usuario_id = :tipoUsuarioId AND ativo = true", nativeQuery = true)
    List<Feriado> findByDataBetweenAndAtivoTrueAndTipoUsuarioId(@Param("dataInicio") LocalDateTime dataInicio, @Param("dataFim") LocalDateTime dataFim, @Param("tipoUsuarioId") Integer tipoUsuarioId);
}
