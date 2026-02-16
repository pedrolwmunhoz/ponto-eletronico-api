package com.pontoeletronico.api.domain.entity.empresa;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import lombok.Data;

@Entity
@Data
@Table(name = "identificacao_funcionario")
public class IdentificacaoFuncionario {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "funcionario_id", nullable = false)
    private UUID funcionarioId;

    @Column(name = "empresa_id", nullable = false)
    private UUID empresaId;

    @Column(name = "nome_completo", nullable = false, length = 255)
    private String nomeCompleto;

    @Column(name = "primeiro_nome", nullable = false, length = 100)
    private String primeiroNome;

    @Column(name = "ultimo_nome", nullable = false, length = 100)
    private String ultimoNome;

    @Column(name = "cpf", nullable = false, unique = true, length = 14)
    private String cpf;

    @Column(name = "codigo_ponto", nullable = false)
    private Integer codigoPonto;

    @Column(name = "data_nascimento")
    private java.time.LocalDate dataNascimento;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IdentificacaoFuncionario that = (IdentificacaoFuncionario) o;
        return Objects.equals(id, that.id);
    }
    @Override
    public int hashCode() { return Objects.hash(id); }
}
