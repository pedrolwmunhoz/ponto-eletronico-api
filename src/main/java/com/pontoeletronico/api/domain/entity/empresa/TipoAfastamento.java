package com.pontoeletronico.api.domain.entity.empresa;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "tipo_afastamento")
public class TipoAfastamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "descricao", nullable = false, unique = true, length = 255)
    private String descricao;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;
}
