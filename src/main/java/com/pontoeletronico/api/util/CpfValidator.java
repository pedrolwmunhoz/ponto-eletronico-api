package com.pontoeletronico.api.util;

import java.util.regex.Pattern;

/**
 * Validação de CPF pelos dígitos verificadores (Módulo 11).
 * <p>
 * CPF: 11 dígitos (9 da base + 2 DVs). O penúltimo é o DV módulo 11 dos 9 primeiros;
 * o último é o DV módulo 11 dos 10 primeiros.
 * <ul>
 *   <li>DV1: base (9 dígitos) × pesos 10, 9, 8, 7, 6, 5, 4, 3, 2. Soma % 11. Se resto &lt; 2 → 0; senão 11 − resto.</li>
 *   <li>DV2: base + DV1 (10 dígitos) × pesos 11, 10, 9, 8, 7, 6, 5, 4, 3, 2. Mesma regra.</li>
 * </ul>
 * Espera valor sem máscara (11 dígitos).
 */
public final class CpfValidator {

    private static final Pattern PADRAO_CPF = Pattern.compile("^[0-9]{11}$");

    private static final int LENGTH_BASE = 9;
    private static final int LENGTH_TOTAL = 11;

    /** Pesos para o primeiro DV (9 dígitos): 10, 9, 8, 7, 6, 5, 4, 3, 2. */
    private static final int[] PESOS_DV1 = { 10, 9, 8, 7, 6, 5, 4, 3, 2 };

    /** Pesos para o segundo DV (10 dígitos): 11, 10, 9, 8, 7, 6, 5, 4, 3, 2. */
    private static final int[] PESOS_DV2 = { 11, 10, 9, 8, 7, 6, 5, 4, 3, 2 };

    private CpfValidator() {}

    /** Formato esperado: exatamente 11 dígitos, sem máscara. */
    public static boolean isFormatoValido(String cpf) {
        if (cpf == null || cpf.isBlank()) return false;
        String s = cpf.trim();
        return s.length() == LENGTH_TOTAL && PADRAO_CPF.matcher(s).matches();
    }

    /**
     * Valida CPF: formato 11 dígitos e DVs pelo Módulo 11.
     */
    public static boolean isValid(String cpf) {
        if (!isFormatoValido(cpf)) return false;
        String s = cpf.trim();
        int dv1 = calcularDv1(s);
        int dv2 = calcularDv2(s, dv1);
        return (s.charAt(LENGTH_BASE) - '0') == dv1 && (s.charAt(LENGTH_BASE + 1) - '0') == dv2;
    }

    private static int calcularDv1(String base9) {
        int soma = 0;
        for (int i = 0; i < LENGTH_BASE; i++) {
            soma += (base9.charAt(i) - '0') * PESOS_DV1[i];
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

    private static int calcularDv2(String base9, int dv1) {
        int soma = 0;
        for (int i = 0; i < LENGTH_BASE; i++) {
            soma += (base9.charAt(i) - '0') * PESOS_DV2[i];
        }
        soma += dv1 * PESOS_DV2[LENGTH_BASE];
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }
}
