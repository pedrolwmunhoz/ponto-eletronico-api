package com.pontoeletronico.api.domain.entity.empresa;

import com.pontoeletronico.api.config.DurationStringConverter;
import jakarta.persistence.*;
import lombok.Data;
import java.time.Duration;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Table(name = "jornada_funcionario_config")
public class JornadaFuncionarioConfig {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "funcionario_id", nullable = false, unique = true)
    private UUID funcionarioId;

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

    @Column(name = "grava_geo_obrigatoria", nullable = false)
    private Boolean gravaGeoObrigatoria = false;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

}
