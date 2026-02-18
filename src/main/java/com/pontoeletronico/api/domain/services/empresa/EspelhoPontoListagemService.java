package com.pontoeletronico.api.domain.services.empresa;

import com.pontoeletronico.api.domain.services.audit.AuditoriaRegistroAsyncService;
import com.pontoeletronico.api.infrastructure.input.dto.common.Paginacao;
import com.pontoeletronico.api.infrastructure.input.dto.empresa.EspelhoPontoListagemPageResponse;
import com.pontoeletronico.api.infrastructure.input.dto.empresa.EspelhoPontoListagemResponse;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.EspelhoPontoListagemProjection;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.EspelhoPontoListagemRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EspelhoPontoListagemService {

    private static final String ACAO_LISTAGEM_ESPELHO_PONTO = "ACESSO_LISTAGEM_ESPELHO_PONTO";

    private final EspelhoPontoListagemRepository espelhoPontoListagemRepository;
    private final AuditoriaRegistroAsyncService auditoriaRegistroAsyncService;

    public EspelhoPontoListagemPageResponse listar(UUID empresaId, int page, int pageSize, String nome, Integer ano, Integer mes, HttpServletRequest httpRequest) {
        int limit = Math.max(1, Math.min(pageSize, 100));
        int offset = Math.max(0, page) * limit;
        var nomePattern = (nome != null && !nome.isBlank()) ? nome.trim().toLowerCase() + "%" : "%";

        LocalDate ref = LocalDate.now();
        int anoRef = (ano != null && ano > 0) ? ano : ref.getYear();
        int mesRef = (mes != null && mes > 0 && mes <= 12) ? mes : ref.getMonthValue();

        var list = espelhoPontoListagemRepository.findEspelhoPontoByEmpresaId(empresaId, anoRef, mesRef, nomePattern, limit, offset);
        long total = espelhoPontoListagemRepository.countEspelhoPontoByEmpresaId(empresaId, nomePattern);

        var conteudo = list.stream()
                .map(p -> mapToResponse(p))
                .toList();

        int totalPaginas = (int) Math.max(1, (total + limit - 1) / limit);
        var paginacao = new Paginacao(totalPaginas, total, conteudo.size(), Math.max(0, page));
        auditoriaRegistroAsyncService.registrarSemDispositivoID(empresaId, ACAO_LISTAGEM_ESPELHO_PONTO, "Listagem espelho de ponto", null, null, true, null, LocalDateTime.now(), httpRequest);
        return new EspelhoPontoListagemPageResponse(paginacao, conteudo);
    }

    private EspelhoPontoListagemResponse mapToResponse(EspelhoPontoListagemProjection p) {
        return new EspelhoPontoListagemResponse(
                p.getUsuarioId(),
                p.getNomeCompleto(),
                formatDurationToHHmm(p.getTotalHorasEsperadas()),
                formatDurationToHHmm(p.getTotalHorasTrabalhadas()),
                formatDurationToHHmm(p.getTotalHorasTrabalhadasFeriado())
        );
    }

    private static String formatDurationToHHmm(String s) {
        if (s == null || s.isBlank()) return "00:00";
        try {
            Duration d = Duration.parse(s);
            long totalMinutes = d.toMinutes();
            long h = totalMinutes / 60;
            long m = Math.abs(totalMinutes % 60);
            return String.format("%02d:%02d", h, m);
        } catch (Exception e) {
            return "00:00";
        }
    }
}
