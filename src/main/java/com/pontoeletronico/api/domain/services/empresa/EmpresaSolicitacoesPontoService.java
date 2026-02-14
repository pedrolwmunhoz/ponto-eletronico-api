package com.pontoeletronico.api.domain.services.empresa;

import com.pontoeletronico.api.domain.services.auth.DispositivoService;
import com.pontoeletronico.api.domain.services.bancohoras.CalcularBancoHorasSoftDeleteService;
import com.pontoeletronico.api.domain.services.bancohoras.CalcularHorasMetricasService;
import com.pontoeletronico.api.domain.services.registro.LockRegistroPontoService;
import com.pontoeletronico.api.domain.services.registro.FuncionarioRegistroPontoService;
import com.pontoeletronico.api.domain.services.util.ObterJornadaConfigUtils;
import com.pontoeletronico.api.exception.BadRequestException;
import com.pontoeletronico.api.exception.FuncionarioNaoPertenceEmpresaException;
import com.pontoeletronico.api.exception.RegistroNaoEncontradoException;
import com.pontoeletronico.api.domain.entity.registro.RegistroPonto;
import com.pontoeletronico.api.domain.entity.registro.SolicitacaoPonto;
import com.pontoeletronico.api.infrastructure.input.dto.registro.*;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.IdentificacaoFuncionarioRepository;
import com.pontoeletronico.api.infrastructure.output.repository.registro.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Data
public class EmpresaSolicitacoesPontoService {

    private static final int TIPO_MARCACAO_MANUAL = 1;

    private static final Map<DayOfWeek, String> DIA_SEMANA = Map.of(
            DayOfWeek.MONDAY, "SEG", DayOfWeek.TUESDAY, "TER", DayOfWeek.WEDNESDAY, "QUA",
            DayOfWeek.THURSDAY, "QUI", DayOfWeek.FRIDAY, "SEX", DayOfWeek.SATURDAY, "SAB", DayOfWeek.SUNDAY, "DOM");

    private final IdentificacaoFuncionarioRepository identificacaoFuncionarioRepository;
    private final FuncionarioRegistroPontoService registroPontoService;
    private final RegistroPontoRepository registroPontoRepository;
    private final SolicitacaoPontoRepository solicitacaoPontoRepository;
    private final SolicitacoesPontoListagemRepository solicitacoesPontoListagemRepository;
    private final DispositivoService dispositivoService;
    private final LockRegistroPontoService lockRegistroPontoService;
    private final CalcularBancoHorasSoftDeleteService calcularBancoHorasSoftDeleteService;
    private final MetricasDiariaEmpresaContadorService metricasDiariaEmpresaContadorService;
    private final CalcularHorasMetricasService calcularHorasMetricasService;
    private final ObterJornadaConfigUtils obterJornadaConfigUtils;

    /** Doc id 33: Listar informações de ponto de um funcionário (ano/mês). */
    public List<PontoListagemResponse> listarPonto(UUID empresaId, UUID funcionarioId, int ano, int mes, HttpServletRequest httpRequest) {
        if (funcionarioId == null) {
            throw new BadRequestException("Id do funcionário é obrigatório");
        }
        if (ano <= 0 || mes <= 0) {
            throw new BadRequestException("Ano e mês são obrigatórios");
        }
        identificacaoFuncionarioRepository.findByEmpresaIdAndFuncionarioIdAndAtivoTrue(empresaId, funcionarioId)
                .orElseThrow(() -> new FuncionarioNaoPertenceEmpresaException());
        return registroPontoService.listarPontoFuncionario(funcionarioId, ano, mes, httpRequest);
    }

    /** Doc id 34: Deletar registro de ponto de um funcionário. Empresa sempre desativa direto e chama recálculo. */
    @Transactional
    public void empresaDeletarRegistroManual(UUID empresaId, UUID funcionarioId, UUID registroId) {
        if (funcionarioId == null) {
            throw new BadRequestException("Id do funcionário é obrigatório");
        }
        if (registroId == null) {
            throw new BadRequestException("Id do registro de ponto é obrigatório");
        }

        lockRegistroPontoService.adquirirLock(funcionarioId);
        
        identificacaoFuncionarioRepository.findByEmpresaIdAndFuncionarioIdAndAtivoTrue(empresaId, funcionarioId)
                .orElseThrow(() -> new FuncionarioNaoPertenceEmpresaException());
        
        RegistroPonto registroPonto = registroPontoRepository.findById(registroId)
            .orElseThrow(() -> new RegistroNaoEncontradoException("Registro de ponto não encontrado"));
        registroPontoRepository.desativar(registroId, funcionarioId);


        
        calcularBancoHorasSoftDeleteService.processarSoftDelete(funcionarioId, empresaId, registroId, registroPonto.getCreatedAt(), obterJornadaConfigUtils.obterJornadaConfig(empresaId, funcionarioId));
    }

