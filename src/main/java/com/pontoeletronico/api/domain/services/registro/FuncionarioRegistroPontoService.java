package com.pontoeletronico.api.domain.services.registro;

import com.pontoeletronico.api.domain.services.auth.DispositivoService;
import com.pontoeletronico.api.domain.services.bancohoras.CalcularBancoHorasAplicativoService;
import com.pontoeletronico.api.domain.services.bancohoras.CalcularBancoHorasSoftDeleteService;
import com.pontoeletronico.api.domain.services.bancohoras.CalcularHorasMetricasService;
import com.pontoeletronico.api.domain.services.bancohoras.record.JornadaConfig;
import com.pontoeletronico.api.domain.services.empresa.MetricasDiariaEmpresaContadorService;
import com.pontoeletronico.api.domain.enums.MensagemErro;
import com.pontoeletronico.api.domain.services.util.ObterJornadaConfigUtils;
import com.pontoeletronico.api.exception.BadRequestException;
import com.pontoeletronico.api.exception.FuncionarioNaoEncontradoException;
import com.pontoeletronico.api.exception.RegistroNaoEncontradoException;
import com.pontoeletronico.api.exception.TipoNaoEncontradoException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pontoeletronico.api.infrastructure.input.dto.registro.*;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.EmpresaJornadaConfigRepository;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.IdentificacaoFuncionarioRepository;
import com.pontoeletronico.api.domain.entity.registro.RegistroPonto;
import com.pontoeletronico.api.domain.entity.registro.SolicitacaoPonto;
import com.pontoeletronico.api.infrastructure.output.repository.registro.RegistroPontoRepository;
import com.pontoeletronico.api.infrastructure.output.repository.registro.ResumoPontoDiaRepository;
import com.pontoeletronico.api.infrastructure.output.repository.registro.SolicitacaoPontoRepository;
import com.pontoeletronico.api.infrastructure.output.repository.registro.TipoJustificativaRepository;
import com.pontoeletronico.api.infrastructure.output.repository.registro.XrefPontoResumoRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Data
public class FuncionarioRegistroPontoService {

    private static final int TIPO_MARCACAO_MANUAL = 1;
    private static final int TIPO_MARCACAO_SISTEMA = 2;

    private static final String MOTIVO_OUTROS = "OUTROS";

    private static final Map<DayOfWeek, String> DIA_SEMANA = Map.of(
            DayOfWeek.MONDAY, "SEG", DayOfWeek.TUESDAY, "TER", DayOfWeek.WEDNESDAY, "QUA",
            DayOfWeek.THURSDAY, "QUI", DayOfWeek.FRIDAY, "SEX", DayOfWeek.SATURDAY, "SAB", DayOfWeek.SUNDAY, "DOM");

    private final RegistroPontoRepository registroPontoRepository;
    private final IdentificacaoFuncionarioRepository identificacaoFuncionarioRepository;
    private final EmpresaJornadaConfigRepository empresaJornadaConfigRepository;
    private final SolicitacaoPontoRepository solicitacaoPontoRepository;
    private final TipoJustificativaRepository tipoJustificativaRepository;
    private final DispositivoService dispositivoService;
    private final RegistroPontoValidacaoService registroPontoValidacaoService;
    private final LockRegistroPontoService lockRegistroPontoService;
    private final CalcularBancoHorasAplicativoService calcularBancoHorasRegistroAplicativoService;
    private final CalcularBancoHorasSoftDeleteService calcularBancoHorasSoftDeleteService;
    private final ResumoPontoDiaRepository resumoPontoDiaRepository;
    private final XrefPontoResumoRepository xrefPontoResumoRepository;
    private final MetricasDiariaEmpresaContadorService metricasDiariaEmpresaContadorService;
    private final CalcularHorasMetricasService calcularHorasMetricasService;
   
    private static final int IDX_JORNADA = 0;
    private static final int IDX_DATA = 1;
    private static final int IDX_DIA_SEMANA = 2;
    private static final int IDX_STATUS = 3;
    private static final int IDX_TOTAL_HORAS_RAW = 4;
    private static final int IDX_MARCADOES_JSON = 5;

