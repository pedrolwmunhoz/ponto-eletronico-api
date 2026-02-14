package com.pontoeletronico.api.domain.entity.usuario;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;
import java.util.UUID;

@Entity
@Data
@Table(name = "usuario_geofence")
public class UsuarioGeofence {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "descricao", nullable = false, length = 255)
    private String descricao;

    @Column(name = "latitude", nullable = false, precision = 12, scale = 8)
    private BigDecimal latitude;

    @Column(name = "longitude", nullable = false, precision = 12, scale = 8)
    private BigDecimal longitude;

    @Column(name = "raio_metros", nullable = false)
    private Integer raioMetros = 100;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

}
