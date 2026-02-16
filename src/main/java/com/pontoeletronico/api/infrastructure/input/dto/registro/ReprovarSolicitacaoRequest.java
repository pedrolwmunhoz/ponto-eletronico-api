package com.pontoeletronico.api.infrastructure.input.dto.registro;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Doc id 38: Reprovar solicitação de ponto pendente - Request. */
public record ReprovarSolicitacaoRequest(
        @NotNull(message = "motivo é obrigatório")
        @NotBlank(message = "motivo não pode ser vazio")
        @Size(min = 2, max = 500, message = "motivo deve ter entre 2 e 500 caracteres")
        String motivo,
        String observacao
) {}
