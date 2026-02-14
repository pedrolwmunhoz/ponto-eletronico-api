package com.pontoeletronico.api.domain.services.auditoria;

import com.pontoeletronico.api.domain.entity.audit.AuditoriaLog;
import com.pontoeletronico.api.exception.RegistroNaoEncontradoException;
import com.pontoeletronico.api.infrastructure.input.dto.auditoria.AuditoriaDetalheResponse;
import com.pontoeletronico.api.infrastructure.input.dto.auditoria.AuditoriaItemResponse;
import com.pontoeletronico.api.infrastructure.input.dto.auditoria.AuditoriaListagemResponse;
import com.pontoeletronico.api.infrastructure.output.repository.audit.AuditoriaLogRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuditoriaService {

    private final AuditoriaLogRepository auditoriaLogRepository;

    public AuditoriaService(AuditoriaLogRepository auditoriaLogRepository) {
        this.auditoriaLogRepository = auditoriaLogRepository;
    }

    /** Doc id 49: Listar log de auditoria. */
    public AuditoriaListagemResponse listarPorEmpresa(UUID empresaId, int page, int size) {
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
        return new AuditoriaListagemResponse(items, total, Math.max(0, page), limit);
    }

    /** Doc id 50: Detalhar log de auditoria. */
    public AuditoriaDetalheResponse detalhar(UUID empresaId, UUID logId) {
        if (auditoriaLogRepository.existsByIdAndEmpresaId(logId, empresaId).isEmpty()) {
            throw new RegistroNaoEncontradoException("Log de auditoria não encontrado");
        }
        AuditoriaLog log = auditoriaLogRepository.findById(logId)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Log de auditoria não encontrado"));
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
