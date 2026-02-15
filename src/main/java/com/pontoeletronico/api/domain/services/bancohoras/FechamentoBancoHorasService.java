package com.pontoeletronico.api.domain.services.bancohoras;

import com.pontoeletronico.api.domain.entity.empresa.BancoHorasHistorico;
import com.pontoeletronico.api.domain.entity.registro.RegistroPonto;
import com.pontoeletronico.api.domain.enums.MensagemErro;
import com.pontoeletronico.api.domain.services.audit.AuditoriaRegistroAsyncService;
import com.pontoeletronico.api.exception.FuncionarioNaoPertenceEmpresaException;
import com.pontoeletronico.api.infrastructure.output.repository.bancohoras.BancoHorasHistoricoRepository;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.EmpresaJornadaConfigRepository;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.IdentificacaoFuncionarioRepository;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.JornadaFuncionarioConfigRepository;
import com.pontoeletronico.api.infrastructure.output.repository.registro.RegistroPontoRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class FechamentoBancoHorasService {

    private static final String ACAO_FECHAMENTO = "FECHAMENTO_BANCO_HORAS";

    private final IdentificacaoFuncionarioRepository identificacaoFuncionarioRepository;
    private final BancoHorasHistoricoRepository bancoHorasHistoricoRepository;
    private final EmpresaJornadaConfigRepository empresaJornadaConfigRepository;
    private final JornadaFuncionarioConfigRepository jornadaFuncionarioConfigRepository;
    private final RegistroPontoRepository registroPontoRepository;
    private final AuditoriaRegistroAsyncService auditoriaRegistroAsyncService;

    public FechamentoBancoHorasService(IdentificacaoFuncionarioRepository identificacaoFuncionarioRepository,
                                       BancoHorasHistoricoRepository bancoHorasHistoricoRepository,
                                       EmpresaJornadaConfigRepository empresaJornadaConfigRepository,
                                       JornadaFuncionarioConfigRepository jornadaFuncionarioConfigRepository,
                                       RegistroPontoRepository registroPontoRepository,
                                       AuditoriaRegistroAsyncService auditoriaRegistroAsyncService) {
        this.identificacaoFuncionarioRepository = identificacaoFuncionarioRepository;
        this.bancoHorasHistoricoRepository = bancoHorasHistoricoRepository;
        this.empresaJornadaConfigRepository = empresaJornadaConfigRepository;
        this.jornadaFuncionarioConfigRepository = jornadaFuncionarioConfigRepository;
        this.registroPontoRepository = registroPontoRepository;
        this.auditoriaRegistroAsyncService = auditoriaRegistroAsyncService;
    }

    @Transactional
    public void fechar(UUID empresaId, UUID funcionarioId, int ano, int mes, HttpServletRequest httpRequest) {
        if (!identificacaoFuncionarioRepository.findByEmpresaIdAndFuncionarioIdAndAtivoTrue(empresaId, funcionarioId).isPresent()) {
            var dataRef = LocalDateTime.now();
            auditoriaRegistroAsyncService.registrarSemDispositivoID(empresaId, ACAO_FECHAMENTO, "Fechamento banco de horas", null, null, false, MensagemErro.FUNCIONARIO_NAO_PERTENCE_EMPRESA.getMensagem(), dataRef, httpRequest);
            throw new FuncionarioNaoPertenceEmpresaException();
        }

        var zoneId = empresaJornadaConfigRepository.findByEmpresaId(empresaId)
                .map(e -> ZoneId.of(e.getTimezone()))
                .orElse(ZoneId.of("America/Sao_Paulo"));

        var mesRef = YearMonth.of(ano, mes);
        var inicioMes = mesRef.atDay(1).atStartOfDay(zoneId).toLocalDateTime();
        var fimMes = mesRef.plusMonths(1).atDay(1).atStartOfDay(zoneId).toLocalDateTime();

        long totalTrabalhadas = calcularTotalHorasTrabalhadas(funcionarioId, inicioMes, fimMes);
        long totalEsperadas = calcularTotalHorasEsperadasMes(funcionarioId, mesRef, zoneId, empresaId);
        int totalBancoHorasFinal = (int) (totalTrabalhadas - totalEsperadas);

        var existing = bancoHorasHistoricoRepository.findByFuncionarioIdAndAnoReferenciaAndMesReferencia(funcionarioId, ano, mes);
        var now = LocalDateTime.now();
        if (existing.isPresent()) {
            var h = existing.get();
            h.setTotalHorasEsperadas((int) totalEsperadas);
            h.setTotalHorasTrabalhadas((int) totalTrabalhadas);
            h.setTotalBancoHorasFinal(totalBancoHorasFinal);
            h.setStatus("FECHADO");
            bancoHorasHistoricoRepository.save(h);
        } else {
            var h = new BancoHorasHistorico();
            h.setId(UUID.randomUUID());
            h.setFuncionarioId(funcionarioId);
            h.setAnoReferencia(ano);
            h.setMesReferencia(mes);
            h.setTotalHorasEsperadas((int) totalEsperadas);
            h.setTotalHorasTrabalhadas((int) totalTrabalhadas);
            h.setTotalBancoHorasFinal(totalBancoHorasFinal);
            h.setStatus("FECHADO");
            h.setCreatedAt(now);
            bancoHorasHistoricoRepository.save(h);
        }

        auditoriaRegistroAsyncService.registrarSemDispositivoID(empresaId, ACAO_FECHAMENTO, "Fechamento banco de horas", null, null, true, null, now, httpRequest);
    }

    private long calcularTotalHorasTrabalhadas(UUID funcionarioId, LocalDateTime inicioMes, LocalDateTime fimMes) {
        var registros = registroPontoRepository.findByUsuarioIdAndCreatedAtBetweenOrderByCreatedAtAsc(
                funcionarioId, inicioMes, fimMes);
        if (registros.isEmpty()) return 0;
        long totalMinutos = 0;
        RegistroPonto entradaPendente = null;
        for (RegistroPonto r : registros) {
            if (Boolean.TRUE.equals(r.getTipoEntrada())) {
                entradaPendente = r;
            } else {
                if (entradaPendente != null) {
                    totalMinutos += ChronoUnit.MINUTES.between(entradaPendente.getCreatedAt(), r.getCreatedAt());
                    entradaPendente = null;
                } else {
                    totalMinutos += ChronoUnit.MINUTES.between(inicioMes, r.getCreatedAt());
                }
            }
        }
        return totalMinutos;
    }

    private long calcularTotalHorasEsperadasMes(UUID funcionarioId, YearMonth mes, ZoneId zoneId, UUID empresaId) {
        var jornadaFuncOpt = jornadaFuncionarioConfigRepository.findByFuncionarioId(funcionarioId);
        var jornadaEmpresaOpt = empresaJornadaConfigRepository.findByEmpresaId(empresaId);
        int escalaId;
        int cargaDiariaMinutos;
        int cargaSemanalMinutos;
        if (jornadaFuncOpt.isPresent()) {
            var j = jornadaFuncOpt.get();
            escalaId = j.getTipoEscalaJornadaId();
            cargaDiariaMinutos = (int) j.getCargaHorariaDiaria().toMinutes();
            cargaSemanalMinutos = (int) j.getCargaHorariaSemanal().toMinutes();
        } else if (jornadaEmpresaOpt.isPresent()) {
            var j = jornadaEmpresaOpt.get();
            escalaId = j.getTipoEscalaJornadaId();
            cargaDiariaMinutos = (int) j.getCargaHorariaDiaria().toMinutes();
            cargaSemanalMinutos = (int) j.getCargaHorariaSemanal().toMinutes();
        } else {
            return 0;
        }
        int diasUteis = 0;
        var primeiroDia = mes.atDay(1);
        var ultimoDia = mes.atEndOfMonth();
        for (var d = primeiroDia; !d.isAfter(ultimoDia); d = d.plusDays(1)) {
            var dow = d.getDayOfWeek();
            if (escalaId == 1) {
                if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY) diasUteis++;
            } else if (escalaId == 2) {
                if (dow != DayOfWeek.SUNDAY) diasUteis++;
            } else {
                diasUteis++;
            }
        }
        return escalaId == 3 ? (long) (cargaSemanalMinutos * mes.lengthOfMonth() / 7.0) : (long) diasUteis * cargaDiariaMinutos;
    }
}
