package com.pontoeletronico.api.domain.entity.empresa;

import jakarta.persistence.*;
import java.util.Objects;
import java.util.UUID;

import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "empresa_compliance")
public class EmpresaCompliance {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "empresa_id", nullable = false, unique = true)
    private UUID empresaId;

    @Column(name = "controle_ponto_obrigatorio", nullable = false)
    private Boolean controlePontoObrigatorio = true;

    @Column(name = "tipo_modelo_ponto_id", nullable = false)
    private Integer tipoModeloPontoId;

    @Column(name = "tempo_retencao_anos", nullable = false)
    private Integer tempoRetencaoAnos;

    @Column(name = "auditoria_ativa", nullable = false)
    private Boolean auditoriaAtiva = true;

    @Column(name = "assinatura_digital_obrigatoria", nullable = false)
    private Boolean assinaturaDigitalObrigatoria = true;

    @Basic(fetch = FetchType.EAGER)
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "certificado")
    private byte[] certificado;

    @Column(name = "certificado_hash", length = 64)
    private String certificadoHash;

    @Column(name = "certificado_senha_criptografada", columnDefinition = "TEXT")
    private String certificadoSenhaCriptografada;

    @Column(name = "data_expiracao_certificado")
    private LocalDateTime dataExpiracaoCertificado;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmpresaCompliance that = (EmpresaCompliance) o;
        return Objects.equals(id, that.id);
    }
    @Override
    public int hashCode() { return Objects.hash(id); }
}
