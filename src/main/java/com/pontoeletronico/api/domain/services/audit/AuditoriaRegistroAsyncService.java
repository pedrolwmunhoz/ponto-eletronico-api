package com.pontoeletronico.api.domain.services.audit;

import com.pontoeletronico.api.domain.services.auth.DispositivoService;
import com.pontoeletronico.api.infrastructure.output.repository.audit.AuditoriaLogRepository;
import com.pontoeletronico.api.infrastructure.output.repository.auth.AuthLoginProjection;
import com.pontoeletronico.api.util.HttpRequestUtils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Service async para registro de auditoria. Tem acesso a obterOuCriar dispositivo.
 */
@Service
public class AuditoriaRegistroAsyncService {

    private final AuditoriaLogRepository auditoriaLogRepository;
    private final DispositivoService dispositivoService;

    public AuditoriaRegistroAsyncService(AuditoriaLogRepository auditoriaLogRepository,
                                         DispositivoService dispositivoService) {
        this.auditoriaLogRepository = auditoriaLogRepository;
        this.dispositivoService = dispositivoService;
    }

    @Async
    @Transactional
    public CompletableFuture<Void> registrarSemDispositivoID(UUID usuarioId, String acao, String descricao, String dadosAntigos, String dadosNovos, boolean sucesso, String mensagemErro, LocalDateTime createdAt, HttpServletRequest httpRequest) {
        String ipAddress = httpRequest != null ? HttpRequestUtils.obterIpAddress(httpRequest) : null;
        String userAgent = httpRequest != null ? HttpRequestUtils.obterUserAgent(httpRequest) : null;
        UUID dispositivoId = dispositivoService.obterOuCriar(usuarioId, ipAddress, userAgent);


        auditoriaLogRepository.insert(
                UUID.randomUUID(), usuarioId, acao, descricao, dadosAntigos, dadosNovos, dispositivoId, sucesso, mensagemErro, createdAt);
        return CompletableFuture.completedFuture(null);
    }

    @Transactional
    public CompletableFuture<Void> registrarComDispositivoID(UUID usuarioId,UUID dispositivoId, String acao, String descricao, String dadosAntigos, String dadosNovos, boolean sucesso, String mensagemErro, LocalDateTime createdAt, HttpServletRequest httpRequest) {
        auditoriaLogRepository.insert(
            UUID.randomUUID(), usuarioId, acao, descricao, dadosAntigos, dadosNovos, dispositivoId, sucesso, mensagemErro, createdAt);
    return CompletableFuture.completedFuture(null);
    }
}
