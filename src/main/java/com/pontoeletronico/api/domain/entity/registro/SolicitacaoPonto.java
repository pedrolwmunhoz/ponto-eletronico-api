package com.pontoeletronico.api.domain.entity.registro;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "solicitacao_ponto")
public class SolicitacaoPonto {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "idempotency_key", nullable = false)
    private UUID idempotencyKey;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "tipo_solicitacao_id", nullable = false)
    private Integer tipoSolicitacaoId;

    @Column(name = "data_hora_registro")
    private LocalDateTime dataHoraRegistro;

    @Column(name = "registro_ponto_id")
    private UUID registroPontoId;

    @Column(name = "tipo_justificativa_id", nullable = false)
    private Integer tipoJustificativaId;

    @Column(name = "aprovado")
    private Boolean aprovado;

    @Column(name = "empresa_aprovacao_id")
    private UUID empresaAprovacaoId;

    @Column(name = "observacao_aprovacao", columnDefinition = "TEXT")
    private String observacaoAprovacao;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "data_aprovacao")
    private LocalDateTime dataAprovacao;

    public static final int TIPO_CRIAR = 1;
    public static final int TIPO_REMOVER = 2;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SolicitacaoPonto that = (SolicitacaoPonto) o;
        return Objects.equals(id, that.id);
    }
    @Override
    public int hashCode() { return Objects.hash(id); }
}
