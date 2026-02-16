package com.pontoeletronico.api.domain.services.auditoria;

import com.pontoeletronico.api.domain.entity.audit.AuditoriaLog;
import com.pontoeletronico.api.domain.services.audit.AuditoriaRegistroAsyncService;
import com.pontoeletronico.api.exception.RegistroNaoEncontradoException;
import com.pontoeletronico.api.infrastructure.input.dto.auditoria.AuditoriaDetalheResponse;
import com.pontoeletronico.api.infrastructure.input.dto.auditoria.AuditoriaItemResponse;
import com.pontoeletronico.api.infrastructure.input.dto.auditoria.AuditoriaListagemResponse;
import com.pontoeletronico.api.infrastructure.output.repository.audit.AuditoriaLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuditoriaService {

    private static final String ACAO_LISTAGEM_AUDITORIA = "ACESSO_LISTAGEM_AUDITORIA";
    private static final String ACAO_DETALHE_AUDITORIA = "ACESSO_DETALHE_AUDITORIA";

    private final AuditoriaLogRepository auditoriaLogRepository;
    private final AuditoriaRegistroAsyncService auditoriaRegistroAsyncService;

    public AuditoriaService(AuditoriaLogRepository auditoriaLogRepository,
                            AuditoriaRegistroAsyncService auditoriaRegistroAsyncService) {
        this.auditoriaLogRepository = auditoriaLogRepository;
        this.auditoriaRegistroAsyncService = auditoriaRegistroAsyncService;
    }

    /** Doc id 49: Listar log de auditoria. */
    public AuditoriaListagemResponse listarPorEmpresa(UUID empresaId, int page, int size, HttpServletRequest httpRequest) {
        int limit = Math.max(1, Math.min(size, 100));
        int offset = Math.max(0, page) * limit;
        var list = auditoriaLogRepository.findPageByEmpresaId(empresaId, limit, offset);
        long total = auditoriaLogRepository.countByEmpresaId(empresaId);
        var items = list.stream()
                .map(p -> new AuditoriaItemResponse(
                        p.getAcao(),
                        p.getDescricao(),
                        p.getData(),
                        p.getNomeUsuario(),
                        p.getSucesso()))
                .toList();
        auditoriaRegistroAsyncService.registrarSemDispositivoID(empresaId, ACAO_LISTAGEM_AUDITORIA, "Listagem de auditoria", null, null, true, null, LocalDateTime.now(), httpRequest);
        return new AuditoriaListagemResponse(items, total, Math.max(0, page), limit);
    }

    /** Doc id 50: Detalhar log de auditoria. */
    public AuditoriaDetalheResponse detalhar(UUID empresaId, UUID logId, HttpServletRequest httpRequest) {
        if (auditoriaLogRepository.existsByIdAndEmpresaId(logId, empresaId).isEmpty()) {
            throw new RegistroNaoEncontradoException("Log de auditoria não encontrado");
        }
        AuditoriaLog log = auditoriaLogRepository.findById(logId)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Log de auditoria não encontrado"));
        auditoriaRegistroAsyncService.registrarSemDispositivoID(empresaId, ACAO_DETALHE_AUDITORIA, "Detalhe de log de auditoria", null, null, true, null, LocalDateTime.now(), httpRequest);
        return new AuditoriaDetalheResponse(
                log.getUsuarioId(),
                log.getAcao(),
                log.getDescricao(),
                log.getDadosAntigos(),
                log.getDadosNovos(),
                log.getDispositivoId(),
                log.getSucesso(),
                log.getMensagemErro(),
                log.getCreatedAt()
        );
    }
}
