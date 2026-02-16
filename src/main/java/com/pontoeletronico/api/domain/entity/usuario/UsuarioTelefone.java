package com.pontoeletronico.api.domain.entity.usuario;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Objects;
import java.util.UUID;

@Entity
@Data
@Table(name = "usuario_telefone")
public class UsuarioTelefone {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "codigo_pais", nullable = false, length = 10)
    private String codigoPais;

    @Column(name = "ddd", nullable = false, length = 5)
    private String ddd;

    @Column(name = "numero", nullable = false, length = 20)
    private String numero;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UsuarioTelefone that = (UsuarioTelefone) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
