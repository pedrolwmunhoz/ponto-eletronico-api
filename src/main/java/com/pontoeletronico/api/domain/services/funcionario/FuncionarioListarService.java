package com.pontoeletronico.api.domain.services.funcionario;

import com.pontoeletronico.api.domain.services.audit.AuditoriaRegistroAsyncService;
import com.pontoeletronico.api.infrastructure.input.dto.common.Paginacao;
import com.pontoeletronico.api.infrastructure.input.dto.empresa.FuncionarioListagemPageResponse;
import com.pontoeletronico.api.infrastructure.input.dto.empresa.FuncionarioListagemResponse;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.IdentificacaoFuncionarioRepository;
import com.pontoeletronico.api.util.ListagemParseUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class FuncionarioListarService {

    private static final String ACAO_LISTAGEM_FUNCIONARIOS = "ACESSO_LISTAGEM_FUNCIONARIOS";

    private final IdentificacaoFuncionarioRepository identificacaoFuncionarioRepository;
    private final AuditoriaRegistroAsyncService auditoriaRegistroAsyncService;

    public FuncionarioListarService(IdentificacaoFuncionarioRepository identificacaoFuncionarioRepository,
                                    AuditoriaRegistroAsyncService auditoriaRegistroAsyncService) {
        this.identificacaoFuncionarioRepository = identificacaoFuncionarioRepository;
        this.auditoriaRegistroAsyncService = auditoriaRegistroAsyncService;
    }

    /** Doc id 18: Listar funcionários da empresa com paginação e filtro por nome. page e pageSize; query usa limit e offset. Busca abrangente: primeiro, último ou nome completo. */
    public FuncionarioListagemPageResponse listar(UUID empresaId, int page, int pageSize, String nome, HttpServletRequest httpRequest) {
        var nomeParam = (nome != null && !nome.isBlank()) ? nome.trim() : "";
        int limit = Math.max(1, Math.min(pageSize, 100));
        int offset = Math.max(0, page) * limit;
        var list = identificacaoFuncionarioRepository.findFuncionariosByEmpresaId(empresaId, nomeParam, limit, offset);
        long total = identificacaoFuncionarioRepository.countFuncionariosByEmpresaId(empresaId, nomeParam);

        var conteudo = list.stream()
                .map(p -> {
                    var emails = ListagemParseUtil.parseEmails(p.getEmails());
                    var telefones = ListagemParseUtil.parseTelefones(p.getTelefones());
                    return new FuncionarioListagemResponse(p.getUsuarioId(), p.getPrimeiroNome(), p.getUltimoNome(), p.getUsername(), emails, telefones);
                })
                .toList();

        int totalPaginas = (int) Math.max(1, (total + limit - 1) / limit);
        var paginacao = new Paginacao(totalPaginas, total, conteudo.size(), Math.max(0, page));
        auditoriaRegistroAsyncService.registrarSemDispositivoID(empresaId, ACAO_LISTAGEM_FUNCIONARIOS, "Listagem de funcionários", null, null, true, null, LocalDateTime.now(), httpRequest);
        return new FuncionarioListagemPageResponse(paginacao, conteudo);
    }
}
