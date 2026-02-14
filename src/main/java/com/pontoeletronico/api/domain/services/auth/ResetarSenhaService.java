package com.pontoeletronico.api.domain.services.auth;

import com.pontoeletronico.api.domain.services.audit.AuditoriaRegistroAsyncService;
import jakarta.servlet.http.HttpServletRequest;
import com.pontoeletronico.api.exception.TokenRecuperacaoInvalidoException;
import com.pontoeletronico.api.exception.TipoTokenRecuperacaoNaoEncontradoException;
import com.pontoeletronico.api.infrastructure.input.dto.auth.ResetarSenhaRequest;
import com.pontoeletronico.api.infrastructure.output.repository.auth.CredencialTokenRecuperacaoRepository;
import com.pontoeletronico.api.infrastructure.output.repository.auth.TipoTokenRecuperacaoRepository;
import com.pontoeletronico.api.infrastructure.output.repository.auth.UserPasswordRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ResetarSenhaService {

    private static final String ACAO_RESETAR_SENHA = "RESETAR_SENHA";

    private final CredencialTokenRecuperacaoRepository credencialTokenRecuperacaoRepository;
    private final TipoTokenRecuperacaoRepository tipoTokenRecuperacaoRepository;
    private final UserPasswordRepository userPasswordRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditoriaRegistroAsyncService auditoriaRegistroAsyncService;

    public ResetarSenhaService(CredencialTokenRecuperacaoRepository credencialTokenRecuperacaoRepository,
                               TipoTokenRecuperacaoRepository tipoTokenRecuperacaoRepository,
                               UserPasswordRepository userPasswordRepository,
                               PasswordEncoder passwordEncoder,
                               AuditoriaRegistroAsyncService auditoriaRegistroAsyncService) {
        this.credencialTokenRecuperacaoRepository = credencialTokenRecuperacaoRepository;
        this.tipoTokenRecuperacaoRepository = tipoTokenRecuperacaoRepository;
        this.userPasswordRepository = userPasswordRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditoriaRegistroAsyncService = auditoriaRegistroAsyncService;
    }

    /** Doc id 4: Resetar senha com token temporário. */
    @Transactional
    public void resetar(ResetarSenhaRequest request, HttpServletRequest httpRequest) {
        var tipoTokenResetId = tipoTokenRecuperacaoRepository.findIdByDescricao("TOKEN_RESET");
        if (tipoTokenResetId == null) {
            throw new TipoTokenRecuperacaoNaoEncontradoException();
        }
        var dataReferencia = LocalDateTime.now();
        var registro = credencialTokenRecuperacaoRepository.findByTokenAndTipoAndAtivoAndNaoExpirado(
                        request.token().trim(), tipoTokenResetId, dataReferencia)
                .orElseThrow(TokenRecuperacaoInvalidoException::new);

        var usuarioId = registro.getUsuarioId();
        var senhaHash = passwordEncoder.encode(request.senhaNova());
        userPasswordRepository.desativarByUsuarioId(usuarioId, dataReferencia);
        userPasswordRepository.insert(UUID.randomUUID(), usuarioId, senhaHash, dataReferencia);
        credencialTokenRecuperacaoRepository.desativar(registro.getId(), dataReferencia);
        registrarAuditoriaResetarSenha(registro.getUsuarioId(), true, null, dataReferencia, httpRequest);
    }

    private void registrarAuditoriaResetarSenha(UUID usuarioId, boolean sucesso, String mensagemErro, LocalDateTime dataReferencia, HttpServletRequest httpRequest) {
        auditoriaRegistroAsyncService.registrarSemDispositivoID(usuarioId, ACAO_RESETAR_SENHA, "Reset de senha", null, null, sucesso, mensagemErro, dataReferencia, httpRequest);
    }
}
