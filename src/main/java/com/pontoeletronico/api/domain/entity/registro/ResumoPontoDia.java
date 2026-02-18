package com.pontoeletronico.api.domain.entity.registro;

import com.pontoeletronico.api.config.DurationStringConverter;
import jakarta.persistence.*;
import java.time.Duration;
import java.time.LocalDateTime;
import lombok.Data;
import java.util.Objects;
import java.util.UUID;

@Entity
@Data
@Table(name = "resumo_ponto_dia")
public class ResumoPontoDia {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "funcionario_id", nullable = false)
    private UUID funcionarioId;

    @Column(name = "empresa_id", nullable = false)
    private UUID empresaId;

    @Column(name = "primeira_batida")
    private LocalDateTime primeiraBatida;

    @Column(name = "ultima_batida")
    private LocalDateTime ultimaBatida;

    @Convert(converter = DurationStringConverter.class)
    @Column(name = "total_horas_trabalhadas", nullable = false, length = 20)
    private Duration totalHorasTrabalhadas = Duration.ZERO;

    @Convert(converter = DurationStringConverter.class)
    @Column(name = "total_horas_trabalhadas_feriado", nullable = false, length = 20)
    private Duration totalHorasTrabalhadasFeriado = Duration.ZERO;

    @Convert(converter = DurationStringConverter.class)
    @Column(name = "total_horas_esperadas", nullable = false, length = 20)
    private Duration totalHorasEsperadas = Duration.ZERO;

    @Column(name = "quantidade_registros", nullable = false)
    private Integer quantidadeRegistros = 0;

    @Column(name = "inconsistente", nullable = false)
    private Boolean inconsistente = false;

    @Column(name = "motivo_inconsistencia", length = 50)
    private String motivoInconsistencia;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResumoPontoDia that = (ResumoPontoDia) o;
        return Objects.equals(id, that.id);
    }
    @Override
    public int hashCode() { return Objects.hash(id); }
}
