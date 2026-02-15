package com.pontoeletronico.api.infrastructure.output.repository.empresa;

import com.pontoeletronico.api.domain.entity.empresa.JornadaFuncionarioConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

public interface JornadaFuncionarioConfigRepository extends JpaRepository<JornadaFuncionarioConfig, UUID> {

    Optional<JornadaFuncionarioConfig> findByFuncionarioId(UUID funcionarioId);

    @Modifying
    @Query(value = """
            INSERT INTO jornada_funcionario_config (id, funcionario_id, tipo_escala_jornada_id, carga_horaria_diaria, carga_horaria_semanal, tolerancia_padrao, intervalo_padrao, entrada_padrao, saida_padrao, tempo_descanso_entre_jornada, grava_geo_obrigatoria, updated_at)
            VALUES (:id, :funcionarioId, :tipoEscalaJornadaId, :cargaHorariaDiaria, :cargaHorariaSemanal, :toleranciaPadrao, :intervaloPadrao, :entradaPadrao, :saidaPadrao, :tempoDescansoEntreJornada, :gravaGeoObrigatoria, :updatedAt)
            """, nativeQuery = true)
    void insert(@Param("id") UUID id, @Param("funcionarioId") UUID funcionarioId, @Param("tipoEscalaJornadaId") Integer tipoEscalaJornadaId,
                @Param("cargaHorariaDiaria") Duration cargaHorariaDiaria, @Param("cargaHorariaSemanal") Duration cargaHorariaSemanal,
                @Param("toleranciaPadrao") Duration toleranciaPadrao, @Param("intervaloPadrao") Duration intervaloPadrao,
                @Param("entradaPadrao") LocalTime entradaPadrao, @Param("saidaPadrao") LocalTime saidaPadrao,
                @Param("tempoDescansoEntreJornada") Duration tempoDescansoEntreJornada,
                @Param("gravaGeoObrigatoria") boolean gravaGeoObrigatoria, @Param("updatedAt") LocalDateTime updatedAt);

    @Modifying
    @Query(value = """
            UPDATE jornada_funcionario_config SET tipo_escala_jornada_id = :tipoEscalaJornadaId, carga_horaria_diaria = :cargaHorariaDiaria,
            carga_horaria_semanal = :cargaHorariaSemanal, tolerancia_padrao = :toleranciaPadrao, intervalo_padrao = :intervaloPadrao,
            entrada_padrao = :entradaPadrao, saida_padrao = :saidaPadrao, tempo_descanso_entre_jornada = :tempoDescansoEntreJornada, grava_geo_obrigatoria = :gravaGeoObrigatoria, updated_at = :updatedAt
            WHERE funcionario_id = :funcionarioId
            """, nativeQuery = true)
    int updateByFuncionarioId(@Param("funcionarioId") UUID funcionarioId, @Param("tipoEscalaJornadaId") Integer tipoEscalaJornadaId,
                              @Param("cargaHorariaDiaria") Duration cargaHorariaDiaria, @Param("cargaHorariaSemanal") Duration cargaHorariaSemanal,
                              @Param("toleranciaPadrao") Duration toleranciaPadrao, @Param("intervaloPadrao") Duration intervaloPadrao,
                              @Param("entradaPadrao") LocalTime entradaPadrao, @Param("saidaPadrao") LocalTime saidaPadrao,
                              @Param("tempoDescansoEntreJornada") Duration tempoDescansoEntreJornada,
                              @Param("gravaGeoObrigatoria") boolean gravaGeoObrigatoria, @Param("updatedAt") LocalDateTime updatedAt);
}