    /** Doc id 28: Lista todas as jornadas do mês (resumo_ponto_dia) e todos os registros de cada uma (xref + registro_ponto). Período = mês inteiro (dia 1 ao último dia). Data = primeira batida. Jornada sequencial 1, 2, 3... */
    public List<PontoListagemResponse> listarPontoFuncionario(UUID funcionarioId, int ano, int mes, HttpServletRequest httpRequest) {
        var inicio = LocalDate.of(ano, mes, 1);
        var fim = inicio.plusMonths(1).minusDays(1);
        var rows = resumoPontoDiaRepository.findPontoListagemRowsRaw(funcionarioId, inicio, fim);
        return mapPontoListagemRowsToResponse(rows);
    }

    private static List<PontoListagemResponse> mapPontoListagemRowsToResponse(List<Object[]> rows) {
        ObjectMapper om = new ObjectMapper();
        om.registerModule(new JavaTimeModule());
        List<PontoListagemResponse> result = new ArrayList<>();
        for (Object[] row : rows) {
            String jornada = row[IDX_JORNADA] != null ? row[IDX_JORNADA].toString() : null;
            LocalDate data = toLocalDate(row[IDX_DATA]);
            String diaSemana = data != null ? DIA_SEMANA.get(data.getDayOfWeek()) : null;
            String status = row[IDX_STATUS] != null ? row[IDX_STATUS].toString() : null;
            String totalHorasRaw = row[IDX_TOTAL_HORAS_RAW] != null ? row[IDX_TOTAL_HORAS_RAW].toString() : "PT0S";
            String totalHoras = "00:00";
            try {
                Duration d = Duration.parse(totalHorasRaw);
                long min = d.toMinutes();
                totalHoras = String.format("%02d:%02d", min / 60, Math.abs(min % 60));
            } catch (Exception ignored) { }
            String marcacoesJson = toStringJson(row[IDX_MARCADOES_JSON]);
            List<PontoListagemResponse.MarcacaoResponse> marcacoes = new ArrayList<>();
            if (marcacoesJson != null && !marcacoesJson.isEmpty()) {
                try {
                    List<MarcacaoJsonItem> list = om.readValue(marcacoesJson, new TypeReference<List<MarcacaoJsonItem>>() { });
                    for (MarcacaoJsonItem m : list) {
                        marcacoes.add(new PontoListagemResponse.MarcacaoResponse(m.registroId(), m.horario(), m.tipo()));
                    }
                } catch (Exception ignored) { }
            }
            result.add(new PontoListagemResponse(jornada, data, diaSemana, status, marcacoes, totalHoras));
        }
        return result;
    }

    private static LocalDate toLocalDate(Object val) {
        if (val == null) return null;
        if (val instanceof LocalDate d) return d;
        if (val instanceof java.sql.Date d) return d.toLocalDate();
        if (val instanceof java.util.Date d) return d.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        return LocalDate.parse(val.toString());
    }

    private static String toStringJson(Object val) {
        if (val == null) return null;
        if (val instanceof String s) return s;
        try {
            return val.getClass().getMethod("getValue").invoke(val).toString();
        } catch (Exception e) {
            return val.toString();
        }
    }

    private record MarcacaoJsonItem(UUID registroId, LocalDateTime horario, String tipo) { }

