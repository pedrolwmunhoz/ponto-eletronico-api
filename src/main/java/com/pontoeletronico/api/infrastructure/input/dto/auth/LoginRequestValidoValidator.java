package com.pontoeletronico.api.infrastructure.input.dto.auth;

import com.pontoeletronico.api.domain.enums.TiposCredencial;
import com.pontoeletronico.api.util.CnpjValidator;
import com.pontoeletronico.api.util.CpfValidator;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * Valida valor conforme tipo de credencial.
 * CPF e CNPJ usam os mesmos validadores dos cadastros (CpfValidator, CnpjValidator).
 */
public class LoginRequestValidoValidator implements ConstraintValidator<LoginRequestValido, LoginRequest> {

    private static final Pattern EMAIL = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    private static final Pattern APENAS_DIGITOS = Pattern.compile("^\\d+$");

    @Override
    public boolean isValid(LoginRequest value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        String valor = value.valor();
        TiposCredencial tipo = value.tipoCredencial();
        if (valor == null || tipo == null) {
            return true; // NotBlank/NotNull tratam
        }
        valor = valor.trim();
        if (valor.isEmpty()) {
            return true;
        }

        boolean valido = switch (tipo) {
            case EMAIL -> EMAIL.matcher(valor).matches();
            case CPF -> {
                String digits = valor.replaceAll("\\D", "");
                yield digits.length() == 11 && CpfValidator.isValid(digits);
            }
            case CNPJ -> {
                String raw = valor.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
                yield raw.length() == 14 && CnpjValidator.isValid(raw);
            }
            case TELEFONE -> APENAS_DIGITOS.matcher(valor).matches() && (valor.length() == 10 || valor.length() == 11);
            case USERNAME -> valor.length() >= 2 && valor.length() <= 255;
        };

        if (!valido) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(tipo.name() + " inválido")
                    .addPropertyNode("valor")
                    .addConstraintViolation();
        }
        return valido;
    }
}
