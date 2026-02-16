package com.pontoeletronico.api.domain.services.admin;

import com.pontoeletronico.api.domain.services.audit.AuditoriaRegistroAsyncService;
import com.pontoeletronico.api.exception.NaoAdminException;
import com.pontoeletronico.api.exception.RegistroNaoEncontradoException;
import com.pontoeletronico.api.infrastructure.input.dto.common.Paginacao;
import com.pontoeletronico.api.infrastructure.input.dto.feriado.*;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.FeriadoListagemProjection;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.FeriadoRepository;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.TipoFeriadoRepository;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.TipoUsuarioRepository;
import com.pontoeletronico.api.infrastructure.output.repository.usuario.UsersRepository;
import com.pontoeletronico.api.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AdminFeriadoService {

    private static final String ACAO_ADMIN_CRIAR_FERIADO_ABRANGENCIA = "ADMIN_CRIAR_FERIADO_ABRANGENCIA";
    private static final String ACAO_ADMIN_ATUALIZAR_FERIADO_ABRANGENCIA = "ADMIN_ATUALIZAR_FERIADO_ABRANGENCIA";
    private static final String ACAO_ADMIN_DELETAR_FERIADO_ABRANGENCIA = "ADMIN_DELETAR_FERIADO_ABRANGENCIA";
    private static final String ACAO_ADMIN_CRIAR_FERIADO_EMPRESA = "ADMIN_CRIAR_FERIADO_EMPRESA";
    private static final String ACAO_ADMIN_ATUALIZAR_FERIADO_EMPRESA = "ADMIN_ATUALIZAR_FERIADO_EMPRESA";
    private static final String ACAO_ADMIN_DELETAR_FERIADO_EMPRESA = "ADMIN_DELETAR_FERIADO_EMPRESA";

    private final JwtUtil jwtUtil;
    private final FeriadoRepository feriadoRepository;
    private final TipoFeriadoRepository tipoFeriadoRepository;
    private final TipoUsuarioRepository tipoUsuarioRepository;
    private final UsersRepository usersRepository;
    private final AuditoriaRegistroAsyncService auditoriaRegistroAsyncService;

    public AdminFeriadoService(JwtUtil jwtUtil,
                               FeriadoRepository feriadoRepository,
                               TipoFeriadoRepository tipoFeriadoRepository,
                               TipoUsuarioRepository tipoUsuarioRepository,
                               UsersRepository usersRepository,
                               AuditoriaRegistroAsyncService auditoriaRegistroAsyncService) {
        this.jwtUtil = jwtUtil;
        this.feriadoRepository = feriadoRepository;
        this.tipoFeriadoRepository = tipoFeriadoRepository;
        this.tipoUsuarioRepository = tipoUsuarioRepository;
        this.usersRepository = usersRepository;
        this.auditoriaRegistroAsyncService = auditoriaRegistroAsyncService;
    }

    /** Admin: Listar feriados de abrangência (criados por Admin). */
    public FeriadoListagemPageResponse listarAbrangencia(String authorization, int page, int size) {
        var adminId = jwtUtil.extractUserIdFromToken(authorization);
        validarAdmin(adminId);
        int limit = Math.max(1, Math.min(size, 100));
        int offset = Math.max(0, page) * limit;
        var list = feriadoRepository.findPageByUsuarioIdIsAdmin(limit, offset);
        long total = feriadoRepository.countByUsuarioIdIsAdmin();
        var conteudo = list.stream().map(this::toItemResponse).toList();
        int totalPaginas = (int) Math.max(1, (total + limit - 1) / limit);
        var paginacao = new Paginacao(totalPaginas, total, conteudo.size(), Math.max(0, page));
        return new FeriadoListagemPageResponse(paginacao, conteudo);
    }

    /** Admin: Criar feriado de abrangência. usuarioId = admin do JWT. */
    @Transactional
    public void criarAbrangencia(String authorization, CriarFeriadoRequest request, HttpServletRequest httpRequest) {
        var adminId = jwtUtil.extractUserIdFromToken(authorization);
        validarAdmin(adminId);
        tipoFeriadoRepository.findByIdAndAtivoTrue(request.tipoFeriadoId())
                .orElseThrow(() -> new RegistroNaoEncontradoException("Tipo de feriado não encontrado ou inativo"));
        var now = LocalDateTime.now();
        var ativo = request.ativo() != null ? request.ativo() : true;
        feriadoRepository.insert(
                UUID.randomUUID(),
                request.data(),
                request.descricao().trim(),
                request.tipoFeriadoId(),
                adminId,
                ativo,
                now
        );
        auditoriaRegistroAsyncService.registrarSemDispositivoID(adminId, ACAO_ADMIN_CRIAR_FERIADO_ABRANGENCIA, "Admin criou feriado de abrangência", null, null, true, null, now, httpRequest);
    }

    /** Admin: Editar feriado de abrangência (próprios). */
    @Transactional
    public void atualizarAbrangencia(String authorization, UUID feriadoId, EditarFeriadoRequest request, HttpServletRequest httpRequest) {
        var adminId = jwtUtil.extractUserIdFromToken(authorization);
        validarAdmin(adminId);
        feriadoRepository.findByIdAndUsuarioIdAndAtivoTrue(feriadoId, adminId)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Feriado não encontrado"));
        tipoFeriadoRepository.findByIdAndAtivoTrue(request.tipoFeriadoId())
                .orElseThrow(() -> new RegistroNaoEncontradoException("Tipo de feriado não encontrado ou inativo"));
        var ativo = request.ativo() != null ? request.ativo() : true;
        var rows = feriadoRepository.update(
                feriadoId, adminId, request.data(), request.descricao().trim(),
                request.tipoFeriadoId(), ativo
        );
        if (rows == 0) {
            throw new RegistroNaoEncontradoException("Feriado não encontrado");
        }
        var now = LocalDateTime.now();
        auditoriaRegistroAsyncService.registrarSemDispositivoID(adminId, ACAO_ADMIN_ATUALIZAR_FERIADO_ABRANGENCIA, "Admin atualizou feriado de abrangência", null, null, true, null, now, httpRequest);
    }

    /** Admin: Excluir feriado de abrangência. */
    @Transactional
    public void deletarAbrangencia(String authorization, UUID feriadoId, HttpServletRequest httpRequest) {
        var adminId = jwtUtil.extractUserIdFromToken(authorization);
        validarAdmin(adminId);
        feriadoRepository.findByIdAndUsuarioIdAndAtivoTrue(feriadoId, adminId)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Feriado não encontrado"));
        var rows = feriadoRepository.desativar(feriadoId, adminId);
        if (rows == 0) {
            throw new RegistroNaoEncontradoException("Feriado não encontrado");
        }
        var now = LocalDateTime.now();
        auditoriaRegistroAsyncService.registrarSemDispositivoID(adminId, ACAO_ADMIN_DELETAR_FERIADO_ABRANGENCIA, "Admin excluiu feriado de abrangência", null, null, true, null, now, httpRequest);
    }

    /** Admin: Listar feriados por empresa (usuario_id = empresaId). */
    public FeriadoListagemPageResponse listarPorEmpresa(String authorization, UUID empresaId, int page, int size) {
        var adminId = jwtUtil.extractUserIdFromToken(authorization);
        validarAdmin(adminId);
        validarEmpresaExiste(empresaId);
        int limit = Math.max(1, Math.min(size, 100));
        int offset = Math.max(0, page) * limit;
        var list = feriadoRepository.findPageByUsuarioId(empresaId, limit, offset);
        long total = feriadoRepository.countByUsuarioId(empresaId);
        var conteudo = list.stream().map(this::toItemResponse).toList();
        int totalPaginas = (int) Math.max(1, (total + limit - 1) / limit);
        var paginacao = new Paginacao(totalPaginas, total, conteudo.size(), Math.max(0, page));
        return new FeriadoListagemPageResponse(paginacao, conteudo);
    }

    /** Admin: Criar feriado por empresa. usuario_id = empresaId (feriado pertence à empresa). */
    @Transactional
    public void criarPorEmpresa(String authorization, UUID empresaId, CriarFeriadoRequest request, HttpServletRequest httpRequest) {
        var adminId = jwtUtil.extractUserIdFromToken(authorization);
        validarAdmin(adminId);
        validarEmpresaExiste(empresaId);
        tipoFeriadoRepository.findByIdAndAtivoTrue(request.tipoFeriadoId())
                .orElseThrow(() -> new RegistroNaoEncontradoException("Tipo de feriado não encontrado ou inativo"));
        var now = LocalDateTime.now();
        var ativo = request.ativo() != null ? request.ativo() : true;
        feriadoRepository.insert(
                UUID.randomUUID(),
                request.data(),
                request.descricao().trim(),
                request.tipoFeriadoId(),
                empresaId,
                ativo,
                now
        );
        auditoriaRegistroAsyncService.registrarSemDispositivoID(adminId, ACAO_ADMIN_CRIAR_FERIADO_EMPRESA, "Admin criou feriado para empresa", null, null, true, null, now, httpRequest);
    }

    /** Admin: Editar feriado por empresa. */
    @Transactional
    public void atualizarPorEmpresa(String authorization, UUID empresaId, UUID feriadoId, EditarFeriadoRequest request, HttpServletRequest httpRequest) {
        var adminId = jwtUtil.extractUserIdFromToken(authorization);
        validarAdmin(adminId);
        validarEmpresaExiste(empresaId);
        feriadoRepository.findByIdAndUsuarioIdAndAtivoTrue(feriadoId, empresaId)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Feriado não encontrado ou não pertence à empresa"));
        tipoFeriadoRepository.findByIdAndAtivoTrue(request.tipoFeriadoId())
                .orElseThrow(() -> new RegistroNaoEncontradoException("Tipo de feriado não encontrado ou inativo"));
        var ativo = request.ativo() != null ? request.ativo() : true;
        var rows = feriadoRepository.update(
                feriadoId, empresaId, request.data(), request.descricao().trim(),
                request.tipoFeriadoId(), ativo
        );
        if (rows == 0) {
            throw new RegistroNaoEncontradoException("Feriado não encontrado ou não pertence à empresa");
        }
        var now = LocalDateTime.now();
        auditoriaRegistroAsyncService.registrarSemDispositivoID(adminId, ACAO_ADMIN_ATUALIZAR_FERIADO_EMPRESA, "Admin atualizou feriado da empresa", null, null, true, null, now, httpRequest);
    }

    /** Admin: Excluir feriado por empresa. */
    @Transactional
    public void deletarPorEmpresa(String authorization, UUID empresaId, UUID feriadoId, HttpServletRequest httpRequest) {
        var adminId = jwtUtil.extractUserIdFromToken(authorization);
        validarAdmin(adminId);
        validarEmpresaExiste(empresaId);
        feriadoRepository.findByIdAndUsuarioIdAndAtivoTrue(feriadoId, empresaId)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Feriado não encontrado ou não pertence à empresa"));
        var rows = feriadoRepository.desativar(feriadoId, empresaId);
        if (rows == 0) {
            throw new RegistroNaoEncontradoException("Feriado não encontrado ou não pertence à empresa");
        }
        var now = LocalDateTime.now();
        auditoriaRegistroAsyncService.registrarSemDispositivoID(adminId, ACAO_ADMIN_DELETAR_FERIADO_EMPRESA, "Admin excluiu feriado da empresa", null, null, true, null, now, httpRequest);
    }

    private void validarAdmin(UUID usuarioId) {
        var tipoAdminId = tipoUsuarioRepository.findIdByDescricao("ADMIN");
        if (tipoAdminId == null) {
            throw new NaoAdminException();
        }
        var user = usersRepository.findByIdQuery(usuarioId).orElseThrow(NaoAdminException::new);
        if (!tipoAdminId.equals(user.getTipoUsuarioId())) {
            throw new NaoAdminException();
        }
    }

    private void validarEmpresaExiste(UUID empresaId) {
        var user = usersRepository.findByIdQuery(empresaId).orElseThrow(() -> new RegistroNaoEncontradoException("Empresa não encontrada"));
        var tipoEmpresaId = tipoUsuarioRepository.findIdByDescricao("EMPRESA");
        if (tipoEmpresaId != null && !tipoEmpresaId.equals(user.getTipoUsuarioId())) {
            throw new RegistroNaoEncontradoException("Empresa não encontrada");
        }
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
