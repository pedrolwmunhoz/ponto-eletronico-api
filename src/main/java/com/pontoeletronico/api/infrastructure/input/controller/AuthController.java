package com.pontoeletronico.api.infrastructure.input.controller;

import com.pontoeletronico.api.domain.services.auth.AuthService;
import com.pontoeletronico.api.domain.services.auth.LogoutService;
import com.pontoeletronico.api.domain.services.auth.RecuperarSenhaService;
import com.pontoeletronico.api.domain.services.auth.RefreshTokenService;
import com.pontoeletronico.api.domain.services.auth.ResetarSenhaService;
import com.pontoeletronico.api.domain.services.auth.ValidarCodigoService;
import com.pontoeletronico.api.infrastructure.input.controller.openapi.AuthSwagger;
import com.pontoeletronico.api.infrastructure.input.dto.auth.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/auth")
public class AuthController implements AuthSwagger {

    private final AuthService authService;
    private final RecuperarSenhaService recuperarSenhaService;
    private final ValidarCodigoService validarCodigoService;
    private final ResetarSenhaService resetarSenhaService;
    private final RefreshTokenService refreshTokenService;
    private final LogoutService logoutService;

    public AuthController(AuthService authService,
                          RecuperarSenhaService recuperarSenhaService,
                          ValidarCodigoService validarCodigoService,
                          ResetarSenhaService resetarSenhaService,
                          RefreshTokenService refreshTokenService,
                          LogoutService logoutService) {
        this.authService = authService;
        this.recuperarSenhaService = recuperarSenhaService;
        this.validarCodigoService = validarCodigoService;
        this.resetarSenhaService = resetarSenhaService;
        this.refreshTokenService = refreshTokenService;
        this.logoutService = logoutService;
    }

    @PostMapping
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        var response = authService.login(request, httpRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/recuperar-senha")
    public ResponseEntity<Void> recuperarSenha(@Valid @RequestBody RecuperarSenhaRequest request, HttpServletRequest httpRequest) {
        recuperarSenhaService.recuperar(request, httpRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/validar-codigo")
    public ResponseEntity<ValidarCodigoResponse> validarCodigo(@Valid @RequestBody ValidarCodigoRequest request, HttpServletRequest httpRequest) {
        return ResponseEntity.ok(validarCodigoService.validar(request, httpRequest));
    }

    @PostMapping("/resetar-senha")
    public ResponseEntity<Void> resetarSenha(@Valid @RequestBody ResetarSenhaRequest request, HttpServletRequest httpRequest) {
        resetarSenhaService.resetar(request, httpRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponse> refresh(@Valid @RequestBody RefreshRequest request, HttpServletRequest httpRequest) {
        return ResponseEntity.ok(refreshTokenService.refresh(request, httpRequest));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authorization, HttpServletRequest httpRequest) {
        logoutService.logout(authorization, httpRequest);
        return ResponseEntity.ok().build();
    }
}
