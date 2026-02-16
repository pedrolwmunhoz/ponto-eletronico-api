package com.pontoeletronico.api.infrastructure.input.controller;

import com.pontoeletronico.api.domain.services.perfil.FuncionarioPerfilService;
import com.pontoeletronico.api.domain.services.funcionario.FuncionarioAtualizarService;
import com.pontoeletronico.api.domain.services.funcionario.FuncionarioCriarService;
import com.pontoeletronico.api.domain.services.funcionario.FuncionarioDeletarService;
import com.pontoeletronico.api.domain.services.funcionario.FuncionarioDesbloquearService;
import com.pontoeletronico.api.domain.services.funcionario.FuncionarioListarService;
import com.pontoeletronico.api.domain.services.funcionario.FuncionarioResetarEmailService;
import com.pontoeletronico.api.domain.services.funcionario.FuncionarioResetarSenhaService;
import com.pontoeletronico.api.infrastructure.input.controller.openapi.FuncionarioSwagger;
import com.pontoeletronico.api.infrastructure.input.dto.empresa.FuncionarioCreateRequest;
import com.pontoeletronico.api.infrastructure.input.dto.empresa.FuncionarioListagemPageResponse;
import com.pontoeletronico.api.infrastructure.input.dto.empresa.FuncionarioResetarEmailRequest;
import com.pontoeletronico.api.infrastructure.input.dto.empresa.FuncionarioResetarSenhaRequest;
import com.pontoeletronico.api.infrastructure.input.dto.empresa.FuncionarioUpdateRequest;
import com.pontoeletronico.api.infrastructure.input.dto.perfil.FuncionarioPerfilResponse;
import com.pontoeletronico.api.util.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/empresa")
public class FuncionarioController implements FuncionarioSwagger {

    private final FuncionarioDesbloquearService funcionarioDesbloquearService;
    private final FuncionarioCriarService funcionarioCriarService;
    private final FuncionarioResetarSenhaService funcionarioResetarSenhaService;
    private final FuncionarioResetarEmailService funcionarioResetarEmailService;
    private final FuncionarioAtualizarService funcionarioAtualizarService;
    private final FuncionarioDeletarService funcionarioDeletarService;
    private final FuncionarioListarService funcionarioListarService;
    private final FuncionarioPerfilService funcionarioPerfilService;
    private final JwtUtil jwtUtil;

    @PostMapping("/funcionario")
    @PreAuthorize("hasAuthority('SCOPE_EMPRESA')")
    public ResponseEntity<Map<String, UUID>> cadastrarFuncionario(@Valid @RequestBody FuncionarioCreateRequest request,
                                                                 @RequestHeader("Authorization") String authorization,
                                                                 HttpServletRequest httpRequest) {
        var empresaId = jwtUtil.extractUserIdFromToken(authorization);
        UUID funcionarioId = funcionarioCriarService.criar(empresaId, request, httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("funcionarioId", funcionarioId));
    }

    @PostMapping("/funcionario/{funcionarioId}/resetar-senha")
    @PreAuthorize("hasAuthority('SCOPE_EMPRESA')")
    public ResponseEntity<Void> resetarSenhaFuncionario(@PathVariable UUID funcionarioId,
                                                        @Valid @RequestBody FuncionarioResetarSenhaRequest request,
                                                        @RequestHeader("Authorization") String authorization,
                                                        HttpServletRequest httpRequest) {
        var empresaId = jwtUtil.extractUserIdFromToken(authorization);
        funcionarioResetarSenhaService.resetar(empresaId, funcionarioId, request, httpRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/funcionario/{funcionarioId}/resetar-email")
    @PreAuthorize("hasAuthority('SCOPE_EMPRESA')")
    public ResponseEntity<Void> resetarEmailFuncionario(@PathVariable UUID funcionarioId,
                                                        @Valid @RequestBody FuncionarioResetarEmailRequest request,
                                                        @RequestHeader("Authorization") String authorization,
                                                        HttpServletRequest httpRequest) {
        var empresaId = jwtUtil.extractUserIdFromToken(authorization);
        funcionarioResetarEmailService.resetar(empresaId, funcionarioId, request, httpRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/funcionario/{funcionarioId}/desbloquear")
    @PreAuthorize("hasAuthority('SCOPE_EMPRESA')")
    public ResponseEntity<Void> desbloquearFuncionario(@PathVariable UUID funcionarioId,
                                                      @RequestHeader("Authorization") String authorization,
                                                      HttpServletRequest httpRequest) {
        var empresaId = jwtUtil.extractUserIdFromToken(authorization);
        funcionarioDesbloquearService.desbloquear(empresaId, funcionarioId, httpRequest);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/funcionario/{funcionarioId}")
    @PreAuthorize("hasAuthority('SCOPE_EMPRESA')")
    public ResponseEntity<Void> atualizarFuncionario(@PathVariable UUID funcionarioId,
                                                     @Valid @RequestBody FuncionarioUpdateRequest request,
                                                     @RequestHeader("Authorization") String authorization,
                                                     HttpServletRequest httpRequest) {
        var empresaId = jwtUtil.extractUserIdFromToken(authorization);
        funcionarioAtualizarService.atualizar(empresaId, funcionarioId, request, httpRequest);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/funcionario/{funcionarioId}")
    @PreAuthorize("hasAuthority('SCOPE_EMPRESA')")
    public ResponseEntity<Void> deletarFuncionario(@PathVariable UUID funcionarioId,
                                                   @RequestHeader("Authorization") String authorization,
                                                   HttpServletRequest httpRequest) {
        var empresaId = jwtUtil.extractUserIdFromToken(authorization);
        funcionarioDeletarService.deletar(empresaId, funcionarioId, httpRequest);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/funcionario")
    @PreAuthorize("hasAuthority('SCOPE_EMPRESA')")
    public ResponseEntity<FuncionarioListagemPageResponse> listarFuncionarios(@RequestParam(defaultValue = "0") int page,
                                                                              @RequestParam(defaultValue = "20") int pageSize,
                                                                              @RequestParam(required = false) String nome,
                                                                              @RequestHeader("Authorization") String authorization,
                                                                              HttpServletRequest httpRequest) {
        var empresaId = jwtUtil.extractUserIdFromToken(authorization);
        var response = funcionarioListarService.listar(empresaId, page, pageSize, nome, httpRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/funcionario/{funcionarioId}/perfil")
    @PreAuthorize("hasAuthority('SCOPE_EMPRESA')")
    public ResponseEntity<FuncionarioPerfilResponse> getPerfilFuncionario(@PathVariable UUID funcionarioId,
                                                                          @RequestHeader("Authorization") String authorization,
                                                                          HttpServletRequest httpRequest) {
        var empresaId = jwtUtil.extractUserIdFromToken(authorization);
        var response = funcionarioPerfilService.buscar(empresaId, funcionarioId, httpRequest);
        return ResponseEntity.ok(response);
    }
}
