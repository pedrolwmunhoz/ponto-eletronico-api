package com.pontoeletronico.api.domain.services.admin;

import com.pontoeletronico.api.domain.enums.MensagemErro;
import jakarta.servlet.http.HttpServletRequest;
import com.pontoeletronico.api.domain.services.audit.AuditoriaRegistroAsyncService;
import com.pontoeletronico.api.exception.ConflitoException;
import com.pontoeletronico.api.exception.TipoCredencialNaoEncontradoException;
import com.pontoeletronico.api.exception.TipoUsuarioNaoEncontradoException;
import com.pontoeletronico.api.infrastructure.input.dto.admin.AdminCriarRequest;
import com.pontoeletronico.api.infrastructure.output.repository.auth.TipoCategoriaCredentialRepository;
import com.pontoeletronico.api.infrastructure.output.repository.auth.TipoCredentialRepository;
import com.pontoeletronico.api.infrastructure.output.repository.auth.UserCredentialRepository;
import com.pontoeletronico.api.infrastructure.output.repository.auth.UserPasswordRepository;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.TipoUsuarioRepository;
import com.pontoeletronico.api.infrastructure.output.repository.usuario.UsersRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AdminCriarService {

    private static final String TIPO_ADMIN = "ADMIN";
    private static final String ACAO_CRIAR_ADMIN = "CRIAR_ADMIN";

    private final TipoUsuarioRepository tipoUsuarioRepository;
    private final TipoCredentialRepository tipoCredentialRepository;
    private final TipoCategoriaCredentialRepository tipoCategoriaCredentialRepository;
    private final UsersRepository usersRepository;
    private final UserCredentialRepository userCredentialRepository;
    private final UserPasswordRepository userPasswordRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditoriaRegistroAsyncService auditoriaRegistroAsyncService;

    public AdminCriarService(TipoUsuarioRepository tipoUsuarioRepository,
                             TipoCredentialRepository tipoCredentialRepository,
                             TipoCategoriaCredentialRepository tipoCategoriaCredentialRepository,
                             UsersRepository usersRepository,
                             UserCredentialRepository userCredentialRepository,
                             UserPasswordRepository userPasswordRepository,
                             PasswordEncoder passwordEncoder,
                             AuditoriaRegistroAsyncService auditoriaRegistroAsyncService) {
        this.tipoUsuarioRepository = tipoUsuarioRepository;
        this.tipoCredentialRepository = tipoCredentialRepository;
        this.tipoCategoriaCredentialRepository = tipoCategoriaCredentialRepository;
        this.usersRepository = usersRepository;
        this.userCredentialRepository = userCredentialRepository;
        this.userPasswordRepository = userPasswordRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditoriaRegistroAsyncService = auditoriaRegistroAsyncService;
    }

    /** Doc id 51: Criar administrador. */
    @Transactional
    public UUID criar(AdminCriarRequest request, HttpServletRequest httpRequest) {
        var valorNormalizado = normalizarValor(request.valor(), request.tipoCredencial().name());
        if (usersRepository.existsByUsername(request.username()).isPresent()) {
            throw new ConflitoException(MensagemErro.USERNAME_JA_CADASTRADO.getMensagem());
        }
        var tipoCredencialId = tipoCredentialRepository.findIdByDescricao(request.tipoCredencial().name());
        if (tipoCredencialId == null) {
            throw new TipoCredencialNaoEncontradoException();
        }
        if (userCredentialRepository.existsByValorAndTipoCredencialId(valorNormalizado, tipoCredencialId).isPresent()) {
            throw new ConflitoException(MensagemErro.VALOR_CREDENCIAL_JA_CADASTRADO.getMensagem());
        }

        var tipoAdminId = tipoUsuarioRepository.findIdByDescricao(TIPO_ADMIN);
        if (tipoAdminId == null) {
            throw new TipoUsuarioNaoEncontradoException();
        }

        var dataCriacao = LocalDateTime.now();
        var adminId = UUID.randomUUID();
        usersRepository.insert(adminId, request.username(), tipoAdminId, dataCriacao);

        var credencialId = UUID.randomUUID();
        var categoriaPrimarioId = tipoCategoriaCredentialRepository.findIdByDescricao("PRIMARIO");
        if (categoriaPrimarioId == null) throw new TipoCredencialNaoEncontradoException();
        userCredentialRepository.insert(credencialId, adminId, tipoCredencialId, categoriaPrimarioId, valorNormalizado);
        userPasswordRepository.insert(UUID.randomUUID(), adminId, passwordEncoder.encode(request.senha()), dataCriacao);

        registrarAuditoriaCriarAdmin(adminId, true, null, dataCriacao, httpRequest);
        return adminId;
    }

    private void registrarAuditoriaCriarAdmin(UUID adminId, boolean sucesso, String mensagemErro, LocalDateTime dataCriacao, HttpServletRequest httpRequest) {
        auditoriaRegistroAsyncService.registrarSemDispositivoID(adminId, ACAO_CRIAR_ADMIN, "Cadastro de administrador", null, null, sucesso, mensagemErro, dataCriacao, httpRequest);
    }

    private String normalizarValor(String valor, String tipoCredencial) {
        var valorNormalizado = valor.trim();
        return "EMAIL".equals(tipoCredencial) ? valorNormalizado.toLowerCase() : valorNormalizado;
    }
}
