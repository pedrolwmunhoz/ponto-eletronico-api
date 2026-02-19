package com.pontoeletronico.api.util;

import java.util.regex.Pattern;

/**
 * Validação de CNPJ pelo Módulo 11 (IN RFB nº 2.229/2024).
 * <p>
 * Uma única rotina vale para os dois formatos:
 * <ul>
 *   <li>CNPJ atual: 14 dígitos (formato numérico)</li>
 *   <li>CNPJ 2026+: 12 alfanuméricos (A-Z, 0-9) + 2 dígitos verificadores</li>
 * </ul>
 * A Receita estabeleceu que valores numéricos e alfanuméricos sejam substituídos por
 * (código ASCII − 48). Com isso, dígitos 0-9 mantêm valor 0-9 e a mesma fórmula
 * valida o sistema atual e o alfanumérico. Formato: [0-9]{14} ou [A-Z0-9]{12}[0-9]{2}.
 */
public final class CnpjValidator {

    /** Formato atual [0-9]{14} ou novo [A-Z0-9]{12}[0-9]{2}. Uma regex cobre os dois. */
    private static final Pattern PADRAO_CNPJ = Pattern.compile("^[A-Za-z0-9]{12}[0-9]{2}$");

    private static final int LENGTH_BASE = 12;
    private static final int LENGTH_DV = 2;
    private static final int LENGTH_TOTAL = LENGTH_BASE + LENGTH_DV;

    private static final int[] PESOS_PRIMEIRO_DV = { 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2 };
    private static final int[] PESOS_SEGUNDO_DV = { 6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2 };

    private CnpjValidator() {}

    /** Formato esperado: exatamente 14 caracteres, 12 alfanuméricos (A-Z, 0-9) + 2 dígitos (DV), sem máscara. */
    public static boolean isFormatoValido(String cnpj) {
        if (cnpj == null || cnpj.isBlank()) return false;
        String s = cnpj.trim();
        return s.length() == LENGTH_TOTAL && PADRAO_CNPJ.matcher(s).matches();
    }

    /**
     * Valida CNPJ: formato por regex (12 alfanuméricos + 2 dígitos) e DVs pelo Módulo 11.
     * Espera valor sem máscara (14 caracteres).
     */
    public static boolean isValid(String cnpj) {
        if (!isFormatoValido(cnpj)) {
            return false;
        }
        String s = cnpj.trim();
        String base = s.toUpperCase();
        int dv1 = calcularPrimeiroDv(base);
        int dv2 = calcularSegundoDv(base, dv1);
        return (base.charAt(LENGTH_BASE) - '0') == dv1 && (base.charAt(LENGTH_BASE + 1) - '0') == dv2;
    }

    /**
     * Valor numérico para o cálculo: código ASCII - 48 (conforme IN 2.229/2024).
     * 0-9 → 0 a 9; A-Z → 17 a 42.
     */
    private static int valorParaCalculo(char c) {
        return (int) c - 48;
    }

    private static int calcularPrimeiroDv(String base12) {
        int soma = 0;
        for (int i = 0; i < LENGTH_BASE; i++) {
            soma += valorParaCalculo(base12.charAt(i)) * PESOS_PRIMEIRO_DV[i];
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

    private static int calcularSegundoDv(String base12, int primeiroDv) {
        int soma = 0;
        for (int i = 0; i < LENGTH_BASE; i++) {
            soma += valorParaCalculo(base12.charAt(i)) * PESOS_SEGUNDO_DV[i];
        }
        soma += primeiroDv * PESOS_SEGUNDO_DV[LENGTH_BASE];
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }
}
