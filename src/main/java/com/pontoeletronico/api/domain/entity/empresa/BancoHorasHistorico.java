package com.pontoeletronico.api.domain.entity.empresa;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Data;
import java.util.Objects;
import java.util.UUID;

@Entity
@Data
@Table(name = "banco_horas_historico", uniqueConstraints = @UniqueConstraint(columnNames = {"funcionario_id", "ano_referencia", "mes_referencia"}))
public class BancoHorasHistorico {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "funcionario_id", nullable = false)
    private UUID funcionarioId;

    @Column(name = "ano_referencia", nullable = false)
    private Integer anoReferencia;

    @Column(name = "mes_referencia", nullable = false)
    private Integer mesReferencia;

    @Column(name = "total_horas_esperadas", nullable = false)
    private Integer totalHorasEsperadas = 0;

    @Column(name = "total_horas_trabalhadas", nullable = false)
    private Integer totalHorasTrabalhadas = 0;

    @Column(name = "total_banco_horas_final", nullable = false)
    private Integer totalBancoHorasFinal = 0;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "FECHADO";

    @Column(name = "valor_compensado_parcial", nullable = false)
    private Integer valorCompensadoParcial = 0;

    @Column(name = "tipo_status_pagamento_id", nullable = false)
    private Integer tipoStatusPagamentoId = 1;

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
        BancoHorasHistorico that = (BancoHorasHistorico) o;
        return Objects.equals(id, that.id);
    }
    @Override
    public int hashCode() { return Objects.hash(id); }
}