    /** Editar registro: desativa o antigo e cria outro com os novos dados; dispara recálculo (soft delete + entrada manual). */
    @Transactional
    public void empresaEditarRegistroManual(UUID empresaId, UUID idempotencyKey, UUID funcionarioId, UUID registroId, EmpresaCriarRegistroPontoRequest request, HttpServletRequest httpRequest) {
        if (funcionarioId == null) {
            throw new BadRequestException("Id do funcionário é obrigatório");
        }
        if (idempotencyKey == null) {
            throw new BadRequestException("Idempotency-Key é obrigatório");
        }
        if (registroId == null) {
            throw new BadRequestException("Id do registro de ponto é obrigatório");
        }
        
        if (registroPontoRepository.findByIdempotencyKeyAndUsuarioIdAndAtivoTrue(idempotencyKey, funcionarioId).isPresent()) {
            return;
        }
        
        lockRegistroPontoService.adquirirLock(funcionarioId);
        identificacaoFuncionarioRepository.findByEmpresaIdAndFuncionarioIdAndAtivoTrue(empresaId, funcionarioId)
                .orElseThrow(() -> new FuncionarioNaoPertenceEmpresaException());
        
        var rows = registroPontoRepository.desativar(registroId, funcionarioId);
        if (rows == 0) {
            throw new RegistroNaoEncontradoException("Registro de ponto não encontrado");
        }

        var horario = request.horario();
        var dispositivoId = dispositivoService.obterOuCriar(funcionarioId, httpRequest);
        var descricao = request.justificativa() + (request.observacao() != null ? " | " + request.observacao() : "");
        var idNovoRegistro = UUID.randomUUID();
        var diaSemana = DIA_SEMANA.get(horario.getDayOfWeek());
        registroPontoRepository.insert(idNovoRegistro, idempotencyKey, funcionarioId, diaSemana, dispositivoId, TIPO_MARCACAO_MANUAL, descricao, horario);
        RegistroPonto registroPonto = registroPontoRepository.findById(idNovoRegistro).orElseThrow(() -> new RegistroNaoEncontradoException("Registro de ponto não encontrado"));

        calcularHorasMetricasService.calcularHorasAposEntradaManual(empresaId, registroPonto, obterJornadaConfigUtils.obterJornadaConfig(empresaId, funcionarioId));
    }

    /** Doc id 35: Criar registro de ponto para um funcionário. Empresa sempre insere direto (não cria solicitação). */
    @Transactional
    public void empresaCriarRegistroManual(UUID empresaId, UUID idempotencyKey, UUID funcionarioId, EmpresaCriarRegistroPontoRequest request, HttpServletRequest httpRequest) {
        if (funcionarioId == null) {
            throw new BadRequestException("Id do funcionário é obrigatório");
        }
        if (idempotencyKey == null) {
            throw new BadRequestException("Idempotency-Key é obrigatório");
        }
        if (request == null || request.horario() == null || request.justificativa() == null) {
            throw new BadRequestException("Horário e justificativa são obrigatórios");
        }

        if (registroPontoRepository.findByIdempotencyKeyAndUsuarioIdAndAtivoTrue(idempotencyKey, funcionarioId).isPresent()) {
            return;
        }

        lockRegistroPontoService.adquirirLock(funcionarioId);
        identificacaoFuncionarioRepository.findByEmpresaIdAndFuncionarioIdAndAtivoTrue(empresaId, funcionarioId)
                .orElseThrow(() -> new FuncionarioNaoPertenceEmpresaException());
        var horario = request.horario();
        var dispositivoId = dispositivoService.obterOuCriar(funcionarioId, httpRequest);
        var descricao = request.justificativa() + (request.observacao() != null ? " | " + request.observacao() : "");
        var idNovoRegistro = UUID.randomUUID();
        var diaSemana = DIA_SEMANA.get(horario.getDayOfWeek());

        registroPontoRepository.insert(idNovoRegistro, idempotencyKey, funcionarioId, diaSemana, dispositivoId, TIPO_MARCACAO_MANUAL, descricao, horario);
        RegistroPonto registroPonto = registroPontoRepository.findById(idNovoRegistro).orElseThrow(() -> new RegistroNaoEncontradoException("Registro de ponto não encontrado"));
        calcularHorasMetricasService.calcularHorasAposEntradaManual(empresaId, registroPonto, obterJornadaConfigUtils.obterJornadaConfig(empresaId, funcionarioId));
    }

    /** Doc id 36: Listar todas as solicitações de ponto dos funcionários (criar/excluir registro manual). */
    public SolicitacoesPontoListagemResponse listarSolicitacoes(UUID empresaId, int page, int size) {
        int limit = Math.max(1, Math.min(size, 100));
        int offset = Math.max(0, page) * limit;
        var list = solicitacoesPontoListagemRepository.findPageByEmpresaId(empresaId, limit, offset);
        long total = solicitacoesPontoListagemRepository.countByEmpresaId(empresaId);
        var items = list.stream()
                .map(p -> new SolicitacaoPontoItemResponse(p.getId(), p.getTipo(), p.getData(), p.getMotivo(), p.getNomeFuncionario(), p.getStatus()))
                .toList();
        return new SolicitacoesPontoListagemResponse(items, total, Math.max(0, page), limit);
    }

