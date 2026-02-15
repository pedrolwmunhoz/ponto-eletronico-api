package com.pontoeletronico.api.domain.entity.empresa;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Data;

@Entity
@Data
@Table(name = "contrato_funcionario")
public class ContratoFuncionario {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "funcionario_id", nullable = false)
    private UUID funcionarioId;

    @Column(name = "matricula", length = 50)
    private String matricula;

    @Column(name = "pis_pasep", length = 20)
    private String pisPasep;

    @Column(name = "cargo", nullable = false, length = 255)
    private String cargo;

    @Column(name = "departamento", length = 255)
    private String departamento;

    @Column(name = "tipo_contrato_id", nullable = false)
    private Integer tipoContratoId;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;

    @Column(name = "data_admissao", nullable = false)
    private LocalDate dataAdmissao;

    @Column(name = "data_demissao")
    private LocalDate dataDemissao;

    @Column(name = "salario_mensal", nullable = false, precision = 15, scale = 2)
    private BigDecimal salarioMensal;

    @Column(name = "salario_hora", nullable = false, precision = 10, scale = 4)
    private BigDecimal salarioHora;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

}
