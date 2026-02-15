package com.pontoeletronico.api.domain.services.feriasafastamentos;

import com.pontoeletronico.api.exception.FuncionarioNaoPertenceEmpresaException;
import com.pontoeletronico.api.exception.RegistroNaoEncontradoException;
import com.pontoeletronico.api.infrastructure.input.dto.feriasafastamentos.CriarAfastamentoRequest;
import com.pontoeletronico.api.infrastructure.input.dto.feriasafastamentos.FeriasAfastamentoItemResponse;
import com.pontoeletronico.api.infrastructure.input.dto.feriasafastamentos.FeriasAfastamentosListagemResponse;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.AfastamentoRepository;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.FeriasAfastamentosListagemProjection;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.FeriasAfastamentosListagemRepository;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.IdentificacaoFuncionarioRepository;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.TipoAfastamentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class FeriasAfastamentosService {

    private final FeriasAfastamentosListagemRepository listagemRepository;
    private final AfastamentoRepository afastamentoRepository;
    private final TipoAfastamentoRepository tipoAfastamentoRepository;
    private final IdentificacaoFuncionarioRepository identificacaoFuncionarioRepository;

    public FeriasAfastamentosService(FeriasAfastamentosListagemRepository listagemRepository,
                                    AfastamentoRepository afastamentoRepository,
                                    TipoAfastamentoRepository tipoAfastamentoRepository,
                                    IdentificacaoFuncionarioRepository identificacaoFuncionarioRepository) {
        this.listagemRepository = listagemRepository;
        this.afastamentoRepository = afastamentoRepository;
        this.tipoAfastamentoRepository = tipoAfastamentoRepository;
        this.identificacaoFuncionarioRepository = identificacaoFuncionarioRepository;
    }

    /** Doc id 39: Listar férias e afastamentos do funcionário. */
    public FeriasAfastamentosListagemResponse listarPorFuncionario(UUID funcionarioId, int page, int size) {
        int limit = Math.max(1, Math.min(size, 100));
        int offset = Math.max(0, page) * limit;
        var list = listagemRepository.findPageByFuncionarioId(funcionarioId, limit, offset);
        long total = listagemRepository.countByFuncionarioId(funcionarioId);
        var items = mapItemsSemNomeFuncionario(list);
        return new FeriasAfastamentosListagemResponse(items, total, Math.max(0, page), limit);
    }

    /** Doc id 40: Listar férias e afastamentos de um funcionário por id. */
    public FeriasAfastamentosListagemResponse listarPorFuncionarioIdEmpresa(UUID empresaId, UUID funcionarioId, int page, int size) {
        identificacaoFuncionarioRepository.findByEmpresaIdAndFuncionarioIdAndAtivoTrue(empresaId, funcionarioId)
        .orElseThrow( () -> new FuncionarioNaoPertenceEmpresaException()); 
        int limit = Math.max(1, Math.min(size, 100));
        int offset = Math.max(0, page) * limit;
        var list = listagemRepository.findPageByFuncionarioIdEmpresa(funcionarioId, limit, offset);
        long total = listagemRepository.countByFuncionarioIdEmpresa(funcionarioId);
        var items = mapItemsSemNomeFuncionario(list);
        return new FeriasAfastamentosListagemResponse(items, total, Math.max(0, page), limit);
    }

    /** Doc id 41: Listar férias e afastamentos da empresa. */
    public FeriasAfastamentosListagemResponse listarPorEmpresa(UUID empresaId, int page, int size) {
        int limit = Math.max(1, Math.min(size, 100));
        int offset = Math.max(0, page) * limit;
        var list = listagemRepository.findPageByEmpresaId(empresaId, limit, offset);
        long total = listagemRepository.countByEmpresaId(empresaId);
        var items = mapItemsComNomeFuncionario(list);
        return new FeriasAfastamentosListagemResponse(items, total, Math.max(0, page), limit);
    }

    /** Doc id 42: Criar afastamento para um funcionário. */
    @Transactional
    public void criarAfastamento(UUID empresaId, UUID funcionarioId, CriarAfastamentoRequest request) {
        identificacaoFuncionarioRepository.findByEmpresaIdAndFuncionarioIdAndAtivoTrue(empresaId, funcionarioId)
        .orElseThrow( () -> new FuncionarioNaoPertenceEmpresaException()); 
        tipoAfastamentoRepository.findByIdAndAtivoTrue(request.tipoAfastamentoId())
                .orElseThrow(() -> new RegistroNaoEncontradoException("Tipo de afastamento não encontrado ou inativo"));
        var now = LocalDateTime.now();
        afastamentoRepository.insert(
                UUID.randomUUID(),
                funcionarioId,
                request.tipoAfastamentoId(),
                request.dataInicio(),
                request.dataFim(),
                request.observacao(),
                request.ativo() != null ? request.ativo() : true,
                now,
                now
        );
    }

    private static List<FeriasAfastamentoItemResponse> mapItemsSemNomeFuncionario(List<FeriasAfastamentosListagemProjection> list) {
        return list.stream()
                .map(p -> FeriasAfastamentoItemResponse.semNomeFuncionario(
                        p.getNomeAfastamento(),
                        p.getInicio(),
                        p.getFim(),
                        p.getStatus()))
                .toList();
    }

    private static List<FeriasAfastamentoItemResponse> mapItemsComNomeFuncionario(List<FeriasAfastamentosListagemProjection> list) {
        return list.stream()
                .map(p -> FeriasAfastamentoItemResponse.comNomeFuncionario(
                        p.getNomeFuncionario(),
                        p.getNomeAfastamento(),
                        p.getInicio(),
                        p.getFim(),
                        p.getStatus()))
                .toList();
    }
}
