package com.pontoeletronico.api.infrastructure.input.dto.perfil;

import com.pontoeletronico.api.infrastructure.input.dto.empresa.ContratoFuncionarioRequest;
import com.pontoeletronico.api.infrastructure.input.dto.empresa.JornadaFuncionarioConfigRequest;
import com.pontoeletronico.api.infrastructure.input.dto.empresa.UsuarioTelefoneRequest;

import java.time.LocalDate;

/** Resposta do perfil do funcionário. Reutiliza Request DTOs para telefone, contrato e jornada (mesmo shape do editar). */
public record FuncionarioPerfilResponse(
        String username,
        Boolean funcionarioAtivo,
        String nomeCompleto,
        String primeiroNome,
        String ultimoNome,
        String cpf,
        LocalDate dataNascimento,
        String matricula,
        String email,
        UsuarioTelefoneRequest usuarioTelefone,
        ContratoFuncionarioRequest contratoFuncionario,
        JornadaFuncionarioConfigRequest jornadaFuncionarioConfig
) {
}
