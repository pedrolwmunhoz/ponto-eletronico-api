package com.pontoeletronico.api.domain.entity.empresa;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Data;
import java.util.Objects;
import java.util.UUID;

@Entity
@Data
@Table(name = "empresa_endereco")
public class EmpresaEndereco {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "empresa_id", nullable = false, unique = true)
    private UUID empresaId;

    @Column(name = "rua", nullable = false, length = 255)
    private String rua;

    @Column(name = "numero", nullable = false, length = 20)
    private String numero;

    @Column(name = "complemento", length = 255)
    private String complemento;

    @Column(name = "bairro", nullable = false, length = 255)
    private String bairro;

    @Column(name = "cidade", nullable = false, length = 255)
    private String cidade;

    @Column(name = "uf", nullable = false, length = 2)
    private String uf;

    @Column(name = "cep", nullable = false, length = 8)
    private String cep;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmpresaEndereco that = (EmpresaEndereco) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
