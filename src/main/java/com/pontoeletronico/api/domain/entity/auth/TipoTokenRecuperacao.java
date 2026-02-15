package com.pontoeletronico.api.domain.entity.auth;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Objects;

@Entity
@Data
@Table(name = "tipo_token_recuperacao")
public class TipoTokenRecuperacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "descricao", nullable = false, unique = true, length = 100)
    private String descricao;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TipoTokenRecuperacao that = (TipoTokenRecuperacao) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
