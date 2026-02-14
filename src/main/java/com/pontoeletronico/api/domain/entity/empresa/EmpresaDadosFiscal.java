package com.pontoeletronico.api.domain.entity.empresa;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Data;
import java.util.Objects;
import java.util.UUID;

@Entity
@Data
@Table(name = "empresa_dados_fiscal")
public class EmpresaDadosFiscal {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "empresa_id", nullable = false, unique = true)
    private UUID empresaId;

    @Column(name = "razao_social", nullable = false, length = 255)
    private String razaoSocial;

    @Column(name = "cnpj", nullable = false, unique = true, length = 18)
    private String cnpj;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmpresaDadosFiscal that = (EmpresaDadosFiscal) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
