package com.pontoeletronico.api.domain.services.feriado;

import com.pontoeletronico.api.domain.services.audit.AuditoriaRegistroAsyncService;
import com.pontoeletronico.api.exception.ConflitoException;
import com.pontoeletronico.api.exception.RegistroNaoEncontradoException;
import com.pontoeletronico.api.infrastructure.input.dto.common.Paginacao;
import com.pontoeletronico.api.infrastructure.input.dto.feriado.*;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.FeriadoListagemProjection;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.FeriadoRepository;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.TipoFeriadoRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class FeriadoService {

    private static final String ACAO_CRIAR_FERIADO = "CRIAR_FERIADO";
    private static final String ACAO_ATUALIZAR_FERIADO = "ATUALIZAR_FERIADO";
    private static final String ACAO_DELETAR_FERIADO = "DELETAR_FERIADO";
    private static final String ACAO_LISTAGEM_FERIADOS = "ACESSO_LISTAGEM_FERIADOS";

    private final FeriadoRepository feriadoRepository;
    private final TipoFeriadoRepository tipoFeriadoRepository;
    private final AuditoriaRegistroAsyncService auditoriaRegistroAsyncService;

    public FeriadoService(FeriadoRepository feriadoRepository,
                          TipoFeriadoRepository tipoFeriadoRepository,
                          AuditoriaRegistroAsyncService auditoriaRegistroAsyncService) {
        this.feriadoRepository = feriadoRepository;
        this.tipoFeriadoRepository = tipoFeriadoRepository;
        this.auditoriaRegistroAsyncService = auditoriaRegistroAsyncService;
    }

    /** Doc id 52: Listar feriados da empresa (paginado). Inclui feriados da empresa + feriados criados por Admin. Filtro opcional por observacao (descricao). */
    public FeriadoListagemPageResponse listarPorEmpresa(UUID empresaId, int page, int size, String observacao, HttpServletRequest httpRequest) {
        var observacaoPattern = (observacao != null && !observacao.isBlank()) ? "%" + observacao.trim() + "%" : "%";
        int limit = Math.max(1, Math.min(size, 100));
        int offset = Math.max(0, page) * limit;
        var list = feriadoRepository.findPageForEmpresa(empresaId, observacaoPattern, limit, offset);
        long total = feriadoRepository.countForEmpresa(empresaId, observacaoPattern);
        var conteudo = list.stream().map(this::toItemResponse).toList();
        int totalPaginas = (int) Math.max(1, (total + limit - 1) / limit);
        var paginacao = new Paginacao(totalPaginas, total, conteudo.size(), Math.max(0, page));
        auditoriaRegistroAsyncService.registrarSemDispositivoID(empresaId, ACAO_LISTAGEM_FERIADOS, "Listagem de feriados", null, null, true, null, LocalDateTime.now(), httpRequest);
        return new FeriadoListagemPageResponse(paginacao, conteudo);
    }

    /** Doc id 53: Criar feriado para a empresa. Empresa só pode cadastrar ESTADUAL ou MUNICIPAL. usuarioId do JWT. */
    @Transactional
    public void criar(UUID usuarioId, CriarFeriadoRequest request, HttpServletRequest httpRequest) {
        var tipoFeriado = tipoFeriadoRepository.findByIdAndAtivoTrue(request.tipoFeriadoId())
                .orElseThrow(() -> new RegistroNaoEncontradoException("Tipo de feriado não encontrado ou inativo"));
        if ("NACIONAL".equals(tipoFeriado.getDescricao())) {
            throw new ConflitoException("Empresa pode cadastrar apenas feriados Estadual ou Municipal");
        }
        var now = LocalDateTime.now();
        var ativo = request.ativo() != null ? request.ativo() : true;
        feriadoRepository.insert(
                UUID.randomUUID(),
                request.data(),
                request.descricao().trim(),
                request.tipoFeriadoId(),
                usuarioId,
                ativo,
                now
        );
        auditoriaRegistroAsyncService.registrarSemDispositivoID(usuarioId, ACAO_CRIAR_FERIADO, "Cadastro de feriado", null, null, true, null, now, httpRequest);
    }

    /** Doc id 54: Atualizar feriado. Empresa só edita os seus (usuario_id = empresa). ESTADUAL ou MUNICIPAL. */
    @Transactional
    public void atualizar(UUID usuarioId, UUID feriadoId, EditarFeriadoRequest request, HttpServletRequest httpRequest) {
        feriadoRepository.findByIdAndUsuarioIdAndAtivoTrue(feriadoId, usuarioId)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Feriado não encontrado ou não pertence à empresa"));
        var tipoFeriado = tipoFeriadoRepository.findByIdAndAtivoTrue(request.tipoFeriadoId())
                .orElseThrow(() -> new RegistroNaoEncontradoException("Tipo de feriado não encontrado ou inativo"));
        if ("NACIONAL".equals(tipoFeriado.getDescricao())) {
            throw new ConflitoException("Empresa pode cadastrar apenas feriados Estadual ou Municipal");
        }
        var ativo = request.ativo() != null ? request.ativo() : true;
        var rows = feriadoRepository.update(
                feriadoId, usuarioId, request.data(), request.descricao().trim(),
                request.tipoFeriadoId(), ativo
        );
        if (rows == 0) {
            throw new RegistroNaoEncontradoException("Feriado não encontrado ou não pertence à empresa");
        }
        var now = LocalDateTime.now();
        auditoriaRegistroAsyncService.registrarSemDispositivoID(usuarioId, ACAO_ATUALIZAR_FERIADO, "Atualização de feriado", null, null, true, null, now, httpRequest);
    }

    /** Doc id 55: Excluir feriado (soft delete). Empresa só exclui os seus. */
    @Transactional
    public void deletar(UUID usuarioId, UUID feriadoId, HttpServletRequest httpRequest) {
        feriadoRepository.findByIdAndUsuarioIdAndAtivoTrue(feriadoId, usuarioId)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Feriado não encontrado ou não pertence à empresa"));
        var rows = feriadoRepository.desativar(feriadoId, usuarioId);
        if (rows == 0) {
            throw new RegistroNaoEncontradoException("Feriado não encontrado ou não pertence à empresa");
        }
        var now = LocalDateTime.now();
        auditoriaRegistroAsyncService.registrarSemDispositivoID(usuarioId, ACAO_DELETAR_FERIADO, "Exclusão de feriado", null, null, true, null, now, httpRequest);
    }

    private FeriadoItemResponse toItemResponse(FeriadoListagemProjection p) {
        return new FeriadoItemResponse(
                p.getId(),
                p.getData(),
                p.getDescricao(),
                p.getTipoFeriadoId(),
                p.getTipoFeriadoDescricao(),
                p.getAtivo(),
                p.getCreatedAt()
        );
    }
}
