package com.pontoeletronico.api.domain.entity.empresa;

import com.pontoeletronico.api.config.DurationStringConverter;
import jakarta.persistence.*;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

import lombok.Data;

@Entity
@Data
@Table(name = "metricas_diaria_empresa", uniqueConstraints = @UniqueConstraint(columnNames = {"empresa_id", "data_ref"}))
public class MetricasDiariaEmpresa {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "empresa_id", nullable = false)
    private UUID empresaId;

    @Column(name = "data_ref", nullable = false)
    private LocalDate dataRef;

    @Column(name = "ano_ref", nullable = false)
    private Integer anoRef;

    @Column(name = "mes_ref", nullable = false)
    private Integer mesRef;

    @Column(name = "quantidade_funcionarios", nullable = false)
    private Integer quantidadeFuncionarios = 0;

    @Column(name = "solicitacoes_pendentes", nullable = false)
    private Integer solicitacoesPendentes = 0;

    @Convert(converter = DurationStringConverter.class)
    @Column(name = "total_do_dia", nullable = false, length = 20)
    private Duration totalDoDia = Duration.ZERO;

    @Column(name = "total_ponto_hoje", nullable = false)
    private Integer totalPontoHoje = 0;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MetricasDiariaEmpresa that = (MetricasDiariaEmpresa) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}
