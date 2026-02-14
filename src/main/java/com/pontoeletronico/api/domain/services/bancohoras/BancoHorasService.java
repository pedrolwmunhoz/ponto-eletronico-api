package com.pontoeletronico.api.domain.services.bancohoras;

import com.pontoeletronico.api.domain.enums.MensagemErro;
import com.pontoeletronico.api.domain.services.audit.AuditoriaRegistroAsyncService;
import com.pontoeletronico.api.exception.FuncionarioNaoPertenceEmpresaException;
import com.pontoeletronico.api.exception.RegistroNaoEncontradoException;
import com.pontoeletronico.api.infrastructure.input.dto.common.Paginacao;
import com.pontoeletronico.api.infrastructure.input.dto.empresa.BancoHorasCompensacaoRequest;
import com.pontoeletronico.api.infrastructure.input.dto.empresa.BancoHorasHistoricoPageResponse;
import com.pontoeletronico.api.infrastructure.input.dto.empresa.BancoHorasHistoricoResponse;
import com.pontoeletronico.api.infrastructure.input.dto.empresa.ResumoBancoHorasResponse;
import com.pontoeletronico.api.infrastructure.output.repository.bancohoras.BancoHorasHistoricoRepository;
import com.pontoeletronico.api.infrastructure.output.repository.bancohoras.BancoHorasMensalRepository;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.EmpresaBancoHorasConfigRepository;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.EmpresaJornadaConfigRepository;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.IdentificacaoFuncionarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class BancoHorasService {

    private static final String ACAO_RESUMO_BANCO_HORAS = "RESUMO_BANCO_HORAS";
    private final IdentificacaoFuncionarioRepository identificacaoFuncionarioRepository;
    private final BancoHorasHistoricoRepository bancoHorasHistoricoRepository;
    private final BancoHorasMensalRepository bancoHorasMensalRepository;
    private final EmpresaJornadaConfigRepository empresaJornadaConfigRepository;
    private final AuditoriaRegistroAsyncService auditoriaRegistroAsyncService;

    public BancoHorasService(IdentificacaoFuncionarioRepository identificacaoFuncionarioRepository,
                             BancoHorasHistoricoRepository bancoHorasHistoricoRepository,
                             EmpresaBancoHorasConfigRepository empresaBancoHorasConfigRepository,
                             BancoHorasMensalRepository bancoHorasMensalRepository,
                             EmpresaJornadaConfigRepository empresaJornadaConfigRepository,
                             AuditoriaRegistroAsyncService auditoriaRegistroAsyncService) {
        this.identificacaoFuncionarioRepository = identificacaoFuncionarioRepository;
        this.bancoHorasHistoricoRepository = bancoHorasHistoricoRepository;
        this.bancoHorasMensalRepository = bancoHorasMensalRepository;
        this.empresaJornadaConfigRepository = empresaJornadaConfigRepository;
        this.auditoriaRegistroAsyncService = auditoriaRegistroAsyncService;
    }

    /** Doc id 43: Resumo banco de horas. Pega registros do mês atual (zoneId da empresa). Soma mês + histórico. */
    public ResumoBancoHorasResponse resumoBancoHoras(UUID empresaId, UUID funcionarioId, HttpServletRequest httpRequest) {
        identificacaoFuncionarioRepository.findByEmpresaIdAndFuncionarioIdAndAtivoTrue(empresaId, funcionarioId)
        .orElseThrow(() -> {
            var dataRef = LocalDateTime.now();
            auditoriaRegistroAsyncService.registrarSemDispositivoID(empresaId, ACAO_RESUMO_BANCO_HORAS, "Resumo banco de horas", null, null, false, MensagemErro.FUNCIONARIO_NAO_PERTENCE_EMPRESA.getMensagem(), dataRef, httpRequest);
            throw new FuncionarioNaoPertenceEmpresaException();
        });

        var zoneId = empresaJornadaConfigRepository.findByEmpresaId(empresaId)
                .map(e -> ZoneId.of(e.getTimezone()))
                .orElse(ZoneId.of("America/Sao_Paulo"));

        var historicos = bancoHorasHistoricoRepository.findByFuncionarioIdOrderByAnoReferenciaDescMesReferenciaDesc(funcionarioId);

        long sumHistoricoEsperadas = historicos.stream()
                .mapToLong(h -> (h.getTotalHorasEsperadas() != null ? h.getTotalHorasEsperadas() : 0))
                .sum();
        long sumHistoricoTrabalhadas = historicos.stream()
                .mapToLong(h -> (h.getTotalHorasTrabalhadas() != null ? h.getTotalHorasTrabalhadas() : 0))
                .sum();
        long sumHistoricoBancoFinal = historicos.stream()
                .mapToLong(h -> (h.getTotalBancoHorasFinal() != null ? h.getTotalBancoHorasFinal() : 0))
                .sum();

        var mesAtual = YearMonth.now(zoneId);
        int anoAtual = mesAtual.getYear();
        int mesAtualVal = mesAtual.getMonthValue();
        Duration mesEsperadas = Duration.ZERO;
        Duration mesTrabalhadas = Duration.ZERO;
        var mensalOpt = bancoHorasMensalRepository.findByFuncionarioIdAndAnoRefAndMesRef(funcionarioId, anoAtual, mesAtualVal);
        if (mensalOpt.isPresent()) {
            var m = mensalOpt.get();
            mesEsperadas = m.getTotalHorasEsperadas() != null ? m.getTotalHorasEsperadas() : Duration.ZERO;
            mesTrabalhadas = m.getTotalHorasTrabalhadas() != null ? m.getTotalHorasTrabalhadas() : Duration.ZERO;
        }
        long mesExtrasMinutos = mesTrabalhadas.toMinutes() - mesEsperadas.toMinutes();
        long totalFinalBanco = mesExtrasMinutos + sumHistoricoBancoFinal;

        Duration totalHorasEsperadas = mesEsperadas.plus(Duration.ofMinutes(sumHistoricoEsperadas));
        Duration totalHorasTrabalhadas = mesTrabalhadas.plus(Duration.ofMinutes(sumHistoricoTrabalhadas));

        var dataRef = LocalDateTime.now();
        auditoriaRegistroAsyncService.registrarSemDispositivoID(empresaId, ACAO_RESUMO_BANCO_HORAS, "Resumo banco de horas", null, null, true, null, dataRef, httpRequest);

        return new ResumoBancoHorasResponse(
                formatMinutesToHHmmSigned(sumHistoricoBancoFinal),
                formatDurationToHHmm(totalHorasEsperadas),
                formatDurationToHHmm(totalHorasTrabalhadas),
                formatMinutesToHHmmSigned(totalFinalBanco)
        );
    }

    /** Doc id 44b: Listar histórico banco de horas de um funcionário (fechamentos mensais). */
    public BancoHorasHistoricoPageResponse listarHistorico(UUID empresaId, UUID funcionarioId, int page, int size, HttpServletRequest httpRequest) {
        identificacaoFuncionarioRepository.findByEmpresaIdAndFuncionarioIdAndAtivoTrue(empresaId, funcionarioId)
        .orElseThrow(() -> {
            var dataRef = LocalDateTime.now();
            auditoriaRegistroAsyncService.registrarSemDispositivoID(empresaId, "LISTAR_BANCO_HORAS_HISTORICO", "Listar histórico banco de horas", null, null, false, MensagemErro.FUNCIONARIO_NAO_PERTENCE_EMPRESA.getMensagem(), dataRef, httpRequest);
            throw new FuncionarioNaoPertenceEmpresaException();
        });
        int limit = Math.max(1, Math.min(size, 100));
        int offset = Math.max(0, page) * limit;
        var list = bancoHorasHistoricoRepository.findPageByFuncionarioId(funcionarioId, limit, offset);
        long total = bancoHorasHistoricoRepository.countByFuncionarioId(funcionarioId);
        var conteudo = list.stream()
                .map(h -> new BancoHorasHistoricoResponse(
                        h.getId(),
                        h.getFuncionarioId(),
                        h.getAnoReferencia(),
                        h.getMesReferencia(),
                        formatMinutesToHHmm(h.getTotalHorasEsperadas() != null ? h.getTotalHorasEsperadas() : 0),
                        formatMinutesToHHmm(h.getTotalHorasTrabalhadas() != null ? h.getTotalHorasTrabalhadas() : 0),
                        formatMinutesToHHmmSigned(h.getTotalBancoHorasFinal() != null ? h.getTotalBancoHorasFinal() : 0),
                        h.getStatus() != null ? h.getStatus() : "FECHADO",
                        h.getValorCompensadoParcial() != null ? h.getValorCompensadoParcial() : 0,
                        mapStatusPagamento(h.getTipoStatusPagamentoId()),
                        h.getAtivo() == null ? true : h.getAtivo(),
                        h.getDataDesativacao()
                ))
                .toList();
        int totalPaginas = (int) Math.max(1, (total + limit - 1) / limit);
        var paginacao = new Paginacao(totalPaginas, total, conteudo.size(), Math.max(0, page));
        var dataRef = LocalDateTime.now();
        auditoriaRegistroAsyncService.registrarSemDispositivoID(empresaId, "LISTAR_BANCO_HORAS_HISTORICO", "Listar histórico banco de horas", null, null, true, null, dataRef, httpRequest);
        return new BancoHorasHistoricoPageResponse(paginacao, conteudo);
    }

    private static final String ACAO_COMPENSACAO = "REGISTRAR_COMPENSACAO_BANCO_HORAS";

    @Transactional
    public void registrarCompensacao(UUID empresaId, BancoHorasCompensacaoRequest request, HttpServletRequest httpRequest) {
        var historico = bancoHorasHistoricoRepository.findById(request.historicoId())
                .orElseThrow(() -> {
                    var dataRef = LocalDateTime.now();
                    auditoriaRegistroAsyncService.registrarSemDispositivoID(empresaId, ACAO_COMPENSACAO, "Registrar compensação banco de horas", null, null, false, "Histórico não encontrado", dataRef, httpRequest);
                    return new RegistroNaoEncontradoException("Histórico de banco de horas não encontrado");
                });
        identificacaoFuncionarioRepository.findByEmpresaIdAndFuncionarioIdAndAtivoTrue(empresaId, historico.getFuncionarioId())
        .orElseThrow(() -> {
            var dataRef = LocalDateTime.now();
                auditoriaRegistroAsyncService.registrarSemDispositivoID(empresaId, ACAO_COMPENSACAO, "Registrar compensação banco de horas", null, null, false, MensagemErro.FUNCIONARIO_NAO_PERTENCE_EMPRESA.getMensagem(), dataRef, httpRequest);
                throw new FuncionarioNaoPertenceEmpresaException();
            });
        int minutos = request.minutos() != null ? request.minutos() : 0;
        if (minutos <= 0) {
            var dataRef = LocalDateTime.now();
            auditoriaRegistroAsyncService.registrarSemDispositivoID(empresaId, ACAO_COMPENSACAO, "Registrar compensação banco de horas", null, null, false, "Minutos deve ser maior que zero", dataRef, httpRequest);
            throw new IllegalArgumentException("Minutos deve ser maior que zero");
        }
        int valorAtual = historico.getValorCompensadoParcial() != null ? historico.getValorCompensadoParcial() : 0;
        int novoValor = valorAtual + minutos;
        historico.setValorCompensadoParcial(novoValor);
        int totalBanco = historico.getTotalBancoHorasFinal() != null ? historico.getTotalBancoHorasFinal() : 0;
        if (totalBanco > 0 && novoValor >= totalBanco) {
            historico.setTipoStatusPagamentoId(3); // PAGO
        } else if (novoValor > 0) {
            historico.setTipoStatusPagamentoId(2); // PARCIAL
        }
        bancoHorasHistoricoRepository.save(historico);
        var dataRef = LocalDateTime.now();
        auditoriaRegistroAsyncService.registrarSemDispositivoID(empresaId, ACAO_COMPENSACAO, "Registrar compensação banco de horas", null, null, true, null, dataRef, httpRequest);
    }

    private static String formatMinutesToHHmm(long totalMinutes) {
        var h = totalMinutes / 60;
        var m = totalMinutes % 60;
        return String.format("%02d:%02d", h, Math.abs(m));
    }

    private static String formatDurationToHHmm(Duration d) {
        if (d == null) return "00:00";
        long totalMinutes = d.toMinutes();
        long h = totalMinutes / 60;
        long m = Math.abs(totalMinutes % 60);
        return String.format("%02d:%02d", h, m);
    }

    private static String formatMinutesToHHmmSigned(long totalMinutes) {
        var sign = totalMinutes < 0 ? "-" : "";
        return sign + formatMinutesToHHmm(Math.abs(totalMinutes));
    }

    private static String mapStatusPagamento(Integer tipoStatusPagamentoId) {
        if (tipoStatusPagamentoId == null) return "PENDENTE";
        return switch (tipoStatusPagamentoId) {
            case 2 -> "PARCIAL";
            case 3 -> "PAGO";
            default -> "PENDENTE";
        };
    }

}
