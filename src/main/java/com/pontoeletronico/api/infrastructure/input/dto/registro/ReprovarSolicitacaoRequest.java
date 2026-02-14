package com.pontoeletronico.api.infrastructure.input.dto.registro;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Doc id 38: Reprovar solicitação de ponto pendente - Request. */
public record ReprovarSolicitacaoRequest(
        @NotNull(message = "motivo é obrigatório")
        @NotBlank(message = "motivo não pode ser vazio")
        String motivo,
        String observacao
) {}
