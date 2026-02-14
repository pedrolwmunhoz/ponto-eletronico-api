package com.pontoeletronico.api.domain.entity.empresa;

import com.pontoeletronico.api.config.DurationStringConverter;
import jakarta.persistence.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;
import java.util.UUID;

import lombok.Data;

@Entity
@Data
@Table(name = "empresa_jornada_config")
public class EmpresaJornadaConfig {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "empresa_id", nullable = false, unique = true)
    private UUID empresaId;

    @Column(name = "tipo_escala_jornada_id", nullable = false)
    private Integer tipoEscalaJornadaId;

    @Convert(converter = DurationStringConverter.class)
    @Column(name = "carga_horaria_diaria", nullable = false, length = 20)
    private Duration cargaHorariaDiaria;

    @Convert(converter = DurationStringConverter.class)
    @Column(name = "carga_horaria_semanal", nullable = false, length = 20)
    private Duration cargaHorariaSemanal;

    @Convert(converter = DurationStringConverter.class)
    @Column(name = "tolerancia_padrao", nullable = false, length = 20)
    private Duration toleranciaPadrao = Duration.ZERO;

    @Convert(converter = DurationStringConverter.class)
    @Column(name = "intervalo_padrao", nullable = false, length = 20)
    private Duration intervaloPadrao;

    @Column(name = "entrada_padrao", nullable = false)
    private LocalTime entradaPadrao;

    @Column(name = "saida_padrao", nullable = false)
    private LocalTime saidaPadrao;

    @Convert(converter = DurationStringConverter.class)
    @Column(name = "tempo_descanso_entre_jornada", nullable = false, length = 20)
    private Duration tempoDescansoEntreJornada = Duration.ofHours(11);

    @Column(name = "timezone", nullable = false, length = 50)
    private String timezone = "America/Sao_Paulo";

    @Column(name = "grava_geo_obrigatoria", nullable = false)
    private Boolean gravaGeoObrigatoria = false;

    @Column(name = "grava_ponto_apenas_em_geofence", nullable = false)
    private Boolean gravaPontoApenasEmGeofence = false;

    @Column(name = "permite_ajuste_ponto", nullable = false)
    private Boolean permiteAjustePonto = false;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmpresaJornadaConfig that = (EmpresaJornadaConfig) o;
        return Objects.equals(id, that.id);
    }
    @Override
    public int hashCode() { return Objects.hash(id); }
}
