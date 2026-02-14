package com.pontoeletronico.api.domain.entity.audit;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Entity
@Data
@Table(name = "auditoria_log")
public class AuditoriaLog {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "acao", nullable = false, length = 50)
    private String acao;

    @Column(name = "descricao", columnDefinition = "TEXT")
    private String descricao;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "dados_antigos", columnDefinition = "jsonb")
    private Map<String, Object> dadosAntigos;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "dados_novos", columnDefinition = "jsonb")
    private Map<String, Object> dadosNovos;

    @Column(name = "dispositivo_id")
    private UUID dispositivoId;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "sucesso", nullable = false)
    private Boolean sucesso;

    @Column(name = "mensagem_erro", columnDefinition = "TEXT")
    private String mensagemErro;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuditoriaLog that = (AuditoriaLog) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
