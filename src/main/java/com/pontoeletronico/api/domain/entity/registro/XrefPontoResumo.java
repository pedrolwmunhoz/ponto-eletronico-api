package com.pontoeletronico.api.domain.entity.registro;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Objects;
import java.util.UUID;
import java.time.LocalDateTime;

/**
 * Vínculo entre registro_ponto e resumo_ponto_dia (jornada).
 */
@Entity
@Data
@Table(name = "xref_ponto_resumo", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"registro_ponto_id"}),
        @UniqueConstraint(columnNames = {"funcionario_id", "data_ref"})
})
public class XrefPontoResumo {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "funcionario_id", nullable = false)
    private UUID funcionarioId;

    @Column(name = "registro_ponto_id", nullable = false, unique = true)
    private UUID registroPontoId;

    @Column(name = "resumo_ponto_dia_id", nullable = false)
    private UUID resumoPontoDiaId;

    @Column(name = "data_ref", nullable = false)
    private LocalDateTime dataRef;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        XrefPontoResumo that = (XrefPontoResumo) o;
        return Objects.equals(id, that.id);
    }
    @Override
    public int hashCode() { return Objects.hash(id); }
}
