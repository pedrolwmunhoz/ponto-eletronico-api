package com.pontoeletronico.api.domain.services.geofence;

import com.pontoeletronico.api.domain.entity.usuario.UsuarioGeofence;
import com.pontoeletronico.api.domain.services.audit.AuditoriaRegistroAsyncService;
import com.pontoeletronico.api.infrastructure.input.dto.common.Paginacao;
import com.pontoeletronico.api.infrastructure.input.dto.geofence.CriarGeofenceRequest;
import com.pontoeletronico.api.infrastructure.input.dto.geofence.GeofenceItemResponse;
import com.pontoeletronico.api.infrastructure.input.dto.geofence.GeofenceListagemPageResponse;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.GeofenceEmpresaConfigRepository;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.IdentificacaoFuncionarioRepository;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.XrefGeofenceFuncionariosRepository;
import com.pontoeletronico.api.infrastructure.output.repository.usuario.UsuarioGeofenceRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class GeofenceService {

    private static final String ACAO_LISTAGEM_GEOFENCES = "ACESSO_LISTAGEM_GEOFENCES";
    private static final String ACAO_CRIAR_GEOFENCE = "CRIAR_GEOFENCE";

    private final UsuarioGeofenceRepository usuarioGeofenceRepository;
    private final GeofenceEmpresaConfigRepository geofenceEmpresaConfigRepository;
    private final XrefGeofenceFuncionariosRepository xrefGeofenceFuncionariosRepository;
    private final IdentificacaoFuncionarioRepository identificacaoFuncionarioRepository;
    private final AuditoriaRegistroAsyncService auditoriaRegistroAsyncService;

    public GeofenceService(UsuarioGeofenceRepository usuarioGeofenceRepository,
                           GeofenceEmpresaConfigRepository geofenceEmpresaConfigRepository,
                           XrefGeofenceFuncionariosRepository xrefGeofenceFuncionariosRepository,
                           IdentificacaoFuncionarioRepository identificacaoFuncionarioRepository,
                           AuditoriaRegistroAsyncService auditoriaRegistroAsyncService) {
        this.usuarioGeofenceRepository = usuarioGeofenceRepository;
        this.geofenceEmpresaConfigRepository = geofenceEmpresaConfigRepository;
        this.xrefGeofenceFuncionariosRepository = xrefGeofenceFuncionariosRepository;
        this.identificacaoFuncionarioRepository = identificacaoFuncionarioRepository;
        this.auditoriaRegistroAsyncService = auditoriaRegistroAsyncService;
    }

    /** Doc id 45: Listar geofences da empresa (paginado). page e size; query usa limit e offset. */
    public GeofenceListagemPageResponse listarPorEmpresa(UUID empresaId, int page, int size, HttpServletRequest httpRequest) {
        int limit = Math.max(1, Math.min(size, 100));
        int offset = Math.max(0, page) * limit;
        var list = usuarioGeofenceRepository.findPageByUsuarioId(empresaId, limit, offset);
        long total = usuarioGeofenceRepository.countByUsuarioId(empresaId);
        var conteudo = list.stream().map(this::toItemResponse).toList();
        int totalPaginas = (int) Math.max(1, (total + limit - 1) / limit);
        var paginacao = new Paginacao(totalPaginas, total, conteudo.size(), Math.max(0, page));
        auditoriaRegistroAsyncService.registrarSemDispositivoID(empresaId, ACAO_LISTAGEM_GEOFENCES, "Listagem de geofences", null, null, true, null, LocalDateTime.now(), httpRequest);
        return new GeofenceListagemPageResponse(paginacao, conteudo);
    }

    /** Doc id 46: Criar novo geofence para a empresa. */
    @Transactional
    public void criar(UUID empresaId, CriarGeofenceRequest request, HttpServletRequest httpRequest) {
        var now = LocalDateTime.now();
        var geofenceId = UUID.randomUUID();
        var nome = request.nome();
        var ativo = request.ativo() != null ? request.ativo() : true;
        usuarioGeofenceRepository.insert(
                geofenceId,
                empresaId,
                nome,
                request.latitude(),
                request.longitude(),
                request.raioMetros(),
                ativo,
                now,
                now
        );
        geofenceEmpresaConfigRepository.insert(UUID.randomUUID(), geofenceId, now);
        if (request.funcionarioIds() != null && !request.funcionarioIds().isEmpty()) {
            for (UUID funcionarioId : request.funcionarioIds()) {
                if (identificacaoFuncionarioRepository.findByEmpresaIdAndFuncionarioIdAndAtivoTrue(empresaId, funcionarioId).isPresent()) {
                    xrefGeofenceFuncionariosRepository.insert(UUID.randomUUID(), geofenceId, funcionarioId);
                }
            }
        }
        auditoriaRegistroAsyncService.registrarSemDispositivoID(empresaId, ACAO_CRIAR_GEOFENCE, "Cadastro de geofence", null, null, true, null, now, httpRequest);
    }

    private GeofenceItemResponse toItemResponse(UsuarioGeofence ug) {
        var coordenadas = ug.getLatitude() + "," + ug.getLongitude();
        int qtd = (int) xrefGeofenceFuncionariosRepository.countByGeofenceId(ug.getId());
        boolean acessoParcial = qtd > 0;
        return new GeofenceItemResponse(
                ug.getId(),
                ug.getDescricao(),
                ug.getAtivo(),
                coordenadas,
                ug.getRaioMetros(),
                ug.getCreatedAt(),
                ug.getUpdatedAt(),
                acessoParcial,
                qtd
        );
    }
}
