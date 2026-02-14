package com.pontoeletronico.api.domain.services.funcionario;

import com.pontoeletronico.api.infrastructure.input.dto.common.Paginacao;
import com.pontoeletronico.api.infrastructure.input.dto.empresa.FuncionarioListagemPageResponse;
import com.pontoeletronico.api.infrastructure.input.dto.empresa.FuncionarioListagemResponse;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.IdentificacaoFuncionarioRepository;
import com.pontoeletronico.api.util.ListagemParseUtil;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class FuncionarioListarService {

    private final IdentificacaoFuncionarioRepository identificacaoFuncionarioRepository;

    public FuncionarioListarService(IdentificacaoFuncionarioRepository identificacaoFuncionarioRepository) {
        this.identificacaoFuncionarioRepository = identificacaoFuncionarioRepository;
    }

    /** Doc id 18: Listar funcionários da empresa com paginação e filtro por nome. page e pageSize; query usa limit e offset. */
    public FuncionarioListagemPageResponse listar(UUID empresaId, int page, int pageSize, String nome) {
        var nomePattern = (nome != null && !nome.isBlank()) ? nome.trim().toLowerCase() + "%" : "%";
        int limit = Math.max(1, Math.min(pageSize, 100));
        int offset = Math.max(0, page) * limit;
        var list = identificacaoFuncionarioRepository.findFuncionariosByEmpresaId(empresaId, nomePattern, limit, offset);
        long total = identificacaoFuncionarioRepository.countFuncionariosByEmpresaId(empresaId, nomePattern);

        var conteudo = list.stream()
                .map(p -> {
                    var emails = ListagemParseUtil.parseEmails(p.getEmails());
                    var telefones = ListagemParseUtil.parseTelefones(p.getTelefones());
                    return new FuncionarioListagemResponse(p.getUsuarioId(), p.getUsername(), p.getTipo(), emails, telefones);
                })
                .toList();

        int totalPaginas = (int) Math.max(1, (total + limit - 1) / limit);
        var paginacao = new Paginacao(totalPaginas, total, conteudo.size(), Math.max(0, page));
        return new FuncionarioListagemPageResponse(paginacao, conteudo);
    }
}
