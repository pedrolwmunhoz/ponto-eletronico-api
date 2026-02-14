package com.pontoeletronico.api.domain.services.admin;

import jakarta.servlet.http.HttpServletRequest;
import com.pontoeletronico.api.domain.services.audit.AuditoriaRegistroAsyncService;
import com.pontoeletronico.api.domain.services.usuario.UsuarioDesbloquearService;
import com.pontoeletronico.api.exception.AutorizacaoInvalidaException;
import com.pontoeletronico.api.exception.NaoAdminException;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.TipoUsuarioRepository;
import com.pontoeletronico.api.infrastructure.output.repository.usuario.UsersRepository;
import com.pontoeletronico.api.util.JwtUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AdminDesbloquearService {

    private static final String ACAO_DESBLOQUEAR_USUARIO_ADMIN = "DESBLOQUEAR_USUARIO_ADMIN";

    private final TipoUsuarioRepository tipoUsuarioRepository;
    private final UsersRepository usersRepository;
    private final UsuarioDesbloquearService desbloquearUsuarioService;
    private final AuditoriaRegistroAsyncService auditoriaRegistroAsyncService;
    private final JwtUtil jwtUtil;

    public AdminDesbloquearService(TipoUsuarioRepository tipoUsuarioRepository,
                                  UsersRepository usersRepository,
                                  UsuarioDesbloquearService desbloquearUsuarioService,
                                  AuditoriaRegistroAsyncService auditoriaRegistroAsyncService,
                                  JwtUtil jwtUtil) {
        this.tipoUsuarioRepository = tipoUsuarioRepository;
        this.usersRepository = usersRepository;
        this.desbloquearUsuarioService = desbloquearUsuarioService;
        this.auditoriaRegistroAsyncService = auditoriaRegistroAsyncService;
        this.jwtUtil = jwtUtil;
    }

    /** Doc id 53: Desbloquear qualquer usuário (apenas admin). */
    @Transactional
    public void desbloquear(String authorizationHeader, UUID usuarioId, HttpServletRequest httpRequest) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new AutorizacaoInvalidaException();
        }
        var adminUsuarioId = jwtUtil.extractUserIdFromToken(authorizationHeader);
        validarAdmin(adminUsuarioId);
        desbloquearUsuarioService.desbloquear(usuarioId);
        var dataReferencia = LocalDateTime.now();
        registrarAuditoriaDesbloquearUsuarioAdmin(adminUsuarioId, true, null, dataReferencia, httpRequest);
    }

    private void registrarAuditoriaDesbloquearUsuarioAdmin(UUID adminUsuarioId, boolean sucesso, String mensagemErro, LocalDateTime dataReferencia, HttpServletRequest httpRequest) {
        auditoriaRegistroAsyncService.registrarSemDispositivoID(adminUsuarioId, ACAO_DESBLOQUEAR_USUARIO_ADMIN, "Desbloqueio de usuário pelo admin", null, null, sucesso, mensagemErro, dataReferencia, httpRequest);
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
}
