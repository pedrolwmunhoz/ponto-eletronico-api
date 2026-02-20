package com.pontoeletronico.api.domain.services.empresa;

import com.pontoeletronico.api.domain.services.audit.AuditoriaRegistroAsyncService;
import com.pontoeletronico.api.infrastructure.input.dto.empresa.AtividadeRecenteResponse;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.AtividadeRecenteRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AtividadeRecenteConsultaService {

    private static final String ACAO_ATIVIDADES_RECENTES = "ACESSO_ATIVIDADES_RECENTES";

    private final AtividadeRecenteRepository atividadeRecenteRepository;
    private final AuditoriaRegistroAsyncService auditoriaRegistroAsyncService;

    public AtividadeRecenteConsultaService(AtividadeRecenteRepository atividadeRecenteRepository,
                                           AuditoriaRegistroAsyncService auditoriaRegistroAsyncService) {
        this.atividadeRecenteRepository = atividadeRecenteRepository;
        this.auditoriaRegistroAsyncService = auditoriaRegistroAsyncService;
    }

    private static final int SIZE_ATIVIDADES_RECENTES = 4;

    /**
     * Lista no máximo 4 atividades recentes (últimos registros de ponto da empresa).
     * Size aplicado direto na query (LIMIT) no repository.
     */
    public List<AtividadeRecenteResponse> listar(UUID empresaId, HttpServletRequest httpRequest) {
        List<Object[]> rows = atividadeRecenteRepository.findByEmpresaIdOrderByCreatedAtDesc(empresaId, SIZE_ATIVIDADES_RECENTES);
        List<AtividadeRecenteResponse> list = rows.stream()
                .map(row -> new AtividadeRecenteResponse(
                        (String) row[0],
                        (LocalDateTime) row[1]
                ))
                .toList();
        auditoriaRegistroAsyncService.registrarSemDispositivoID(empresaId, ACAO_ATIVIDADES_RECENTES, "Consulta atividades recentes (dashboard)", null, null, true, null, LocalDateTime.now(), httpRequest);
        return list;
    }
}