    @Transactional
    public void registrarPontoManualFuncionario(UUID funcionarioId, UUID idempotencyKey, RegistroPontoManualRequest request, HttpServletRequest httpRequest) {
        if (idempotencyKey == null) {
            throw new BadRequestException("Idempotency-Key obrigatório");
        }
        if (request == null || request.horario() == null || request.justificativa() == null) {
            throw new BadRequestException("horario e justificativa são obrigatórios");
        }

        var empresaId = identificacaoFuncionarioRepository.findEmpresaIdByFuncionarioIdAndAtivoTrue(funcionarioId)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Funcionário não encontrado"));
        var jornadaConfig = registroPontoValidacaoService.validar(empresaId, funcionarioId, request.registroMetadados());

        var registroPonto = registroPontoRepository.findByIdempotencyKeyAndUsuarioIdAndAtivoTrue(idempotencyKey, funcionarioId).orElse(null);
        var solicitacaoPonto = solicitacaoPontoRepository.findByIdempotencyKeyAndUsuarioId(idempotencyKey, funcionarioId).orElse(null);
        if (registroPonto != null || solicitacaoPonto != null) {
            return;
        }

        lockRegistroPontoService.adquirirLock(funcionarioId);
        var dataRegistroManual = request.horario();
        if (Boolean.TRUE.equals(jornadaConfig.permiteAjustePonto())) {
            
            var dispositivoId = dispositivoService.obterOuCriar(funcionarioId, httpRequest);
            var descricao = request.justificativa() + (request.observacao() != null ? " | " + request.observacao() : "");
            
            var idNovoRegistro = UUID.randomUUID();
            var diaSemana = DIA_SEMANA.get(dataRegistroManual.getDayOfWeek());

            registroPontoRepository.insert(idNovoRegistro, idempotencyKey, funcionarioId, diaSemana, dispositivoId, TIPO_MARCACAO_MANUAL, descricao, dataRegistroManual);
            var novoRegistro = registroPontoRepository.findById(idNovoRegistro).orElseThrow(() -> new RegistroNaoEncontradoException("Registro de ponto não encontrado"));
            calcularHorasMetricasService.calcularHorasAposEntradaManual(empresaId, novoRegistro, jornadaConfig);
        } else {
            var tipoJustificativaId = tipoJustificativaRepository.findIdByDescricao(request.justificativa());
            if (tipoJustificativaId == null) {
                throw new TipoNaoEncontradoException(MensagemErro.TIPO_MOTIVO_SOLICITACAO_NAO_ENCONTRADO);
            }
            var now = LocalDateTime.now();
            solicitacaoPontoRepository.insert(UUID.randomUUID(), idempotencyKey, funcionarioId, SolicitacaoPonto.TIPO_CRIAR, dataRegistroManual, null, tipoJustificativaId, now);
        }
    }
    /** Doc id 30: Registro de ponto público (tablet da empresa). */
    @Transactional
    public void registrarPontoAppPublicoFuncionario(UUID empresaId, RegistroPontoPublicoRequest request, UUID idempotencyKey, HttpServletRequest httpRequest) {
        var dataNovoRegistro = LocalDateTime.now();
        if (request == null || request.codigoPonto() == null ) {
            throw new BadRequestException("Código de ponto obrigatório");
        }
        if (idempotencyKey == null) {
            throw new BadRequestException("Idempotency-Key obrigatório");
        }
        
        var funcionarioId = identificacaoFuncionarioRepository.findFuncionarioIdByEmpresaIdAndCodigoPontoAndAtivoTrue(empresaId, request.codigoPonto())
                .orElseThrow(() -> new FuncionarioNaoEncontradoException());

        if (registroPontoRepository.findByIdempotencyKeyAndUsuarioIdAndAtivoTrue(idempotencyKey, funcionarioId).isPresent()) {
            return;
        }

        var jornadaConfig = registroPontoValidacaoService.validar(empresaId, funcionarioId, request.registroMetadados());

        lockRegistroPontoService.adquirirLock(funcionarioId);
        
        var dispositivoId = dispositivoService.obterOuCriar(empresaId, httpRequest);

        var idNovoRegistro = UUID.randomUUID();
        var diaSemana = DIA_SEMANA.get(dataNovoRegistro.getDayOfWeek());
        registroPontoRepository.insert(idNovoRegistro, idempotencyKey, funcionarioId, diaSemana, dispositivoId, TIPO_MARCACAO_SISTEMA, null, dataNovoRegistro);
        RegistroPonto registroPonto = registroPontoRepository.findById(idNovoRegistro).orElseThrow(() -> new RegistroNaoEncontradoException("Registro de ponto não encontrado"));
        calcularHorasMetricasService.calcularHorasAposEntradaManual(empresaId, registroPonto, jornadaConfig);
    }

