package com.pontoeletronico.api.domain.services.relatorio;

import com.pontoeletronico.api.domain.entity.empresa.BancoHorasHistorico;
import com.pontoeletronico.api.domain.entity.empresa.IdentificacaoFuncionario;
import com.pontoeletronico.api.domain.services.registro.FuncionarioRegistroPontoService;
import com.pontoeletronico.api.infrastructure.input.dto.registro.PontoListagemResponse;
import com.pontoeletronico.api.infrastructure.input.dto.relatorio.RelatorioPontoDetalhadoDto;
import com.pontoeletronico.api.infrastructure.input.dto.relatorio.RelatorioPontoResumoDto;
import com.pontoeletronico.api.infrastructure.output.repository.bancohoras.BancoHorasHistoricoRepository;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.EmpresaJornadaConfigRepository;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.IdentificacaoFuncionarioRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Doc id 47 e 48: Monta os dados dos relatórios de ponto (detalhado e resumo) para exportação. */
@Service
public class RelatorioPontoService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private final IdentificacaoFuncionarioRepository identificacaoFuncionarioRepository;
    private final FuncionarioRegistroPontoService registroPontoService;
    private final EmpresaJornadaConfigRepository empresaJornadaConfigRepository;
    private final BancoHorasHistoricoRepository bancoHorasHistoricoRepository;

    public RelatorioPontoService(IdentificacaoFuncionarioRepository identificacaoFuncionarioRepository,
                                 FuncionarioRegistroPontoService registroPontoService,
                                 EmpresaJornadaConfigRepository empresaJornadaConfigRepository,
                                 BancoHorasHistoricoRepository bancoHorasHistoricoRepository) {
        this.identificacaoFuncionarioRepository = identificacaoFuncionarioRepository;
        this.registroPontoService = registroPontoService;
        this.empresaJornadaConfigRepository = empresaJornadaConfigRepository;
        this.bancoHorasHistoricoRepository = bancoHorasHistoricoRepository;
    }

    /** Doc id 47: Dados do relatório de ponto detalhado (todos os funcionários da empresa no mês). */
    public RelatorioPontoDetalhadoDto gerarDadosDetalhado(UUID empresaId, int ano, int mes) {
        var inicio = LocalDate.of(ano, mes, 1);
        var fim = inicio.plusMonths(1).minusDays(1);
        var periodo = new RelatorioPontoDetalhadoDto.PeriodoDto(inicio.format(DATE_FMT), fim.format(DATE_FMT));

        var jornadaOpt = empresaJornadaConfigRepository.findByEmpresaId(empresaId);
        var jornadaPrevistaDia = jornadaOpt.map(e -> {
            long min = e.getCargaHorariaDiaria().toMinutes();
            return String.format("%02d:%02d", min / 60, min % 60);
        }).orElse("08:00");

        var funcionarios = identificacaoFuncionarioRepository.findByEmpresaIdOrderByNomeCompletoAsc(empresaId);
        var list = new ArrayList<RelatorioPontoDetalhadoDto.FuncionarioDetalhadoDto>();

        for (IdentificacaoFuncionario id : funcionarios) {
            var diasPonto = registroPontoService.listarPontoFuncionario(id.getFuncionarioId(), ano, mes, null);
            var registros = mapToRegistroDia(diasPonto);
            list.add(new RelatorioPontoDetalhadoDto.FuncionarioDetalhadoDto(
                    id.getFuncionarioId().toString(),
                    id.getNomeCompleto(),
                    jornadaPrevistaDia,
                    registros
            ));
        }

        return new RelatorioPontoDetalhadoDto(periodo, list);
    }

    /** Doc id 49: Dados do relatório de ponto resumo. Dados do fechamento (banco_horas_historico). */
    public RelatorioPontoResumoDto gerarDadosResumo(UUID empresaId, int ano, int mes) {
        var periodo = String.format("%04d-%02d", ano, mes);
        var funcionarios = identificacaoFuncionarioRepository.findByEmpresaIdOrderByNomeCompletoAsc(empresaId);
        var funcionarioIds = funcionarios.stream().map(IdentificacaoFuncionario::getFuncionarioId).toList();
        var fechamentos = bancoHorasHistoricoRepository.findByAnoReferenciaAndMesReferenciaAndFuncionarioIdIn(ano, mes, funcionarioIds);
        var fechamentoPorFunc = fechamentos.stream().collect(java.util.stream.Collectors.toMap(BancoHorasHistorico::getFuncionarioId, h -> h));

        var list = new ArrayList<RelatorioPontoResumoDto.FuncionarioResumoDto>();
        for (IdentificacaoFuncionario id : funcionarios) {
            var h = fechamentoPorFunc.get(id.getFuncionarioId());
            if (h != null) {
                list.add(new RelatorioPontoResumoDto.FuncionarioResumoDto(
                        id.getFuncionarioId().toString(),
                        id.getNomeCompleto(),
                        formatMinutesToHHmm(h.getTotalHorasEsperadas() != null ? h.getTotalHorasEsperadas() : 0),
                        formatMinutesToHHmm(h.getTotalHorasTrabalhadas() != null ? h.getTotalHorasTrabalhadas() : 0),
                        formatMinutesToHHmmSigned(h.getTotalBancoHorasFinal() != null ? h.getTotalBancoHorasFinal() : 0),
                        h.getStatus() != null ? h.getStatus() : "FECHADO"
                ));
            } else {
                list.add(new RelatorioPontoResumoDto.FuncionarioResumoDto(
                        id.getFuncionarioId().toString(),
                        id.getNomeCompleto(),
                        "00:00",
                        "00:00",
                        "00:00",
                        "ABERTO"
                ));
            }
        }
        return new RelatorioPontoResumoDto(periodo, list);
    }

    private List<RelatorioPontoDetalhadoDto.RegistroDiaDto> mapToRegistroDia(List<PontoListagemResponse> diasPonto) {
        var list = new ArrayList<RelatorioPontoDetalhadoDto.RegistroDiaDto>();
        for (var dia : diasPonto) {
            var marcacoes = dia.marcacoes();
            var e1 = marcacoes != null && marcacoes.size() > 0 ? marcacoes.get(0).horario().format(TIME_FMT) : null;
            var s1 = marcacoes != null && marcacoes.size() > 1 ? marcacoes.get(1).horario().format(TIME_FMT) : null;
            var e2 = marcacoes != null && marcacoes.size() > 2 ? marcacoes.get(2).horario().format(TIME_FMT) : null;
            var s2 = marcacoes != null && marcacoes.size() > 3 ? marcacoes.get(3).horario().format(TIME_FMT) : null;
            var horasDia = computarHorasDia(marcacoes);
            list.add(new RelatorioPontoDetalhadoDto.RegistroDiaDto(
                    dia.data(),
                    dia.diaSemana(),
                    e1, s1, e2, s2,
                    horasDia,
                    "00:00",
                    "00:00",
                    "OK"
            ));
        }
        return list;
    }

    /** Total de horas do dia = soma dos pares (entrada, saída). Cada par consecutivo = um período trabalhado. */
    private String computarHorasDia(List<PontoListagemResponse.MarcacaoResponse> marcacoes) {
        if (marcacoes == null || marcacoes.size() < 2) return "00:00";
        long min = 0;
        for (int i = 0; i + 1 < marcacoes.size(); i += 2) {
            min += Duration.between(marcacoes.get(i).horario(), marcacoes.get(i + 1).horario()).toMinutes();
        }
        return formatMinutesToHHmm(min);
    }

    private static String formatMinutesToHHmm(long totalMinutes) {
        var h = totalMinutes / 60;
        var m = totalMinutes % 60;
        return String.format("%02d:%02d", h, Math.abs(m));
    }

    private static String formatMinutesToHHmmSigned(long totalMinutes) {
        var sign = totalMinutes < 0 ? "-" : "";
        return sign + formatMinutesToHHmm(Math.abs(totalMinutes));
    }
}
