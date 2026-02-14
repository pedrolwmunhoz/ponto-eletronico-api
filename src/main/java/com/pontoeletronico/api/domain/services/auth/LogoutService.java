package com.pontoeletronico.api.domain.services.auth;

import com.pontoeletronico.api.domain.services.audit.AuditoriaRegistroAsyncService;
import com.pontoeletronico.api.exception.AutorizacaoInvalidaException;
import com.pontoeletronico.api.infrastructure.output.repository.auth.SessaoAtivaRepository;
import com.pontoeletronico.api.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class LogoutService {

    private static final String ACAO_LOGOUT = "LOGOUT";

    private final SessaoAtivaRepository sessaoAtivaRepository;
    private final AuditoriaRegistroAsyncService auditoriaRegistroAsyncService;
    private final JwtUtil jwtUtil;

    public LogoutService(SessaoAtivaRepository sessaoAtivaRepository,
                         AuditoriaRegistroAsyncService auditoriaRegistroAsyncService,
                         JwtUtil jwtUtil) {
        this.sessaoAtivaRepository = sessaoAtivaRepository;
        this.auditoriaRegistroAsyncService = auditoriaRegistroAsyncService;
        this.jwtUtil = jwtUtil;
    }

    /** Doc id 6: Logout (invalida sessão). */
    @Transactional
    public void logout(String authorizationHeader, HttpServletRequest httpRequest) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new AutorizacaoInvalidaException();
        }
        var usuarioId = jwtUtil.extractUserIdFromToken(authorizationHeader);
        var dataDesativacao = LocalDateTime.now();
        sessaoAtivaRepository.desativarPorUsuarioId(usuarioId, dataDesativacao);
        registrarAuditoriaLogout(usuarioId, true, null, dataDesativacao, httpRequest);
    }

    private void registrarAuditoriaLogout(UUID usuarioId, boolean sucesso, String mensagemErro, LocalDateTime dataReferencia, HttpServletRequest httpRequest) {
        auditoriaRegistroAsyncService.registrarSemDispositivoID(usuarioId, ACAO_LOGOUT, "Logout", null, null, sucesso, mensagemErro, dataReferencia, httpRequest);
    }
}
