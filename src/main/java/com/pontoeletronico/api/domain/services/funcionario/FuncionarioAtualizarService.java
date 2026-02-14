package com.pontoeletronico.api.domain.services.funcionario;

import com.pontoeletronico.api.domain.entity.empresa.XrefGeofenceFuncionario;
import com.pontoeletronico.api.domain.enums.MensagemErro;
import com.pontoeletronico.api.domain.services.audit.AuditoriaRegistroAsyncService;
import com.pontoeletronico.api.exception.*;
import com.pontoeletronico.api.infrastructure.input.dto.empresa.*;
import com.pontoeletronico.api.infrastructure.output.repository.auth.TipoCategoriaCredentialRepository;
import com.pontoeletronico.api.infrastructure.output.repository.auth.TipoCredentialRepository;
import com.pontoeletronico.api.infrastructure.output.repository.auth.UserCredentialRepository;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.*;
import com.pontoeletronico.api.infrastructure.output.repository.usuario.UsuarioGeofenceRepository;
import com.pontoeletronico.api.infrastructure.output.repository.usuario.UsuarioTelefoneRepository;
import com.pontoeletronico.api.infrastructure.output.repository.usuario.UsersRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class FuncionarioAtualizarService {

    private static final String TIPO_CREDENCIAL_CPF = "CPF";
    private static final String CATEGORIA_CREDENCIAL_PRIMARIO = "PRIMARIO";
    private static final String ACAO_ATUALIZAR_FUNCIONARIO = "ATUALIZAR_FUNCIONARIO";

    private final IdentificacaoFuncionarioRepository identificacaoFuncionarioRepository;
    private final UsersRepository usersRepository;
    private final TipoCredentialRepository tipoCredentialRepository;
    private final TipoCategoriaCredentialRepository tipoCategoriaCredentialRepository;
    private final UserCredentialRepository userCredentialRepository;
    private final UsuarioTelefoneRepository usuarioTelefoneRepository;
    private final ContratoFuncionarioRepository contratoFuncionarioRepository;
    private final TipoContratoRepository tipoContratoRepository;
    private final JornadaFuncionarioConfigRepository jornadaFuncionarioConfigRepository;
    private final TipoEscalaJornadaRepository tipoEscalaJornadaRepository;
    private final UsuarioGeofenceRepository usuarioGeofenceRepository;
    private final XrefGeofenceFuncionariosRepository xrefGeofenceFuncionariosRepository;
    private final EmpresaBancoHorasConfigRepository empresaBancoHorasConfigRepository;
    private final AuditoriaRegistroAsyncService auditoriaRegistroAsyncService;

    public FuncionarioAtualizarService(IdentificacaoFuncionarioRepository identificacaoFuncionarioRepository,
                                       UsersRepository usersRepository,
                                       TipoCredentialRepository tipoCredentialRepository,
                                       TipoCategoriaCredentialRepository tipoCategoriaCredentialRepository,
                                       UserCredentialRepository userCredentialRepository,
                                       UsuarioTelefoneRepository usuarioTelefoneRepository,
                                       ContratoFuncionarioRepository contratoFuncionarioRepository,
                                       TipoContratoRepository tipoContratoRepository,
                                       JornadaFuncionarioConfigRepository jornadaFuncionarioConfigRepository,
                                       TipoEscalaJornadaRepository tipoEscalaJornadaRepository,
                                       UsuarioGeofenceRepository usuarioGeofenceRepository,
                                       XrefGeofenceFuncionariosRepository xrefGeofenceFuncionariosRepository,
                                       EmpresaBancoHorasConfigRepository empresaBancoHorasConfigRepository,
                                       AuditoriaRegistroAsyncService auditoriaRegistroAsyncService) {
        this.identificacaoFuncionarioRepository = identificacaoFuncionarioRepository;
        this.usersRepository = usersRepository;
        this.tipoCredentialRepository = tipoCredentialRepository;
        this.tipoCategoriaCredentialRepository = tipoCategoriaCredentialRepository;
        this.userCredentialRepository = userCredentialRepository;
        this.usuarioTelefoneRepository = usuarioTelefoneRepository;
        this.contratoFuncionarioRepository = contratoFuncionarioRepository;
        this.tipoContratoRepository = tipoContratoRepository;
        this.jornadaFuncionarioConfigRepository = jornadaFuncionarioConfigRepository;
        this.tipoEscalaJornadaRepository = tipoEscalaJornadaRepository;
        this.usuarioGeofenceRepository = usuarioGeofenceRepository;
        this.xrefGeofenceFuncionariosRepository = xrefGeofenceFuncionariosRepository;
        this.empresaBancoHorasConfigRepository = empresaBancoHorasConfigRepository;
        this.auditoriaRegistroAsyncService = auditoriaRegistroAsyncService;
    }

    @Transactional
    /** Doc id 16: Alterar dados de funcionário (mesmo corpo do cadastro). */
    public void atualizar(UUID empresaId, UUID funcionarioId, FuncionarioUpdateRequest request, HttpServletRequest httpRequest) {
        if (identificacaoFuncionarioRepository.findByEmpresaIdAndFuncionarioIdAndAtivoTrue(empresaId, funcionarioId).isEmpty()) {
            var dataRef = LocalDateTime.now();
            auditoriaRegistroAsyncService.registrarSemDispositivoID(
                empresaId, 
                ACAO_ATUALIZAR_FUNCIONARIO, 
                "Atualizar funcionário", 
                null, 
                null, 
                false, 
                MensagemErro.FUNCIONARIO_NAO_PERTENCE_EMPRESA.getMensagem(), 
                dataRef, 
                httpRequest
            );
            throw new FuncionarioNaoPertenceEmpresaException();
        }

        var dataAtual = LocalDateTime.now();
        var identificacaoAtual = identificacaoFuncionarioRepository.findByFuncionarioIdAndAtivoTrue(funcionarioId).orElse(null);

        // USERNAME: atualizar só se enviado
        if (request.username() != null && !request.username().isBlank()) {
            if (usersRepository.existsByUsernameAndIdNot(request.username(), funcionarioId).isPresent()) {
                auditoriaRegistroAsyncService.registrarSemDispositivoID(empresaId, ACAO_ATUALIZAR_FUNCIONARIO, "Atualizar funcionário", null, null, false, MensagemErro.USERNAME_JA_CADASTRADO.getMensagem(), dataAtual, httpRequest);
                throw new ConflitoException(MensagemErro.USERNAME_JA_CADASTRADO.getMensagem());
            }
            usersRepository.updateUsername(funcionarioId, request.username());
        }
        var tipoCpfId = tipoCredentialRepository.findIdByDescricao(TIPO_CREDENCIAL_CPF);
        var categoriaPrimarioId = tipoCategoriaCredentialRepository.findIdByDescricao(CATEGORIA_CREDENCIAL_PRIMARIO);
        if (tipoCpfId == null || categoriaPrimarioId == null) {
            auditoriaRegistroAsyncService.registrarSemDispositivoID(empresaId, ACAO_ATUALIZAR_FUNCIONARIO, "Atualizar funcionário", null, null, false, MensagemErro.TIPO_CREDENCIAL_NAO_ENCONTRADO.getMensagem(), dataAtual, httpRequest);
            throw new TipoCredencialNaoEncontradoException();
        }

        // IDENTIFICAÇÃO (nomeCompleto, cpf, dataNascimento): atualizar só se algum for enviado – merge com atuais
        var cpfAtual = identificacaoFuncionarioRepository.findCpfByFuncionarioId(funcionarioId).orElse(null);
        if (request.nomeCompleto() != null || request.cpf() != null || request.dataNascimento() != null) {
            var nomeCompleto = request.nomeCompleto() != null ? request.nomeCompleto() : (identificacaoAtual != null ? identificacaoAtual.getNomeCompleto() : null);
            var cpfNormalizado = request.cpf() != null ? request.cpf().replaceAll("\\D", "") : (identificacaoAtual != null ? identificacaoAtual.getCpf() : null);
            var dataNascimento = request.dataNascimento() != null ? request.dataNascimento() : (identificacaoAtual != null ? identificacaoAtual.getDataNascimento() : null);
            if (cpfNormalizado != null && identificacaoFuncionarioRepository.existsByCpfAndFuncionarioIdNot(cpfNormalizado, funcionarioId).isPresent()) {
                auditoriaRegistroAsyncService.registrarSemDispositivoID(empresaId, ACAO_ATUALIZAR_FUNCIONARIO, "Atualizar funcionário", null, null, false, "CPF já cadastrado.", dataAtual, httpRequest);
                throw new ConflitoException("CPF já cadastrado.");
            }
            identificacaoFuncionarioRepository.updateByFuncionarioId(funcionarioId, nomeCompleto, cpfNormalizado, dataNascimento, dataAtual);
        }
        // CPF (credencial): atualizar só se cpf foi enviado e mudou
        if (request.cpf() != null && tipoCpfId != null) {
            var cpfNormalizadoNovo = request.cpf().replaceAll("\\D", "");
            var cpfMudou = !cpfNormalizadoNovo.equals(cpfAtual);
            if (cpfMudou) {
                if (userCredentialRepository.existsByValorAndTipoCredencialId(cpfNormalizadoNovo, tipoCpfId).isPresent()) {
                    auditoriaRegistroAsyncService.registrarSemDispositivoID(empresaId, ACAO_ATUALIZAR_FUNCIONARIO, "Atualizar funcionário", null, null, false, "CPF já cadastrado.", dataAtual, httpRequest);
                    throw new ConflitoException("CPF já cadastrado.");
                }
                var credencialCpfAntiga = userCredentialRepository.findCredencialIdByUsuarioAndTipo(funcionarioId, tipoCpfId);
                if (credencialCpfAntiga.isPresent()) {
                    userCredentialRepository.desativar(credencialCpfAntiga.get(), funcionarioId, dataAtual);
                }
                userCredentialRepository.insert(UUID.randomUUID(), funcionarioId, tipoCpfId, categoriaPrimarioId, cpfNormalizadoNovo);
            }
        }

        // TELEFONE: só se enviado – desativar antigos e adicionar novo (conjunto validado inteiro via @Valid)
        if (request.usuarioTelefone() != null) {
            usuarioTelefoneRepository.desativarAllByUsuarioId(funcionarioId, dataAtual);
            var usuarioTelefone = request.usuarioTelefone();
            usuarioTelefoneRepository.insert(UUID.randomUUID(), funcionarioId, usuarioTelefone.codigoPais(), usuarioTelefone.ddd(), usuarioTelefone.numero());
        }



        var contrato = request.contratoFuncionario();
        if (contrato != null) {
            if (tipoContratoRepository.existsByIdAndAtivo(contrato.tipoContratoId()).isEmpty()) {
                auditoriaRegistroAsyncService.registrarSemDispositivoID(empresaId, ACAO_ATUALIZAR_FUNCIONARIO, "Atualizar funcionário", null, null, false, MensagemErro.TIPO_CONTRATO_NAO_ENCONTRADO.getMensagem(), dataAtual, httpRequest);
                throw new TipoNaoEncontradoException(MensagemErro.TIPO_CONTRATO_NAO_ENCONTRADO);
            }
            var rows = contratoFuncionarioRepository.updateByFuncionarioId(funcionarioId, contrato.matricula(), contrato.pisPasep(),
                    contrato.cargo(), contrato.departamento(), contrato.tipoContratoId(), contrato.ativo(),
                    contrato.dataAdmissao(), contrato.dataDemissao(), contrato.salarioMensal(), contrato.salarioHora(), dataAtual);
            if (rows == 0) {
                contratoFuncionarioRepository.insert(UUID.randomUUID(), funcionarioId, contrato.matricula(), contrato.pisPasep(),
                        contrato.cargo(), contrato.departamento(), contrato.tipoContratoId(), contrato.ativo(),
                        contrato.dataAdmissao(), contrato.dataDemissao(), contrato.salarioMensal(), contrato.salarioHora(), dataAtual);
            }
        }

        var jornada = request.jornadaFuncionarioConfig();
        if (jornada != null) {
            if (tipoEscalaJornadaRepository.existsByIdAndAtivo(jornada.tipoEscalaJornadaId()).isEmpty()) {
                auditoriaRegistroAsyncService.registrarSemDispositivoID(empresaId, ACAO_ATUALIZAR_FUNCIONARIO, "Atualizar funcionário", null, null, false, MensagemErro.TIPO_ESCALA_JORNADA_NAO_ENCONTRADO.getMensagem(), dataAtual, httpRequest);
                throw new TipoNaoEncontradoException(MensagemErro.TIPO_ESCALA_JORNADA_NAO_ENCONTRADO);
            }
            var rows = jornadaFuncionarioConfigRepository.updateByFuncionarioId(funcionarioId, jornada.tipoEscalaJornadaId(),
                    jornada.cargaHorariaDiaria(), jornada.cargaHorariaSemanal(),
                    jornada.toleranciaPadrao(), jornada.intervaloPadrao(),
                    jornada.entradaPadrao(), jornada.saidaPadrao(),
                    jornada.tempoDescansoEntreJornada(), jornada.gravaGeoObrigatoria(), dataAtual);
            if (rows == 0) {
                jornadaFuncionarioConfigRepository.insert(UUID.randomUUID(), funcionarioId, jornada.tipoEscalaJornadaId(),
                        jornada.cargaHorariaDiaria(), jornada.cargaHorariaSemanal(),
                        jornada.toleranciaPadrao(), jornada.intervaloPadrao(),
                        jornada.entradaPadrao(), jornada.saidaPadrao(),
                        jornada.tempoDescansoEntreJornada(), jornada.gravaGeoObrigatoria(), dataAtual);
            }
        }

        if (request.geofenceIds() != null) {
            xrefGeofenceFuncionariosRepository.deleteByFuncionarioId(funcionarioId);
            for (UUID geofenceId : request.geofenceIds()) {
                if (!usuarioGeofenceRepository.existsByIdAndUsuarioId(geofenceId, empresaId)) {
                    throw new RegistroNaoEncontradoException("Geofence não pertence à empresa");
                }
                xrefGeofenceFuncionariosRepository.save(new XrefGeofenceFuncionario(UUID.randomUUID(), geofenceId, funcionarioId));
            }
        }

        auditoriaRegistroAsyncService.registrarSemDispositivoID(empresaId, ACAO_ATUALIZAR_FUNCIONARIO, "Atualizar funcionário", null, null, true, null, dataAtual, httpRequest);
    }
}
