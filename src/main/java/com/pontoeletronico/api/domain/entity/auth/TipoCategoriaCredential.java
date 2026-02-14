package com.pontoeletronico.api.domain.entity.auth;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "tipo_categoria_credential")
public class TipoCategoriaCredential {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "descricao", nullable = false, unique = true, length = 100)
    private String descricao;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TipoCategoriaCredential that = (TipoCategoriaCredential) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
