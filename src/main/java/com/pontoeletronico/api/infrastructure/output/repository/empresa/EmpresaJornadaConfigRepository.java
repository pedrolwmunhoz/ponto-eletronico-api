package com.pontoeletronico.api.infrastructure.output.repository.empresa;

import com.pontoeletronico.api.domain.entity.empresa.EmpresaJornadaConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

public interface EmpresaJornadaConfigRepository extends JpaRepository<EmpresaJornadaConfig, UUID> {

    Optional<EmpresaJornadaConfig> findByEmpresaId(UUID empresaId);

    @Query(value = "SELECT 1 FROM empresa_jornada_config WHERE empresa_id = :empresaId LIMIT 1", nativeQuery = true)
    Optional<Integer> existsByEmpresaId(@Param("empresaId") UUID empresaId);

    @Modifying
    @Query(value = """
            INSERT INTO empresa_jornada_config (id, empresa_id, tipo_escala_jornada_id, carga_horaria_diaria, carga_horaria_semanal,
            tolerancia_padrao, intervalo_padrao, entrada_padrao, saida_padrao, tempo_descanso_entre_jornada, timezone,
            grava_geo_obrigatoria, grava_ponto_apenas_em_geofence, permite_ajuste_ponto, updated_at)
            VALUES (:id, :empresaId, :tipoEscalaJornadaId, :cargaHorariaDiaria, :cargaHorariaSemanal,
            :toleranciaPadrao, :intervaloPadrao, :entradaPadrao, :saidaPadrao, :tempoDescansoEntreJornada, :timezone,
            :gravaGeoObrigatoria, :gravaPontoApenasEmGeofence, :permiteAjustePonto, :updatedAt)
            """, nativeQuery = true)
    void insert(@Param("id") UUID id, @Param("empresaId") UUID empresaId, @Param("tipoEscalaJornadaId") Integer tipoEscalaJornadaId,
                @Param("cargaHorariaDiaria") Duration cargaHorariaDiaria, @Param("cargaHorariaSemanal") Duration cargaHorariaSemanal,
                @Param("toleranciaPadrao") Duration toleranciaPadrao, @Param("intervaloPadrao") Duration intervaloPadrao,
                @Param("entradaPadrao") LocalTime entradaPadrao, @Param("saidaPadrao") LocalTime saidaPadrao,
                @Param("tempoDescansoEntreJornada") Duration tempoDescansoEntreJornada,
                @Param("timezone") String timezone, @Param("gravaGeoObrigatoria") boolean gravaGeoObrigatoria,
                @Param("gravaPontoApenasEmGeofence") boolean gravaPontoApenasEmGeofence, @Param("permiteAjustePonto") boolean permiteAjustePonto,
                @Param("updatedAt") LocalDateTime updatedAt);

    @Modifying
    @Query(value = """
            UPDATE empresa_jornada_config SET tipo_escala_jornada_id = :tipoEscalaJornadaId, carga_horaria_diaria = :cargaHorariaDiaria,
            carga_horaria_semanal = :cargaHorariaSemanal, tolerancia_padrao = :toleranciaPadrao,
            intervalo_padrao = :intervaloPadrao, entrada_padrao = :entradaPadrao, saida_padrao = :saidaPadrao,
            tempo_descanso_entre_jornada = :tempoDescansoEntreJornada, timezone = :timezone, grava_geo_obrigatoria = :gravaGeoObrigatoria, grava_ponto_apenas_em_geofence = :gravaPontoApenasEmGeofence,
            permite_ajuste_ponto = :permiteAjustePonto, updated_at = :updatedAt
            WHERE empresa_id = :empresaId
            """, nativeQuery = true)
    int updateByEmpresaId(@Param("empresaId") UUID empresaId, @Param("tipoEscalaJornadaId") Integer tipoEscalaJornadaId,
                          @Param("cargaHorariaDiaria") Duration cargaHorariaDiaria, @Param("cargaHorariaSemanal") Duration cargaHorariaSemanal,
                          @Param("toleranciaPadrao") Duration toleranciaPadrao, @Param("intervaloPadrao") Duration intervaloPadrao,
                          @Param("entradaPadrao") LocalTime entradaPadrao, @Param("saidaPadrao") LocalTime saidaPadrao,
                          @Param("tempoDescansoEntreJornada") Duration tempoDescansoEntreJornada,
                          @Param("timezone") String timezone, @Param("gravaGeoObrigatoria") boolean gravaGeoObrigatoria,
                          @Param("gravaPontoApenasEmGeofence") boolean gravaPontoApenasEmGeofence, @Param("permiteAjustePonto") boolean permiteAjustePonto,
                          @Param("updatedAt") LocalDateTime updatedAt);
}
