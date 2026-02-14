package com.pontoeletronico.api.domain.services.auth;

import com.pontoeletronico.api.domain.services.audit.AuditoriaRegistroAsyncService;
import com.pontoeletronico.api.infrastructure.output.repository.auth.AuthLoginProjection;
import com.pontoeletronico.api.infrastructure.output.repository.auth.SessaoAtivaRepository;
import com.pontoeletronico.api.util.HttpRequestUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Registro async de login: dispositivo, sessao, historico e auditoria.
 */
@Service
public class LoginRegistroAsyncService {

    private static final String ACAO_LOGIN = "LOGIN";

    private final AuditoriaRegistroAsyncService auditoriaRegistroAsyncService;
    private final DispositivoService dispositivoService;
    private final SessaoAtivaRepository sessaoAtivaRepository;
    private final HistoricoLoginRegistroService historicoLoginRegistroService;

    public LoginRegistroAsyncService(AuditoriaRegistroAsyncService auditoriaRegistroAsyncService,
                                     DispositivoService dispositivoService,
                                     SessaoAtivaRepository sessaoAtivaRepository,
                                     HistoricoLoginRegistroService historicoLoginRegistroService) {
        this.auditoriaRegistroAsyncService = auditoriaRegistroAsyncService;
        this.dispositivoService = dispositivoService;
        this.sessaoAtivaRepository = sessaoAtivaRepository;
        this.historicoLoginRegistroService = historicoLoginRegistroService;
    }

    @Async
    @Transactional
    public CompletableFuture<Void> registrar(AuthLoginProjection credencial, HttpServletRequest httpRequest,
                                             String refreshToken, LocalDateTime refreshExpiresAt,
                                             boolean sucesso, String mensagemErro, LocalDateTime dataReferencia) {
        String ipAddress = httpRequest != null ? HttpRequestUtils.obterIpAddress(httpRequest) : null;
        String userAgent = httpRequest != null ? HttpRequestUtils.obterUserAgent(httpRequest) : null;
        UUID dispositivoId = dispositivoService.obterOuCriar(credencial.getUsuarioId(), ipAddress, userAgent);

        if (sucesso && refreshToken != null && refreshExpiresAt != null) {
            sessaoAtivaRepository.insert(
                    UUID.randomUUID(), credencial.getUsuarioId(), credencial.getCredencialId(), refreshToken,
                    dispositivoId, true, refreshExpiresAt, null, dataReferencia);
        }

        if (credencial.getCredencialId() != null) {
            historicoLoginRegistroService.registrar(
                    credencial.getUsuarioId(), credencial.getCredencialId(), dataReferencia, dispositivoId, sucesso, dataReferencia);
        }

        auditoriaRegistroAsyncService.registrarComDispositivoID(
                credencial.getUsuarioId(), dispositivoId, ACAO_LOGIN, "Tentativa de login", null, null, sucesso, mensagemErro, dataReferencia, httpRequest).join();

        return CompletableFuture.completedFuture(null);
    }
}
