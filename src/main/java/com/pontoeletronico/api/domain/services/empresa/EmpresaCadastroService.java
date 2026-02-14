package com.pontoeletronico.api.domain.services.empresa;

import com.pontoeletronico.api.domain.enums.MensagemErro;
import com.pontoeletronico.api.domain.services.audit.AuditoriaRegistroAsyncService;
import com.pontoeletronico.api.exception.ConflitoException;
import com.pontoeletronico.api.exception.TipoCredencialNaoEncontradoException;
import com.pontoeletronico.api.exception.TipoUsuarioNaoEncontradoException;
import com.pontoeletronico.api.infrastructure.input.dto.empresa.EmpresaCreateRequest;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.EmpresaDadosFiscalRepository;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.EmpresaEnderecoRepository;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.TipoUsuarioRepository;
import com.pontoeletronico.api.infrastructure.output.repository.usuario.UsersRepository;
import com.pontoeletronico.api.infrastructure.output.repository.usuario.UsuarioTelefoneRepository;
import com.pontoeletronico.api.infrastructure.output.repository.auth.TipoCategoriaCredentialRepository;
import com.pontoeletronico.api.infrastructure.output.repository.auth.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class EmpresaCadastroService {

    private static final String TIPO_EMPRESA = "EMPRESA";
    private static final String TIPO_CREDENCIAL_EMAIL = "EMAIL";
    private static final String CATEGORIA_CREDENCIAL_PRIMARIO = "PRIMARIO";
    private static final String ACAO_CADASTRO_EMPRESA = "CADASTRO_EMPRESA";

    private final UsersRepository usersRepository;
    private final TipoUsuarioRepository tipoUsuarioRepository;
    private final TipoCredentialRepository tipoCredentialRepository;
    private final UserCredentialRepository userCredentialRepository;
    private final UserPasswordRepository userPasswordRepository;
    private final TipoCategoriaCredentialRepository tipoCategoriaCredentialRepository;
    private final EmpresaDadosFiscalRepository empresaDadosFiscalRepository;
    private final EmpresaEnderecoRepository empresaEnderecoRepository;
    private final UsuarioTelefoneRepository usuarioTelefoneRepository;
    private final AuditoriaRegistroAsyncService auditoriaRegistroAsyncService;
    private final PasswordEncoder passwordEncoder;

    public EmpresaCadastroService(UsersRepository usersRepository,
                          TipoUsuarioRepository tipoUsuarioRepository,
                          TipoCredentialRepository tipoCredentialRepository,
                          UserCredentialRepository userCredentialRepository,
                          UserPasswordRepository userPasswordRepository,
                          TipoCategoriaCredentialRepository tipoCategoriaCredentialRepository,
                          EmpresaDadosFiscalRepository empresaDadosFiscalRepository,
                          EmpresaEnderecoRepository empresaEnderecoRepository,
                          UsuarioTelefoneRepository usuarioTelefoneRepository,
                          AuditoriaRegistroAsyncService auditoriaRegistroAsyncService,
                          PasswordEncoder passwordEncoder) {
        this.usersRepository = usersRepository;
        this.tipoUsuarioRepository = tipoUsuarioRepository;
        this.tipoCredentialRepository = tipoCredentialRepository;
        this.userCredentialRepository = userCredentialRepository;
        this.userPasswordRepository = userPasswordRepository;
        this.tipoCategoriaCredentialRepository = tipoCategoriaCredentialRepository;
        this.empresaDadosFiscalRepository = empresaDadosFiscalRepository;
        this.empresaEnderecoRepository = empresaEnderecoRepository;
        this.usuarioTelefoneRepository = usuarioTelefoneRepository;
        this.auditoriaRegistroAsyncService = auditoriaRegistroAsyncService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    /** Doc id 7: Cadastro de empresa. */
    public UUID criar(EmpresaCreateRequest request, HttpServletRequest httpRequest) {
        var emailNormalizado = request.email().trim().toLowerCase(); // ex: "Contato@Empresa.com" -> "contato@empresa.com"
        var cnpjNormalizado = request.cnpj().replaceAll("\\D", "");   // ex: "12.345.678/0001-90" -> "12345678000190"
        var cepNormalizado = request.empresaEndereco().cep().replaceAll("\\D", ""); // ex: "01310-100" -> "01310100"

        if (usersRepository.existsByUsername(request.username()).isPresent()) {
            throw new ConflitoException(MensagemErro.USERNAME_JA_CADASTRADO.getMensagem());
        }
        var tipoEmailId = tipoCredentialRepository.findIdByDescricao(TIPO_CREDENCIAL_EMAIL);
        if (tipoEmailId == null) {
            throw new TipoCredencialNaoEncontradoException();
        }
        if (userCredentialRepository.existsByValorAndTipoCredencialId(emailNormalizado, tipoEmailId).isPresent()) {
            throw new ConflitoException(MensagemErro.EMAIL_JA_CADASTRADO.getMensagem());
        }
        if (empresaDadosFiscalRepository.existsByCnpj(cnpjNormalizado).isPresent()) {
            throw new ConflitoException(MensagemErro.CNPJ_JA_CADASTRADO.getMensagem());
        }

        var tipoEmpresaId = tipoUsuarioRepository.findIdByDescricao(TIPO_EMPRESA);
        if (tipoEmpresaId == null) {
            throw new TipoUsuarioNaoEncontradoException();
        }

        var dataCriacao = LocalDateTime.now();
        var empresaId = UUID.randomUUID();
        usersRepository.insert(empresaId, request.username(), tipoEmpresaId, dataCriacao);

        var credencialId = UUID.randomUUID();
        var categoriaPrimarioId = tipoCategoriaCredentialRepository.findIdByDescricao(CATEGORIA_CREDENCIAL_PRIMARIO);
        if (categoriaPrimarioId == null) throw new TipoCredencialNaoEncontradoException();
        userCredentialRepository.insert(credencialId, empresaId, tipoEmailId, categoriaPrimarioId, emailNormalizado);
        userPasswordRepository.insert(UUID.randomUUID(), empresaId, passwordEncoder.encode(request.senha()), dataCriacao);

        empresaDadosFiscalRepository.insert(UUID.randomUUID(), empresaId, request.razaoSocial(), cnpjNormalizado, dataCriacao);
        empresaEnderecoRepository.insert(
                UUID.randomUUID(), empresaId,
                request.empresaEndereco().rua(), request.empresaEndereco().numero(),
                request.empresaEndereco().complemento(), request.empresaEndereco().bairro(),
                request.empresaEndereco().cidade(), request.empresaEndereco().uf().toUpperCase(), cepNormalizado,
                dataCriacao);
        usuarioTelefoneRepository.insert(
                UUID.randomUUID(), empresaId,
                request.usuarioTelefone().codigoPais(), request.usuarioTelefone().ddd(), request.usuarioTelefone().numero());

        registrarAuditoriaEmpresa(empresaId, true, null, dataCriacao, httpRequest);
        return empresaId;
    }

    /** Chamado apenas pelo EmpresaService (cadastro de empresa). */
    private void registrarAuditoriaEmpresa(UUID empresaId, boolean sucesso, String mensagemErro, LocalDateTime dataCriacao, HttpServletRequest httpRequest) {
        auditoriaRegistroAsyncService.registrarSemDispositivoID(empresaId, ACAO_CADASTRO_EMPRESA, "Cadastro de empresa", null, null, sucesso, mensagemErro, dataCriacao, httpRequest);
    }
}
