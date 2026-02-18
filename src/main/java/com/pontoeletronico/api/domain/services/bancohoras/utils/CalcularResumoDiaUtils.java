package com.pontoeletronico.api.domain.services.bancohoras.utils;

import com.pontoeletronico.api.domain.entity.empresa.Afastamento;
import com.pontoeletronico.api.domain.entity.empresa.Feriado;
import com.pontoeletronico.api.domain.entity.registro.RegistroPonto;
import com.pontoeletronico.api.domain.entity.registro.ResumoPontoDia;
import com.pontoeletronico.api.domain.services.bancohoras.record.JornadaConfig;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.AfastamentoRepository;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.FeriadoRepository;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.TipoAfastamentoRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CalcularResumoDiaUtils {


    private final AfastamentoRepository afastamentoRepository;
    private final TipoAfastamentoRepository tipoAfastamentoRepository;
    private final FeriadoRepository feriadoRepository;

    public void recalcularResumoDoDia(ResumoPontoDia resumo, List<RegistroPonto> registros, JornadaConfig config) {
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
        resumo.setQuantidadeRegistros(registros.size());
        calcularHoraFeriado(resumo, resumo.getEmpresaId(), resumo.getPrimeiraBatida(), resumo.getUltimaBatida(), registros);
        handleInconsistente(resumo, resumo.getFuncionarioId(), resumo.getPrimeiraBatida(), resumo.getUltimaBatida(), registros);
    }

    private void handleInconsistente(ResumoPontoDia resumo, UUID funcionarioId, LocalDateTime dataInicio, LocalDateTime dataFim, List<RegistroPonto> registros) {

        var listaAfastamentos = afastamentoRepository.findByFuncionarioIdAndDataBetween(funcionarioId, dataInicio, dataFim);
        
        for (Afastamento afastamento : listaAfastamentos) {
            LocalDateTime afastamentoInicio = afastamento.getDataInicio().atStartOfDay();
            LocalDateTime afastamentoFim = afastamento.getDataFim() != null
                    ? afastamento.getDataFim().atTime(23, 59, 59)
                    : afastamento.getDataInicio().atTime(23, 59, 59);

            for (RegistroPonto registro : registros) {
                LocalDateTime dataRegistro = registro.getCreatedAt();
                if ((dataRegistro.isEqual(afastamentoInicio) || dataRegistro.isAfter(afastamentoInicio))
                        && (dataRegistro.isEqual(afastamentoFim) || dataRegistro.isBefore(afastamentoFim))) {
                    
                    var tipoAfastamento = tipoAfastamentoRepository.findById(afastamento.getTipoAfastamentoId()).get();
                            resumo.setInconsistente(true);
                    resumo.setMotivoInconsistencia(tipoAfastamento.getDescricao());
                }
            }
        }   
    }

    private void calcularHoraFeriado(ResumoPontoDia resumo, UUID empresaId, LocalDateTime dataInicio, LocalDateTime dataFim, List<RegistroPonto> registros) {
        var listaFeriados = feriadoRepository.findByDataBetweenAndAtivoTrueForEmpresa(dataInicio, dataFim, empresaId);

        Duration totalHorasTrabalhadasFeriado = Duration.ZERO;

        for (Feriado feriado : listaFeriados) {
            LocalDateTime feriadoInicio = feriado.getData().atStartOfDay();
            LocalDateTime feriadoFim = feriado.getData().atTime(23, 59, 59).plusNanos(999_000_000);

            for (int i = 0; i + 1 < registros.size(); i += 2) {
                LocalDateTime entrada = registros.get(i).getCreatedAt();
                LocalDateTime saida = registros.get(i + 1).getCreatedAt();

                // Soma apenas o trecho que cai dentro do dia do feriado: interseção [entrada, saida] ∩ [início do dia, fim do dia]
                LocalDateTime inicioIntervalo = entrada.isAfter(feriadoInicio) ? entrada : feriadoInicio;
                LocalDateTime fimIntervalo = saida.isBefore(feriadoFim) ? saida : feriadoFim;

                if (!fimIntervalo.isBefore(inicioIntervalo)) {
                    totalHorasTrabalhadasFeriado = totalHorasTrabalhadasFeriado.plus(Duration.between(inicioIntervalo, fimIntervalo));
                }
            }
        }

        resumo.setTotalHorasTrabalhadasFeriado(totalHorasTrabalhadasFeriado);
    }
}