    /** Aprova solicitação (criar ou remover registro). Insere registro ou desativa + reordena tipo_entrada. */
    @Transactional
    public void aprovar(UUID empresaId, UUID idempotencyKey, UUID idRegistroPendente, HttpServletRequest httpRequest) {
        if (idRegistroPendente == null) {
            throw new BadRequestException("Id da solicitação de ponto é obrigatório");
        }
        if (idempotencyKey == null) {
            throw new BadRequestException("Idempotency-Key é obrigatório");
        }
        
        var solicitacaoPonto = solicitacaoPontoRepository.findById(idRegistroPendente)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Solicitação não encontrada"));
        identificacaoFuncionarioRepository.findByEmpresaIdAndFuncionarioIdAndAtivoTrue(empresaId, solicitacaoPonto.getUsuarioId())
                .orElseThrow(() -> new FuncionarioNaoPertenceEmpresaException());
        lockRegistroPontoService.adquirirLock(solicitacaoPonto.getUsuarioId());
        
        var now = LocalDateTime.now();
        RegistroPonto registroPonto = null;
        
        
        var rows = solicitacaoPontoRepository.updateAprovacao(idRegistroPendente, true, empresaId, null, now);
        if (rows == 0) {
            throw new RegistroNaoEncontradoException("Solicitação já foi processada ou não encontrada");
        }
        if (SolicitacaoPonto.TIPO_CRIAR == solicitacaoPonto.getTipoSolicitacaoId()) {
            var horario = solicitacaoPonto.getDataHoraRegistro();
            var dispositivoId = dispositivoService.obterOuCriar(solicitacaoPonto.getUsuarioId(), httpRequest);
            var idNovoRegistro = UUID.randomUUID();
            var diaSemana = DIA_SEMANA.get(horario.getDayOfWeek());
            registroPontoRepository.insert(idNovoRegistro, idempotencyKey, solicitacaoPonto.getUsuarioId(), diaSemana, dispositivoId, TIPO_MARCACAO_MANUAL, "Registro aprovado pela empresa", horario);
            registroPonto = registroPontoRepository.findById(idNovoRegistro).orElseThrow(() -> new RegistroNaoEncontradoException("Registro de ponto não encontrado"));
            calcularHorasMetricasService.calcularHorasAposEntradaManual(empresaId, registroPonto, obterJornadaConfigUtils.obterJornadaConfig(empresaId, solicitacaoPonto.getUsuarioId()));
        } else {
            RegistroPonto registroPontoDeletado = registroPontoRepository.findById(solicitacaoPonto.getRegistroPontoId())
            .orElseThrow(() -> new RegistroNaoEncontradoException("Registro de ponto não encontrado"));
            var rowsDelete = registroPontoRepository.desativar(solicitacaoPonto.getRegistroPontoId(), solicitacaoPonto.getUsuarioId());
            if (rowsDelete == 0) {
                throw new RegistroNaoEncontradoException("Registro de ponto não encontrado");
            }
            calcularBancoHorasSoftDeleteService.processarSoftDelete(solicitacaoPonto.getUsuarioId(), empresaId, solicitacaoPonto.getRegistroPontoId(), registroPontoDeletado.getCreatedAt(), obterJornadaConfigUtils.obterJornadaConfig(empresaId, solicitacaoPonto.getUsuarioId()));
        }

    }

    /** Doc id 38: Reprovar solicitação de ponto pendente. */
    @Transactional
    public void reprovar(UUID empresaId, UUID idRegistroPendente, ReprovarSolicitacaoRequest request) {
        
        if (idRegistroPendente == null) {
            throw new BadRequestException("Id da solicitação de ponto é obrigatório");
        }
        if (request == null || request.motivo() == null) {
            throw new BadRequestException("Motivo é obrigatório");
        }
        
        var solicitacaoPonto = solicitacaoPontoRepository.findById(idRegistroPendente)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Solicitação não encontrada"));
        identificacaoFuncionarioRepository.findByEmpresaIdAndFuncionarioIdAndAtivoTrue(empresaId, solicitacaoPonto.getUsuarioId())
                .orElseThrow(() -> new FuncionarioNaoPertenceEmpresaException());
        var observacao = request.motivo() + (request.observacao() != null ? " | " + request.observacao() : "");
        var now = LocalDateTime.now();
        var rows = solicitacaoPontoRepository.updateAprovacao(idRegistroPendente, false, empresaId, observacao, now);
        if (rows == 0) {
            throw new RegistroNaoEncontradoException("Solicitação já foi processada ou não encontrada");
        }
    }


}
