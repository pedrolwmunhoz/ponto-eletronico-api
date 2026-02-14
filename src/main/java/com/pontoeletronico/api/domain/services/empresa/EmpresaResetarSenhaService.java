package com.pontoeletronico.api.domain.services.empresa;

import com.pontoeletronico.api.domain.enums.MensagemErro;
import com.pontoeletronico.api.domain.services.audit.AuditoriaRegistroAsyncService;
import com.pontoeletronico.api.exception.ApiException;
import com.pontoeletronico.api.exception.CredencialNaoEncontradaException;
import com.pontoeletronico.api.infrastructure.input.dto.empresa.EmpresaResetarSenhaRequest;
import com.pontoeletronico.api.infrastructure.output.repository.auth.UserPasswordRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class EmpresaResetarSenhaService {

    private static final String ACAO_RESETAR_SENHA_EMPRESA = "RESETAR_SENHA_EMPRESA";

    private final UserPasswordRepository userPasswordRepository;
    private final AuditoriaRegistroAsyncService auditoriaRegistroAsyncService;
    private final PasswordEncoder passwordEncoder;

    public EmpresaResetarSenhaService(UserPasswordRepository userPasswordRepository,
                                     AuditoriaRegistroAsyncService auditoriaRegistroAsyncService,
                                     PasswordEncoder passwordEncoder) {
        this.userPasswordRepository = userPasswordRepository;
        this.auditoriaRegistroAsyncService = auditoriaRegistroAsyncService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    /** Doc id 10: Resetar senha da empresa. */
    public void resetar(UUID empresaId, EmpresaResetarSenhaRequest request, HttpServletRequest httpRequest) {
        var senhaAtualOpt = userPasswordRepository.findByUsuarioIdAndAtivo(empresaId);
        var now = LocalDateTime.now();
        if (senhaAtualOpt.isEmpty()) {
            auditoriaRegistroAsyncService.registrarSemDispositivoID(empresaId, ACAO_RESETAR_SENHA_EMPRESA, "Resetar senha da empresa", null, null, false, MensagemErro.CREDENCIAL_NAO_ENCONTRADA.getMensagem(), now, httpRequest);
            throw new CredencialNaoEncontradaException();
        }
        var senhaAtual = senhaAtualOpt.get();

        if (!passwordEncoder.matches(request.senhaAntiga(), senhaAtual.getSenhaHash())) {
            auditoriaRegistroAsyncService.registrarSemDispositivoID(empresaId, ACAO_RESETAR_SENHA_EMPRESA, "Resetar senha da empresa", null, null, false, MensagemErro.SENHA_ANTIGA_INCORRETA.getMensagem(), now, httpRequest);
            throw new ApiException(MensagemErro.SENHA_ANTIGA_INCORRETA.getMensagem(), HttpStatus.BAD_REQUEST);
        }

        userPasswordRepository.desativarByUsuarioId(empresaId, now);
        userPasswordRepository.insert(UUID.randomUUID(), empresaId, passwordEncoder.encode(request.senhaNova()), now);
        auditoriaRegistroAsyncService.registrarSemDispositivoID(empresaId, ACAO_RESETAR_SENHA_EMPRESA, "Resetar senha da empresa", null, null, true, null, now, httpRequest);
    }
}
