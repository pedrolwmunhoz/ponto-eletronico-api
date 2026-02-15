package com.pontoeletronico.api.domain.entity.registro;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import lombok.Data;

@Entity
@Table(name = "estado_jornada_funcionario")
@Data
public class EstadoJornadaFuncionario {

    @Id
    @Column(name = "funcionario_id")
    private UUID funcionarioId;

    @Column(name = "empresa_id", nullable = false)
    private UUID empresaId;

    @Column(name = "ultima_batida", nullable = false)
    private LocalDateTime ultimaBatida;

    @Column(name = "tipo_ultima_batida", nullable = false, length = 7)
    private String tipoUltimaBatida; // ENTRADA | SAIDA

    @Column(name = "ultima_jornada_id")
    private UUID ultimaJornadaId;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static final String TIPO_ENTRADA = "ENTRADA";
    public static final String TIPO_SAIDA = "SAIDA";


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EstadoJornadaFuncionario that = (EstadoJornadaFuncionario) o;
        return Objects.equals(funcionarioId, that.funcionarioId);
    }
    @Override
    public int hashCode() { return Objects.hash(funcionarioId); }
}
