package com.pontoeletronico.api.domain.entity.empresa;

import com.pontoeletronico.api.config.DurationStringConverter;
import jakarta.persistence.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import lombok.Data;

@Entity
@Data
@Table(name = "banco_horas_mensal", uniqueConstraints = @UniqueConstraint(columnNames = {"funcionario_id", "ano_ref", "mes_ref"}))
public class BancoHorasMensal {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "funcionario_id", nullable = false)
    private UUID funcionarioId;

    @Column(name = "empresa_id", nullable = false)
    private UUID empresaId;

    @Column(name = "mes_ref", nullable = false)
    private Integer mesRef;

    @Column(name = "ano_ref", nullable = false)
    private Integer anoRef;

    @Convert(converter = DurationStringConverter.class)
    @Column(name = "total_horas_esperadas", nullable = false, length = 20)
    private Duration totalHorasEsperadas = Duration.ZERO;

    @Convert(converter = DurationStringConverter.class)
    @Column(name = "total_horas_trabalhadas", nullable = false, length = 20)
    private Duration totalHorasTrabalhadas = Duration.ZERO;

    @Convert(converter = DurationStringConverter.class)
    @Column(name = "total_horas_trabalhadas_feriado", nullable = false, length = 20)
    private Duration totalHorasTrabalhadasFeriado = Duration.ZERO;

    @Column(name = "inconsistente", nullable = false)
    private Boolean inconsistente = false;

    @Column(name = "motivo_inconsistencia", length = 255)
    private String motivoInconsistencia;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;

    @Column(name = "data_desativacao")
    private LocalDateTime dataDesativacao;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BancoHorasMensal that = (BancoHorasMensal) o;
        return Objects.equals(id, that.id);
    }
    @Override
    public int hashCode() { return Objects.hash(id); }
}
