package com.pontoeletronico.api.domain.services.bancohoras.utils;

import com.pontoeletronico.api.domain.entity.registro.RegistroPonto;

import java.util.Comparator;
import java.util.List;

/**
 * Reordena tipo_entrada (Entrada/Saída) dos registros de ponto.
 * A lista é ordenada por data; em seguida os tipos são resetados do zero:
 * 1º por data = ENTRADA, 2º = SAÍDA, 3º = ENTRADA, 4º = SAÍDA, ...
 */
public final class ReordenarTiposRegistroPontoUtils {

    private ReordenarTiposRegistroPontoUtils() {
    }

    /**
     * Ordena a lista por created_at e atribui tipos do zero: primeiro = Entrada, segundo = Saída, e assim alternando.
     * Altera a lista in-place (ordena e seta tipoEntrada em cada registro).
     */
    public static void reordenarTiposDoZeroPorData(List<RegistroPonto> registros) {
        if (registros == null || registros.isEmpty()) return;
        registros.sort(Comparator.comparing(RegistroPonto::getCreatedAt));
        boolean proximoEntrada = true;
        for (RegistroPonto r : registros) {
            r.setTipoEntrada(proximoEntrada);
            proximoEntrada = !proximoEntrada;
        }
    }
}
