package com.pontoeletronico.api.infrastructure.input.controller;

import com.pontoeletronico.api.domain.services.geofence.GeofenceService;
import com.pontoeletronico.api.infrastructure.input.controller.openapi.GeofencesSwagger;
import com.pontoeletronico.api.infrastructure.input.dto.geofence.CriarGeofenceRequest;
import com.pontoeletronico.api.infrastructure.input.dto.geofence.GeofenceListagemPageResponse;
import com.pontoeletronico.api.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/empresa")
public class GeofencesController implements GeofencesSwagger {

    private final GeofenceService geofenceService;
    private final JwtUtil jwtUtil;

    public GeofencesController(GeofenceService geofenceService, JwtUtil jwtUtil) {
        this.geofenceService = geofenceService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/geofences")
    @PreAuthorize("hasAuthority('SCOPE_EMPRESA')")
    public ResponseEntity<GeofenceListagemPageResponse> listar(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest httpRequest) {
        var empresaId = jwtUtil.extractUserIdFromToken(authorization);
        var response = geofenceService.listarPorEmpresa(empresaId, page, size, httpRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/geofences")
    @PreAuthorize("hasAuthority('SCOPE_EMPRESA')")
    public ResponseEntity<Void> criar(@Valid @RequestBody CriarGeofenceRequest request,
                                     @RequestHeader("Authorization") String authorization,
                                     HttpServletRequest httpRequest) {
        var empresaId = jwtUtil.extractUserIdFromToken(authorization);
        geofenceService.criar(empresaId, request, httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
