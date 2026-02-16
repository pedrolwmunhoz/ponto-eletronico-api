package com.pontoeletronico.api.domain.services.usuario;

import com.pontoeletronico.api.domain.enums.MensagemErro;
import com.pontoeletronico.api.domain.services.audit.AuditoriaRegistroAsyncService;
import com.pontoeletronico.api.exception.ConflitoException;
import com.pontoeletronico.api.exception.CredencialNaoEncontradaException;
import com.pontoeletronico.api.exception.TelefoneNaoEncontradoException;
import com.pontoeletronico.api.exception.UsuarioNaoEncontradoException;
import com.pontoeletronico.api.infrastructure.input.dto.usuario.UsuarioCredentialAdicionarRequest;
import com.pontoeletronico.api.infrastructure.input.dto.usuario.UsuarioEmailRequest;
import com.pontoeletronico.api.infrastructure.input.dto.usuario.UsuarioPerfilAtualizarRequest;
import com.pontoeletronico.api.infrastructure.input.dto.usuario.UsuarioTelefoneAdicionarRequest;
import com.pontoeletronico.api.infrastructure.output.repository.auth.TipoCategoriaCredentialRepository;
import com.pontoeletronico.api.infrastructure.output.repository.auth.TipoCredentialRepository;
import com.pontoeletronico.api.infrastructure.output.repository.auth.UserCredentialRepository;
import com.pontoeletronico.api.infrastructure.output.repository.usuario.UsersRepository;
import com.pontoeletronico.api.infrastructure.output.repository.usuario.UsuarioTelefoneRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UsuarioService {

    private static final String TIPO_CREDENCIAL_EMAIL = "EMAIL";
    private static final String CATEGORIA_CREDENCIAL_PRIMARIO = "PRIMARIO";
    private static final String CATEGORIA_CREDENCIAL_SECUNDARIO = "SECUNDARIO";
    private static final String ACAO_ATUALIZAR_PERFIL = "ATUALIZAR_PERFIL";
    private static final String ACAO_ADICIONAR_EMAIL = "ADICIONAR_EMAIL";
    private static final String ACAO_REMOVER_EMAIL = "REMOVER_EMAIL";
    private static final String ACAO_ATUALIZAR_EMAIL = "ATUALIZAR_EMAIL";
    private static final String ACAO_ADICIONAR_TELEFONE = "ADICIONAR_TELEFONE";
    private static final String ACAO_REMOVER_TELEFONE = "REMOVER_TELEFONE";
    private static final String ACAO_ADICIONAR_CREDENTIAL = "ADICIONAR_CREDENTIAL";
    private static final String ACAO_REMOVER_CREDENTIAL = "REMOVER_CREDENTIAL";

    private final UsersRepository usersRepository;
    private final UserCredentialRepository userCredentialRepository;
    private final UsuarioTelefoneRepository usuarioTelefoneRepository;
    private final TipoCredentialRepository tipoCredentialRepository;
    private final TipoCategoriaCredentialRepository tipoCategoriaCredentialRepository;
    private final AuditoriaRegistroAsyncService auditoriaRegistroAsyncService;

    public UsuarioService(UsersRepository usersRepository,
                          UserCredentialRepository userCredentialRepository,
                          UsuarioTelefoneRepository usuarioTelefoneRepository,
                          TipoCredentialRepository tipoCredentialRepository,
                          TipoCategoriaCredentialRepository tipoCategoriaCredentialRepository,
                          AuditoriaRegistroAsyncService auditoriaRegistroAsyncService) {
        this.usersRepository = usersRepository;
        this.userCredentialRepository = userCredentialRepository;
        this.usuarioTelefoneRepository = usuarioTelefoneRepository;
        this.tipoCredentialRepository = tipoCredentialRepository;
        this.tipoCategoriaCredentialRepository = tipoCategoriaCredentialRepository;
        this.auditoriaRegistroAsyncService = auditoriaRegistroAsyncService;
    }

    /** Doc id 19: Atualizar username. */
    @Transactional
    public void atualizarPerfil(UUID usuarioId, UsuarioPerfilAtualizarRequest request, HttpServletRequest httpRequest) {
        usersRepository.findByIdQuery(usuarioId).orElseThrow(UsuarioNaoEncontradoException::new);
        if (usersRepository.existsByUsername(request.username()).isPresent()) {
            throw new ConflitoException(MensagemErro.USERNAME_JA_CADASTRADO.getMensagem());
        }
        var rows = usersRepository.updateUsername(usuarioId, request.username());
        if (rows == 0) {
            throw new UsuarioNaoEncontradoException();
        }
        var dataReferencia = LocalDateTime.now();
        auditoriaRegistroAsyncService.registrarSemDispositivoID(usuarioId, ACAO_ATUALIZAR_PERFIL, "Atualizar username", null, null, true, null, dataReferencia, httpRequest);
    }

    @Transactional
    public void adicionarEmail(UUID usuarioId, UsuarioEmailRequest request, HttpServletRequest httpRequest) {
        usersRepository.findByIdQuery(usuarioId).orElseThrow(UsuarioNaoEncontradoException::new);
        var tipoEmailId = tipoCredentialRepository.findIdByDescricao(TIPO_CREDENCIAL_EMAIL);
        if (tipoEmailId == null) {
            throw new com.pontoeletronico.api.exception.TipoCredencialNaoEncontradoException();
        }
        if (userCredentialRepository.existsByValorAndTipoCredencialId(request.novoEmail().trim().toLowerCase(), tipoEmailId).isPresent()) {
            throw new ConflitoException(MensagemErro.EMAIL_JA_CADASTRADO.getMensagem());
        }
        var categoriaPrimarioId = tipoCategoriaCredentialRepository.findIdByDescricao(CATEGORIA_CREDENCIAL_PRIMARIO);
        var categoriaSecundarioId = tipoCategoriaCredentialRepository.findIdByDescricao(CATEGORIA_CREDENCIAL_SECUNDARIO);
        if (categoriaPrimarioId == null || categoriaSecundarioId == null) throw new com.pontoeletronico.api.exception.TipoCredencialNaoEncontradoException();
        var jaExistePrincipal = userCredentialRepository.findCredencialIdByUsuarioTipoCategoria(usuarioId, tipoEmailId, categoriaPrimarioId).isPresent();
        var categoriaId = jaExistePrincipal ? categoriaSecundarioId : categoriaPrimarioId;
        userCredentialRepository.insert(
                UUID.randomUUID(), usuarioId, tipoEmailId, categoriaId, request.novoEmail().trim().toLowerCase());
        var dataReferencia = LocalDateTime.now();
        auditoriaRegistroAsyncService.registrarSemDispositivoID(usuarioId, ACAO_ADICIONAR_EMAIL, "Adicionar email", null, null, true, null, dataReferencia, httpRequest);
    }

    /** Atualizar email primário (UPDATE do valor na credencial existente). */
    @Transactional
    public void atualizarEmail(UUID usuarioId, UsuarioEmailRequest request, HttpServletRequest httpRequest) {
        usersRepository.findByIdQuery(usuarioId).orElseThrow(UsuarioNaoEncontradoException::new);
        var tipoEmailId = tipoCredentialRepository.findIdByDescricao(TIPO_CREDENCIAL_EMAIL);
        if (tipoEmailId == null) {
            throw new com.pontoeletronico.api.exception.TipoCredencialNaoEncontradoException();
        }
        var emailNormalizado = request.novoEmail().trim().toLowerCase();
        if (userCredentialRepository.existsByValorAndTipoCredencialId(emailNormalizado, tipoEmailId).isPresent()) {
            throw new ConflitoException(MensagemErro.EMAIL_JA_CADASTRADO.getMensagem());
        }
        var categoriaPrimarioId = tipoCategoriaCredentialRepository.findIdByDescricao(CATEGORIA_CREDENCIAL_PRIMARIO);
        if (categoriaPrimarioId == null) {
            throw new com.pontoeletronico.api.exception.TipoCredencialNaoEncontradoException();
        }
        var credencialId = userCredentialRepository.findCredencialIdByUsuarioTipoCategoria(usuarioId, tipoEmailId, categoriaPrimarioId);
        if (credencialId.isEmpty()) {
            throw new CredencialNaoEncontradaException();
        }
        var rows = userCredentialRepository.updateValor(credencialId.get(), usuarioId, emailNormalizado);
        if (rows == 0) {
            throw new CredencialNaoEncontradaException();
        }
        auditoriaRegistroAsyncService.registrarSemDispositivoID(usuarioId, ACAO_ATUALIZAR_EMAIL, "Atualizar email", null, null, true, null, LocalDateTime.now(), httpRequest);
    }

    /** Doc id 21: Remover email (delete físico). */
    @Transactional
    public void removerEmail(UUID usuarioId, UsuarioEmailRequest request, HttpServletRequest httpRequest) {
        var tipoEmailId = tipoCredentialRepository.findIdByDescricao(TIPO_CREDENCIAL_EMAIL);
        if (tipoEmailId == null) {
            throw new com.pontoeletronico.api.exception.TipoCredencialNaoEncontradoException();
        }
        var credencial = userCredentialRepository.findByUsuarioIdAndValorAndTipoCredencialId(
                usuarioId, request.novoEmail().trim().toLowerCase(), tipoEmailId)
                .orElseThrow(CredencialNaoEncontradaException::new);
        var rows = userCredentialRepository.deleteByIdAndUsuarioId(credencial.getId(), usuarioId);
        if (rows == 0) {
            throw new CredencialNaoEncontradaException();
        }
        auditoriaRegistroAsyncService.registrarSemDispositivoID(usuarioId, ACAO_REMOVER_EMAIL, "Remover email", null, null, true, null, LocalDateTime.now(), httpRequest);
    }

    @Transactional
    public void adicionarTelefone(UUID usuarioId, UsuarioTelefoneAdicionarRequest request, HttpServletRequest httpRequest) {
        usersRepository.findByIdQuery(usuarioId).orElseThrow(UsuarioNaoEncontradoException::new);
        if (usuarioTelefoneRepository.existsByCodigoPaisAndDddAndNumero(
                request.codigoPais(), request.ddd(), request.numero()).isPresent()) {
            throw new ConflitoException(MensagemErro.TELEFONE_JA_CADASTRADO.getMensagem());
        }
        usuarioTelefoneRepository.insert(
                UUID.randomUUID(), usuarioId, request.codigoPais(), request.ddd(), request.numero());
        var dataReferencia = LocalDateTime.now();
        auditoriaRegistroAsyncService.registrarSemDispositivoID(usuarioId, ACAO_ADICIONAR_TELEFONE, "Adicionar telefone", null, null, true, null, dataReferencia, httpRequest);
    }

    /** Doc id 23: Deletar telefone. */
    @Transactional
    public void removerTelefone(UUID usuarioId, UUID telefoneId, HttpServletRequest httpRequest) {
        if (usuarioTelefoneRepository.existsByIdAndUsuarioId(telefoneId, usuarioId).isEmpty()) {
            throw new TelefoneNaoEncontradoException();
        }
        var rows = usuarioTelefoneRepository.deleteByIdAndUsuarioId(telefoneId, usuarioId);
        if (rows == 0) {
            throw new TelefoneNaoEncontradoException();
        }
        auditoriaRegistroAsyncService.registrarSemDispositivoID(usuarioId, ACAO_REMOVER_TELEFONE, "Remover telefone", null, null, true, null, LocalDateTime.now(), httpRequest);
    }

    /** Doc id 24: Adicionar novo tipo de login. */
    @Transactional
    public void adicionarCredential(UUID usuarioId, UsuarioCredentialAdicionarRequest request, HttpServletRequest httpRequest) {
        usersRepository.findByIdQuery(usuarioId).orElseThrow(UsuarioNaoEncontradoException::new);
        if (tipoCredentialRepository.findById(request.tipoCredencialId()).isEmpty()) {
            throw new com.pontoeletronico.api.exception.TipoCredencialNaoEncontradoException();
        }
        if (userCredentialRepository.existsByValorAndTipoCredencialId(request.valor().trim(), request.tipoCredencialId()).isPresent()) {
            throw new ConflitoException(MensagemErro.VALOR_CREDENCIAL_JA_CADASTRADO.getMensagem());
        }
        var categoriaPrimarioId = tipoCategoriaCredentialRepository.findIdByDescricao(CATEGORIA_CREDENCIAL_PRIMARIO);
        var categoriaSecundarioId = tipoCategoriaCredentialRepository.findIdByDescricao(CATEGORIA_CREDENCIAL_SECUNDARIO);
        if (categoriaPrimarioId == null || categoriaSecundarioId == null) throw new com.pontoeletronico.api.exception.TipoCredencialNaoEncontradoException();
        var jaExistePrincipal = userCredentialRepository.findCredencialIdByUsuarioTipoCategoria(usuarioId, request.tipoCredencialId(), categoriaPrimarioId).isPresent();
        var categoriaId = jaExistePrincipal ? categoriaSecundarioId : categoriaPrimarioId;
        userCredentialRepository.insert(
                UUID.randomUUID(), usuarioId, request.tipoCredencialId(), categoriaId, request.valor().trim());
        var dataReferencia = LocalDateTime.now();
        auditoriaRegistroAsyncService.registrarSemDispositivoID(usuarioId, ACAO_ADICIONAR_CREDENTIAL, "Adicionar credencial", null, null, true, null, dataReferencia, httpRequest);
    }

    /** Doc id 25: Deletar tipo de login. */
    @Transactional
    public void removerCredential(UUID usuarioId, UUID credentialId, HttpServletRequest httpRequest) {
        var rows = userCredentialRepository.deleteByIdAndUsuarioId(credentialId, usuarioId);
        if (rows == 0) {
            throw new CredencialNaoEncontradaException();
        }
        auditoriaRegistroAsyncService.registrarSemDispositivoID(usuarioId, ACAO_REMOVER_CREDENTIAL, "Remover credencial", null, null, true, null, LocalDateTime.now(), httpRequest);
    }
}
