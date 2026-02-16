package com.pontoeletronico.api.domain.services.funcionario;

import com.pontoeletronico.api.domain.entity.empresa.XrefGeofenceFuncionario;
import com.pontoeletronico.api.domain.enums.MensagemErro;
import com.pontoeletronico.api.domain.services.audit.AuditoriaRegistroAsyncService;
import com.pontoeletronico.api.domain.services.empresa.MetricasDiariaEmpresaContadorService;
import com.pontoeletronico.api.exception.ConflitoException;
import com.pontoeletronico.api.exception.EmpresaNaoEncontradaException;
import com.pontoeletronico.api.exception.TipoCredencialNaoEncontradoException;
import com.pontoeletronico.api.exception.TipoNaoEncontradoException;
import com.pontoeletronico.api.exception.TipoUsuarioNaoEncontradoException;
import com.pontoeletronico.api.infrastructure.input.dto.empresa.*;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.*;
import com.pontoeletronico.api.infrastructure.output.repository.usuario.UsuarioGeofenceRepository;
import com.pontoeletronico.api.exception.RegistroNaoEncontradoException;
import com.pontoeletronico.api.infrastructure.output.repository.usuario.UsuarioTelefoneRepository;
import com.pontoeletronico.api.infrastructure.output.repository.usuario.UsersRepository;
import com.pontoeletronico.api.infrastructure.output.repository.auth.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class FuncionarioCriarService {

    private static final String TIPO_FUNCIONARIO = "FUNCIONARIO";
    private static final String TIPO_CREDENCIAL_EMAIL = "EMAIL";
    private static final String CATEGORIA_CREDENCIAL_PRIMARIO = "PRIMARIO";
    private static final String ACAO_CADASTRO_FUNCIONARIO = "CADASTRO_FUNCIONARIO";

    private final UsersRepository usersRepository;
    private final TipoUsuarioRepository tipoUsuarioRepository;
    private final TipoCredentialRepository tipoCredentialRepository;
    private final TipoCategoriaCredentialRepository tipoCategoriaCredentialRepository;
    private final UserCredentialRepository userCredentialRepository;
    private final UserPasswordRepository userPasswordRepository;
    private final IdentificacaoFuncionarioRepository identificacaoFuncionarioRepository;
    private final EmpresaDadosFiscalRepository empresaDadosFiscalRepository;
    private final UsuarioTelefoneRepository usuarioTelefoneRepository;
    private final ContratoFuncionarioRepository contratoFuncionarioRepository;
    private final TipoContratoRepository tipoContratoRepository;
    private final JornadaFuncionarioConfigRepository jornadaFuncionarioConfigRepository;
    private final TipoEscalaJornadaRepository tipoEscalaJornadaRepository;
    private final UsuarioGeofenceRepository usuarioGeofenceRepository;
    private final XrefGeofenceFuncionariosRepository xrefGeofenceFuncionariosRepository;
    private final EmpresaBancoHorasConfigRepository empresaBancoHorasConfigRepository;
    private final FuncionarioRegistroLockRepository funcionarioRegistroLockRepository;
    private final AuditoriaRegistroAsyncService auditoriaRegistroAsyncService;
    private final MetricasDiariaEmpresaContadorService metricasDiariaEmpresaContadorService;
    private final PasswordEncoder passwordEncoder;

    public FuncionarioCriarService(UsersRepository usersRepository,
                                   TipoUsuarioRepository tipoUsuarioRepository,
                                   TipoCredentialRepository tipoCredentialRepository,
                                   UserCredentialRepository userCredentialRepository,
                                   UserPasswordRepository userPasswordRepository,
                                   IdentificacaoFuncionarioRepository identificacaoFuncionarioRepository,
                                   TipoCategoriaCredentialRepository tipoCategoriaCredentialRepository,
                                   EmpresaDadosFiscalRepository empresaDadosFiscalRepository,
                                   UsuarioTelefoneRepository usuarioTelefoneRepository,
                                   ContratoFuncionarioRepository contratoFuncionarioRepository,
                                   TipoContratoRepository tipoContratoRepository,
                                   JornadaFuncionarioConfigRepository jornadaFuncionarioConfigRepository,
                                   TipoEscalaJornadaRepository tipoEscalaJornadaRepository,
                                   UsuarioGeofenceRepository usuarioGeofenceRepository,
                                   XrefGeofenceFuncionariosRepository xrefGeofenceFuncionariosRepository,
                                   EmpresaBancoHorasConfigRepository empresaBancoHorasConfigRepository,
                                   FuncionarioRegistroLockRepository funcionarioRegistroLockRepository,
                                   AuditoriaRegistroAsyncService auditoriaRegistroAsyncService,
                                   MetricasDiariaEmpresaContadorService metricasDiariaEmpresaContadorService,
                                   PasswordEncoder passwordEncoder) {
        this.usersRepository = usersRepository;
        this.tipoUsuarioRepository = tipoUsuarioRepository;
        this.tipoCredentialRepository = tipoCredentialRepository;
        this.userCredentialRepository = userCredentialRepository;
        this.userPasswordRepository = userPasswordRepository;
        this.identificacaoFuncionarioRepository = identificacaoFuncionarioRepository;
        this.tipoCategoriaCredentialRepository = tipoCategoriaCredentialRepository;
        this.empresaDadosFiscalRepository = empresaDadosFiscalRepository;
        this.usuarioTelefoneRepository = usuarioTelefoneRepository;
        this.contratoFuncionarioRepository = contratoFuncionarioRepository;
        this.tipoContratoRepository = tipoContratoRepository;
        this.jornadaFuncionarioConfigRepository = jornadaFuncionarioConfigRepository;
        this.tipoEscalaJornadaRepository = tipoEscalaJornadaRepository;
        this.usuarioGeofenceRepository = usuarioGeofenceRepository;
        this.xrefGeofenceFuncionariosRepository = xrefGeofenceFuncionariosRepository;
        this.empresaBancoHorasConfigRepository = empresaBancoHorasConfigRepository;
        this.funcionarioRegistroLockRepository = funcionarioRegistroLockRepository;
        this.auditoriaRegistroAsyncService = auditoriaRegistroAsyncService;
        this.metricasDiariaEmpresaContadorService = metricasDiariaEmpresaContadorService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    /** Doc id 12: Cadastro de funcionário. */
    public UUID criar(UUID empresaId, FuncionarioCreateRequest request, HttpServletRequest httpRequest) {
        var dataCriacao = LocalDateTime.now();

        if (empresaDadosFiscalRepository.existsByEmpresaId(empresaId).isEmpty()) {
            registrarAuditoria(empresaId, null, false, MensagemErro.EMPRESA_NAO_ENCONTRADA.getMensagem(), dataCriacao, httpRequest);
            throw new EmpresaNaoEncontradaException();
        }

        var emailNormalizado = request.email().trim().toLowerCase();
        var cpfNormalizado = request.cpf().replaceAll("\\D", "");

        if (usersRepository.existsByUsername(request.username()).isPresent()) {
            registrarAuditoria(empresaId, null, false, MensagemErro.USERNAME_JA_CADASTRADO.getMensagem(), dataCriacao, httpRequest);
            throw new ConflitoException(MensagemErro.USERNAME_JA_CADASTRADO.getMensagem());
        }
        var tipoEmailId = tipoCredentialRepository.findIdByDescricao(TIPO_CREDENCIAL_EMAIL);
        if (tipoEmailId == null) {
            registrarAuditoria(empresaId, null, false, MensagemErro.TIPO_CREDENCIAL_NAO_ENCONTRADO.getMensagem(), dataCriacao, httpRequest);
            throw new TipoCredencialNaoEncontradoException();
        }
        if (userCredentialRepository.existsByValorAndTipoCredencialId(emailNormalizado, tipoEmailId).isPresent()) {
            registrarAuditoria(empresaId, null, false, MensagemErro.EMAIL_JA_CADASTRADO.getMensagem(), dataCriacao, httpRequest);
            throw new ConflitoException(MensagemErro.EMAIL_JA_CADASTRADO.getMensagem());
        }
        if (identificacaoFuncionarioRepository.existsByCpf(cpfNormalizado).isPresent()) {
            registrarAuditoria(empresaId, null, false, MensagemErro.CPF_JA_CADASTRADO.getMensagem(), dataCriacao, httpRequest);
            throw new ConflitoException(MensagemErro.CPF_JA_CADASTRADO.getMensagem());
        }

        var tipoFuncionarioId = tipoUsuarioRepository.findIdByDescricao(TIPO_FUNCIONARIO);
        if (tipoFuncionarioId == null) {
            registrarAuditoria(empresaId, null, false, MensagemErro.TIPO_USUARIO_NAO_ENCONTRADO.getMensagem(), dataCriacao, httpRequest);
            throw new TipoUsuarioNaoEncontradoException();
        }

        var contrato = request.contratoFuncionario();
        if (contrato != null) {
            if (tipoContratoRepository.existsByIdAndAtivo(contrato.tipoContratoId()).isEmpty()) {
                registrarAuditoria(empresaId, null, false, MensagemErro.TIPO_CONTRATO_NAO_ENCONTRADO.getMensagem(), dataCriacao, httpRequest);
                throw new TipoNaoEncontradoException(MensagemErro.TIPO_CONTRATO_NAO_ENCONTRADO);
            }
        }
        var jornada = request.jornadaFuncionarioConfig();
        if (jornada != null) {
            if (tipoEscalaJornadaRepository.existsByIdAndAtivo(jornada.tipoEscalaJornadaId()).isEmpty()) {
                registrarAuditoria(empresaId, null, false, MensagemErro.TIPO_ESCALA_JORNADA_NAO_ENCONTRADO.getMensagem(), dataCriacao, httpRequest);
                throw new TipoNaoEncontradoException(MensagemErro.TIPO_ESCALA_JORNADA_NAO_ENCONTRADO);
            }
        }
        var funcionarioId = UUID.randomUUID();

        usersRepository.insert(funcionarioId, request.username(), tipoFuncionarioId, dataCriacao);

        var credencialId = UUID.randomUUID();
        var categoriaPrimarioId = tipoCategoriaCredentialRepository.findIdByDescricao(CATEGORIA_CREDENCIAL_PRIMARIO);
        if (categoriaPrimarioId == null) {
            registrarAuditoria(empresaId, null, false, MensagemErro.TIPO_CREDENCIAL_NAO_ENCONTRADO.getMensagem(), dataCriacao, httpRequest);
            throw new TipoCredencialNaoEncontradoException();
        }
        userCredentialRepository.insert(credencialId, funcionarioId, tipoEmailId, categoriaPrimarioId, emailNormalizado);
        userPasswordRepository.insert(UUID.randomUUID(), funcionarioId, passwordEncoder.encode(request.senha()), dataCriacao);

        var codigoPonto = identificacaoFuncionarioRepository.nextCodigoPonto(empresaId);
        if (codigoPonto == null || codigoPonto > 999999) {
            registrarAuditoria(empresaId, null, false, "Limite de funcionários por empresa atingido.", dataCriacao, httpRequest);
            throw new ConflitoException("Limite de funcionários por empresa atingido.");
        }
        identificacaoFuncionarioRepository.insert(
                UUID.randomUUID(), funcionarioId, empresaId,
                request.nomeCompleto(), request.primeiroNome(), request.ultimoNome(), cpfNormalizado, codigoPonto,
                request.dataNascimento(), dataCriacao);
        funcionarioRegistroLockRepository.insert(funcionarioId, empresaId);

        if (request.usuarioTelefone() != null) {
            var telefone = request.usuarioTelefone();
            if (usuarioTelefoneRepository.existsByCodigoPaisAndDddAndNumero(telefone.codigoPais(), telefone.ddd(), telefone.numero()).isPresent()) {
                registrarAuditoria(empresaId, funcionarioId, false, MensagemErro.TELEFONE_JA_CADASTRADO.getMensagem(), dataCriacao, httpRequest);
                throw new ConflitoException(MensagemErro.TELEFONE_JA_CADASTRADO.getMensagem());
            }
            usuarioTelefoneRepository.insert(UUID.randomUUID(), funcionarioId, telefone.codigoPais(), telefone.ddd(), telefone.numero());
        }

        if (contrato != null) {
            if (contrato.matricula() != null && !contrato.matricula().isBlank()
                    && contratoFuncionarioRepository.existsByMatricula(contrato.matricula()).isPresent()) {
                registrarAuditoria(empresaId, funcionarioId, false, "Matrícula já cadastrada.", dataCriacao, httpRequest);
                throw new ConflitoException("Matrícula já cadastrada.");
            }
            if (contrato.pisPasep() != null && !contrato.pisPasep().isBlank()
                    && contratoFuncionarioRepository.existsByPisPasep(contrato.pisPasep().replaceAll("\\D", "")).isPresent()) {
                registrarAuditoria(empresaId, funcionarioId, false, "PIS/PASEP já cadastrado.", dataCriacao, httpRequest);
                throw new ConflitoException("PIS/PASEP já cadastrado.");
            }
            contratoFuncionarioRepository.insert(
                    UUID.randomUUID(), funcionarioId,
                    contrato.matricula(), contrato.pisPasep(),
                    contrato.cargo(), contrato.departamento(),
                    contrato.tipoContratoId(), contrato.ativo(),
                    contrato.dataAdmissao(), contrato.dataDemissao(),
                    contrato.salarioMensal(), contrato.salarioHora(),
                    dataCriacao);
        }

        if (jornada != null) {
            jornadaFuncionarioConfigRepository.insert(
                    UUID.randomUUID(), funcionarioId, jornada.tipoEscalaJornadaId(),
                    jornada.cargaHorariaDiaria(), jornada.cargaHorariaSemanal(),
                    jornada.toleranciaPadrao(), jornada.intervaloPadrao(),
                    jornada.entradaPadrao(), jornada.saidaPadrao(),
                    jornada.tempoDescansoEntreJornada(), jornada.gravaGeoObrigatoria(),
                    dataCriacao);
        }

        if (request.geofenceIds() != null && !request.geofenceIds().isEmpty()) {
            for (UUID geofenceId : request.geofenceIds()) {
                if (!usuarioGeofenceRepository.existsByIdAndUsuarioId(geofenceId, empresaId)) {
                    throw new RegistroNaoEncontradoException("Geofence não pertence à empresa");
                }
                xrefGeofenceFuncionariosRepository.save(new XrefGeofenceFuncionario(UUID.randomUUID(), geofenceId, funcionarioId));
            }
        }

        registrarAuditoria(empresaId, funcionarioId, true, null, dataCriacao, httpRequest);
        metricasDiariaEmpresaContadorService.incrementarQuantidadeFuncionarios(empresaId);
        return funcionarioId;
    }

    private void registrarAuditoria(UUID empresaId, UUID funcionarioId, boolean sucesso, String mensagemErro, LocalDateTime dataCriacao, HttpServletRequest httpRequest) {
        var descricao = funcionarioId != null ? "Cadastro de funcionário " + funcionarioId : "Cadastro de funcionário";
        auditoriaRegistroAsyncService.registrarSemDispositivoID(empresaId, ACAO_CADASTRO_FUNCIONARIO, descricao, null, null, sucesso, mensagemErro, dataCriacao, httpRequest);
    }
}
