package com.pontoeletronico.api.infrastructure.input.controller;

import com.pontoeletronico.api.domain.services.usuario.UsuarioService;
import com.pontoeletronico.api.infrastructure.input.controller.openapi.UsuarioSwagger;
import com.pontoeletronico.api.infrastructure.input.dto.usuario.UsuarioCredentialAdicionarRequest;
import com.pontoeletronico.api.infrastructure.input.dto.usuario.UsuarioEmailRequest;
import com.pontoeletronico.api.infrastructure.input.dto.usuario.UsuarioPerfilAtualizarRequest;
import com.pontoeletronico.api.infrastructure.input.dto.usuario.UsuarioTelefoneAdicionarRequest;
import com.pontoeletronico.api.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/usuario")
public class UsuarioController implements UsuarioSwagger {

    private final UsuarioService usuarioService;
    private final JwtUtil jwtUtil;

    public UsuarioController(UsuarioService usuarioService, JwtUtil jwtUtil) {
        this.usuarioService = usuarioService;
        this.jwtUtil = jwtUtil;
    }

    @PutMapping("/perfil")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN','SCOPE_EMPRESA','SCOPE_FUNCIONARIO')")
    public ResponseEntity<Void> atualizarPerfil(@Valid @RequestBody UsuarioPerfilAtualizarRequest request,
                                                @RequestHeader("Authorization") String authorization,
                                                HttpServletRequest httpRequest) {
        var usuarioId = jwtUtil.extractUserIdFromToken(authorization);
        usuarioService.atualizarPerfil(usuarioId, request, httpRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/email")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN','SCOPE_EMPRESA','SCOPE_FUNCIONARIO')")
    public ResponseEntity<Void> adicionarEmail(@Valid @RequestBody UsuarioEmailRequest request,
                                               @RequestHeader("Authorization") String authorization,
                                               HttpServletRequest httpRequest) {
        var usuarioId = jwtUtil.extractUserIdFromToken(authorization);
        usuarioService.adicionarEmail(usuarioId, request, httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/email")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN','SCOPE_EMPRESA','SCOPE_FUNCIONARIO')")
    public ResponseEntity<Void> removerEmail(@Valid @RequestBody UsuarioEmailRequest request,
                                             @RequestHeader("Authorization") String authorization,
                                             HttpServletRequest httpRequest) {
        var usuarioId = jwtUtil.extractUserIdFromToken(authorization);
        usuarioService.removerEmail(usuarioId, request, httpRequest);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/telefone")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN','SCOPE_EMPRESA','SCOPE_FUNCIONARIO')")
    public ResponseEntity<Void> adicionarTelefone(@Valid @RequestBody UsuarioTelefoneAdicionarRequest request,
                                                  @RequestHeader("Authorization") String authorization,
                                                  HttpServletRequest httpRequest) {
        var usuarioId = jwtUtil.extractUserIdFromToken(authorization);
        usuarioService.adicionarTelefone(usuarioId, request, httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/telefone/{telefoneId}")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN','SCOPE_EMPRESA','SCOPE_FUNCIONARIO')")
    public ResponseEntity<Void> removerTelefone(@PathVariable("telefoneId") UUID telefoneId,
                                                @RequestHeader("Authorization") String authorization,
                                                HttpServletRequest httpRequest) {
        var usuarioId = jwtUtil.extractUserIdFromToken(authorization);
        usuarioService.removerTelefone(usuarioId, telefoneId, httpRequest);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/credential")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN','SCOPE_EMPRESA','SCOPE_FUNCIONARIO')")
    public ResponseEntity<Void> adicionarCredential(@Valid @RequestBody UsuarioCredentialAdicionarRequest request,
                                                    @RequestHeader("Authorization") String authorization,
                                                    HttpServletRequest httpRequest) {
        var usuarioId = jwtUtil.extractUserIdFromToken(authorization);
        usuarioService.adicionarCredential(usuarioId, request, httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/credential/{credentialId}")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN','SCOPE_EMPRESA','SCOPE_FUNCIONARIO')")
    public ResponseEntity<Void> removerCredential(@PathVariable("credentialId") UUID credentialId,
                                                  @RequestHeader("Authorization") String authorization,
                                                  HttpServletRequest httpRequest) {
        var usuarioId = jwtUtil.extractUserIdFromToken(authorization);
        usuarioService.removerCredential(usuarioId, credentialId, httpRequest);
        return ResponseEntity.noContent().build();
    }
}
