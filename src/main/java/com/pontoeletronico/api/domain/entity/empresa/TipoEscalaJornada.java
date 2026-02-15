package com.pontoeletronico.api.domain.entity.empresa;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "tipo_escala_jornada")
public class TipoEscalaJornada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "descricao", nullable = false, unique = true, length = 100)
    private String descricao;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;

}
