package com.pontoeletronico.api.domain.services.bancohoras.utils;

import com.pontoeletronico.api.domain.entity.registro.RegistroPonto;
import com.pontoeletronico.api.domain.entity.registro.ResumoPontoDia;
import com.pontoeletronico.api.domain.services.bancohoras.record.JornadaConfig;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;

public class CalcularResumoDiaUtils {

    public static void recalcularResumoDoDia(ResumoPontoDia resumo, List<RegistroPonto> registros, JornadaConfig config) {
        if (registros.isEmpty()) return;
        registros.sort(Comparator.comparing(RegistroPonto::getCreatedAt));
        resumo.setPrimeiraBatida(registros.get(0).getCreatedAt());
        resumo.setUltimaBatida(registros.get(registros.size() - 1).getCreatedAt());
        Duration totalHorasTrabalhadas = Duration.ZERO;
        for (int i = 0; i + 1 < registros.size(); i += 2) {
            totalHorasTrabalhadas = totalHorasTrabalhadas.plus(Duration.between(registros.get(i).getCreatedAt(), registros.get(i + 1).getCreatedAt()));
        }
        resumo.setTotalHorasTrabalhadas(totalHorasTrabalhadas);
        resumo.setTotalHorasEsperadas(config.cargaDiaria());
        resumo.setInconsistente(registros.size() % 2 != 0);
        resumo.setMotivoInconsistencia(registros.size() % 2 != 0 ? "IMPAR" : null);
    }
}
