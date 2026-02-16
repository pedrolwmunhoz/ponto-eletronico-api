package com.pontoeletronico.api.infrastructure.input.controller;

import com.pontoeletronico.api.domain.services.admin.AdminCriarService;
import jakarta.servlet.http.HttpServletRequest;
import com.pontoeletronico.api.domain.services.admin.AdminDesbloquearService;
import com.pontoeletronico.api.domain.services.admin.AdminListarUsuariosService;
import com.pontoeletronico.api.infrastructure.input.controller.openapi.AdminSwagger;
import com.pontoeletronico.api.infrastructure.input.dto.admin.AdminCriarRequest;
import com.pontoeletronico.api.infrastructure.input.dto.admin.UsuarioListagemPageResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/admin")
public class AdminController implements AdminSwagger {

    private final AdminCriarService adminCriarService;
    private final AdminDesbloquearService adminDesbloquearService;
    private final AdminListarUsuariosService adminListarUsuariosService;

    public AdminController(AdminCriarService adminCriarService,
                           AdminDesbloquearService adminDesbloquearService,
                           AdminListarUsuariosService adminListarUsuariosService) {
        this.adminCriarService = adminCriarService;
        this.adminDesbloquearService = adminDesbloquearService;
        this.adminListarUsuariosService = adminListarUsuariosService;
    }

    @PostMapping("/usuarios")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public ResponseEntity<UUID> criarAdmin(@Valid @RequestBody AdminCriarRequest request, HttpServletRequest httpRequest) {
        var usuarioId = adminCriarService.criar(request, httpRequest);
        return ResponseEntity.status(201).body(usuarioId);
    }

    @GetMapping("/usuarios")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public ResponseEntity<UsuarioListagemPageResponse> listarUsuarios(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(adminListarUsuariosService.listar(authorization, page, size, httpRequest));
    }

    @PostMapping("/usuario/{usuarioId}/desbloquear")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public ResponseEntity<Void> desbloquearUsuario(@PathVariable("usuarioId") UUID usuarioId,
                                                   @RequestHeader("Authorization") String authorization,
                                                   HttpServletRequest httpRequest) {
        adminDesbloquearService.desbloquear(authorization, usuarioId, httpRequest);
        return ResponseEntity.ok().build();
    }
}
