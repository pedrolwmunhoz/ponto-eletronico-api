package com.pontoeletronico.api.domain.entity.empresa;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDateTime;
import lombok.Data;
import java.util.Objects;
import java.util.UUID;

@Entity
@Data
@Table(name = "empresa_banco_horas_config")
public class EmpresaBancoHorasConfig {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "empresa_id", nullable = false, unique = true)
    private UUID empresaId;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo = false;

    @Column(name = "total_dias_vencimento", nullable = false)
    private Integer totalDiasVencimento;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmpresaBancoHorasConfig that = (EmpresaBancoHorasConfig) o;
        return Objects.equals(id, that.id);
    }
    @Override
    public int hashCode() { return Objects.hash(id); }
}
