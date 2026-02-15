package com.pontoeletronico.api.domain.entity.registro;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Data
@Table(name = "registro_ponto")
public class RegistroPonto {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "idempotency_key", nullable = false)
    private UUID idempotencyKey; 

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "dia_semana", nullable = false, length = 3)
    private String diaSemana;

    @Column(name = "dispositivo_id", nullable = false)
    private UUID dispositivoId;

    @Column(name = "tipo_marcacao_id", nullable = false)
    private Integer tipoMarcacaoId;

    @Column(name = "tipo_entrada", nullable = false)
    private Boolean tipoEntrada = true;

    @Column(name = "descricao", columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegistroPonto that = (RegistroPonto) o;
        return Objects.equals(id, that.id);
    }
    @Override
    public int hashCode() { return Objects.hash(id); }
}
