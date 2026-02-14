package com.pontoeletronico.api.domain.services.auth;

import com.pontoeletronico.api.domain.services.audit.AuditoriaRegistroAsyncService;
import jakarta.servlet.http.HttpServletRequest;
import com.pontoeletronico.api.exception.TipoCredencialNaoEncontradoException;
import com.pontoeletronico.api.exception.TipoTokenRecuperacaoNaoEncontradoException;
import com.pontoeletronico.api.infrastructure.input.dto.auth.RecuperarSenhaRequest;
import com.pontoeletronico.api.infrastructure.output.repository.auth.CredencialTokenRecuperacaoRepository;
import com.pontoeletronico.api.infrastructure.output.repository.auth.TipoCredentialRepository;
import com.pontoeletronico.api.infrastructure.output.repository.auth.TipoTokenRecuperacaoRepository;
import com.pontoeletronico.api.infrastructure.output.repository.auth.UserCredentialRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class RecuperarSenhaService {

    private static final String ACAO_RECUPERAR_SENHA = "RECUPERAR_SENHA";
    private static final int EXPIRACAO_MINUTOS = 15;
    private static final String DIGITOS = "0123456789";

    private final UserCredentialRepository userCredentialRepository;
    private final TipoCredentialRepository tipoCredentialRepository;
    private final TipoTokenRecuperacaoRepository tipoTokenRecuperacaoRepository;
    private final CredencialTokenRecuperacaoRepository credencialTokenRecuperacaoRepository;
    private final AuditoriaRegistroAsyncService auditoriaRegistroAsyncService;

    public RecuperarSenhaService(UserCredentialRepository userCredentialRepository,
                                 TipoCredentialRepository tipoCredentialRepository,
                                 TipoTokenRecuperacaoRepository tipoTokenRecuperacaoRepository,
                                 CredencialTokenRecuperacaoRepository credencialTokenRecuperacaoRepository,
                                 AuditoriaRegistroAsyncService auditoriaRegistroAsyncService) {
        this.userCredentialRepository = userCredentialRepository;
        this.tipoCredentialRepository = tipoCredentialRepository;
        this.tipoTokenRecuperacaoRepository = tipoTokenRecuperacaoRepository;
        this.credencialTokenRecuperacaoRepository = credencialTokenRecuperacaoRepository;
        this.auditoriaRegistroAsyncService = auditoriaRegistroAsyncService;
    }

    /** Doc id 2: Gerar código de recuperação de senha. */
    @Transactional
    public void recuperar(RecuperarSenhaRequest request, HttpServletRequest httpRequest) {
        var tipoId = tipoCredentialRepository.findIdByDescricao("EMAIL");
        if (tipoId == null) {
            throw new TipoCredencialNaoEncontradoException();
        }
        var email = request.email().trim().toLowerCase();
        var credencial = userCredentialRepository.findByValorAndTipoCredencialIdAndAtivo(email, tipoId).orElse(null);
        if (credencial == null) {
            return;
        }
        var tipoTokenId = tipoTokenRecuperacaoRepository.findIdByDescricao("CODIGO_EMAIL");
        if (tipoTokenId == null) {
            throw new TipoTokenRecuperacaoNaoEncontradoException();
        }
        var dataCriacao = LocalDateTime.now();
        var expiracao = dataCriacao.plusMinutes(EXPIRACAO_MINUTOS);
        var codigo = gerarCodigo6Digitos();
        credencialTokenRecuperacaoRepository.insert(
                UUID.randomUUID(), credencial.getUsuarioId(), tipoTokenId, codigo, expiracao, dataCriacao);

        registrarAuditoriaRecuperarSenha(credencial.getUsuarioId(), true, null, dataCriacao, httpRequest);
        // TODO: enviar codigo por email
    }

    private void registrarAuditoriaRecuperarSenha(UUID usuarioId, boolean sucesso, String mensagemErro, LocalDateTime dataCriacao, HttpServletRequest httpRequest) {
        auditoriaRegistroAsyncService.registrarSemDispositivoID(usuarioId, ACAO_RECUPERAR_SENHA, "Solicitação de recuperação de senha", null, null, sucesso, mensagemErro, dataCriacao, httpRequest);
    }

    private String gerarCodigo6Digitos() {
        var random = new SecureRandom();
        var sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            sb.append(DIGITOS.charAt(random.nextInt(DIGITOS.length())));
        }
        return sb.toString();
    }
}