    /** Doc id 31: Registro de ponto pelo aplicativo. Data/hora = instante da requisição (backend). */
    @Transactional
    public void registrarPontoAppIndividualFuncionario(UUID funcionarioId, UUID idempotencyKey, RegistroPontoAppRequest request, HttpServletRequest httpRequest) {
        var dataNovoRegistro = LocalDateTime.now();
        if (idempotencyKey == null) {
            throw new BadRequestException("Idempotency-Key obrigatório");
        }
        if (registroPontoRepository.findByIdAndUsuarioIdAndAtivoTrue(idempotencyKey, funcionarioId).isPresent()) {
            return;
        }

        lockRegistroPontoService.adquirirLock(funcionarioId);
        var identificacao = identificacaoFuncionarioRepository.findFirstByFuncionarioIdAndAtivoTrue(funcionarioId).orElse(null);
        if (identificacao == null) {
            throw new FuncionarioNaoEncontradoException();
        }
        var jornadaConfig = registroPontoValidacaoService.validar(identificacao.getEmpresaId(), funcionarioId, request.registroMetadados());
        var dispositivoId = dispositivoService.obterOuCriar(funcionarioId, httpRequest);
        var diaSemana = DIA_SEMANA.get(dataNovoRegistro.getDayOfWeek());
        var idNovoRegistro = UUID.randomUUID();
        registroPontoRepository.insert(idNovoRegistro, idempotencyKey, funcionarioId, diaSemana, dispositivoId, TIPO_MARCACAO_SISTEMA, null, dataNovoRegistro);
        RegistroPonto registroPonto = registroPontoRepository.findById(idNovoRegistro).orElseThrow(() -> new RegistroNaoEncontradoException("Registro de ponto não encontrado"));
        calcularHorasMetricasService.calcularHorasAposEntradaManual(identificacao.getEmpresaId(), registroPonto, jornadaConfig);
    }

    /** Doc id 32: Deletar registro de ponto (funcionário). */
    @Transactional
    public void deletarRegistroFuncionario(UUID funcionarioId, UUID idRegistro, RegistroMetadadosRequest request) {
        if (idRegistro == null) {
            throw new BadRequestException("Id do registro de ponto é obrigatório");
        }
        var identificacao = identificacaoFuncionarioRepository.findFirstByFuncionarioIdAndAtivoTrue(funcionarioId)
                .orElseThrow(() -> new FuncionarioNaoEncontradoException());
        var registroPonto = registroPontoRepository.findById(idRegistro)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Registro de ponto não encontrado"));

        var jornadaConfig = registroPontoValidacaoService.validar(identificacao.getEmpresaId(), funcionarioId, request);
        
        if (Boolean.TRUE.equals(jornadaConfig.permiteAjustePonto())) {
            lockRegistroPontoService.adquirirLock(funcionarioId);

            var rows = registroPontoRepository.desativar(idRegistro, funcionarioId);
            if (rows == 0) {
                throw new RegistroNaoEncontradoException("Registro de ponto não encontrado");
            }
            calcularBancoHorasSoftDeleteService.processarSoftDelete(funcionarioId, identificacao.getEmpresaId(), idRegistro, registroPonto.getCreatedAt(), jornadaConfig);
        } else {
            var tipoJustificativaId = tipoJustificativaRepository.findIdByDescricao(MOTIVO_OUTROS);
            if (tipoJustificativaId == null) {
                throw new IllegalStateException("Tipo justificativa OUTROS não encontrado");
            }
            var now = LocalDateTime.now();
            solicitacaoPontoRepository.insert(UUID.randomUUID(), idRegistro, funcionarioId, SolicitacaoPonto.TIPO_REMOVER, null, idRegistro, tipoJustificativaId, now);
        }
    }
}
