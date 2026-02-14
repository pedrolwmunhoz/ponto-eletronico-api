package com.pontoeletronico.api.domain.services.empresa;

import com.pontoeletronico.api.domain.services.audit.AuditoriaRegistroAsyncService;
import com.pontoeletronico.api.exception.TipoNaoEncontradoException;
import com.pontoeletronico.api.domain.enums.MensagemErro;
import com.pontoeletronico.api.infrastructure.input.dto.empresa.EmpresaConfigInicialRequest;
import com.pontoeletronico.api.infrastructure.input.dto.empresa.UsuarioGeofenceRequest;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.EmpresaBancoHorasConfigRepository;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.EmpresaJornadaConfigRepository;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.GeofenceEmpresaConfigRepository;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.TipoEscalaJornadaRepository;
import com.pontoeletronico.api.infrastructure.output.repository.usuario.UsuarioGeofenceRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class EmpresaConfigInicialService {

    private static final String ACAO_CONFIG_INICIAL = "CONFIG_INICIAL_EMPRESA";

    private final EmpresaJornadaConfigRepository empresaJornadaConfigRepository;
    private final EmpresaBancoHorasConfigRepository empresaBancoHorasConfigRepository;
    private final TipoEscalaJornadaRepository tipoEscalaJornadaRepository;
    private final UsuarioGeofenceRepository usuarioGeofenceRepository;
    private final GeofenceEmpresaConfigRepository geofenceEmpresaConfigRepository;
    private final AuditoriaRegistroAsyncService auditoriaRegistroAsyncService;

    public EmpresaConfigInicialService(EmpresaJornadaConfigRepository empresaJornadaConfigRepository,
                                      EmpresaBancoHorasConfigRepository empresaBancoHorasConfigRepository,
                                      TipoEscalaJornadaRepository tipoEscalaJornadaRepository,
                                      UsuarioGeofenceRepository usuarioGeofenceRepository,
                                      GeofenceEmpresaConfigRepository geofenceEmpresaConfigRepository,
                                      AuditoriaRegistroAsyncService auditoriaRegistroAsyncService) {
        this.empresaJornadaConfigRepository = empresaJornadaConfigRepository;
        this.empresaBancoHorasConfigRepository = empresaBancoHorasConfigRepository;
        this.tipoEscalaJornadaRepository = tipoEscalaJornadaRepository;
        this.usuarioGeofenceRepository = usuarioGeofenceRepository;
        this.geofenceEmpresaConfigRepository = geofenceEmpresaConfigRepository;
        this.auditoriaRegistroAsyncService = auditoriaRegistroAsyncService;
    }

    @Transactional
    /** Doc id 9: Configuração inicial da empresa. */
    public void configurar(UUID empresaId, EmpresaConfigInicialRequest request, HttpServletRequest httpRequest) {
        var empresaJornadaConfig = request.empresaJornadaConfig();
        var empresaBancoHorasConfig = request.empresaBancoHorasConfig();

        var now = LocalDateTime.now();
        if (tipoEscalaJornadaRepository.existsByIdAndAtivo(empresaJornadaConfig.tipoEscalaJornadaId()).isEmpty()) {
            auditoriaRegistroAsyncService.registrarSemDispositivoID(empresaId, ACAO_CONFIG_INICIAL, "Configuração inicial da empresa", null, null, false, MensagemErro.TIPO_ESCALA_JORNADA_NAO_ENCONTRADO.getMensagem(), now, httpRequest);
            throw new TipoNaoEncontradoException(MensagemErro.TIPO_ESCALA_JORNADA_NAO_ENCONTRADO);
        }

        var timezone = empresaJornadaConfig.timezone() != null && !empresaJornadaConfig.timezone().isBlank() ? empresaJornadaConfig.timezone() : "America/Sao_Paulo";
 

        // Jornada: insert ou update
        Duration tempoDescansoEntreJornada = empresaJornadaConfig.tempoDescansoEntreJornada() != null ? empresaJornadaConfig.tempoDescansoEntreJornada() : Duration.ofHours(11);
        Duration toleranciaPadrao = empresaJornadaConfig.toleranciaPadrao() != null ? empresaJornadaConfig.toleranciaPadrao() : Duration.ZERO;
        var existenteJornada = empresaJornadaConfigRepository.findByEmpresaId(empresaId);
        if (existenteJornada.isPresent()) {
            empresaJornadaConfigRepository.updateByEmpresaId(
                    empresaId, empresaJornadaConfig.tipoEscalaJornadaId(), empresaJornadaConfig.cargaHorariaDiaria(), empresaJornadaConfig.cargaHorariaSemanal(),
                    toleranciaPadrao, empresaJornadaConfig.intervaloPadrao(), empresaJornadaConfig.entradaPadrao(), empresaJornadaConfig.saidaPadrao(),
                    tempoDescansoEntreJornada, timezone, empresaJornadaConfig.gravaGeoObrigatoria(), empresaJornadaConfig.gravaPontoApenasEmGeofence(), empresaJornadaConfig.permiteAjustePonto(),
                    now);
        } else {
            empresaJornadaConfigRepository.insert(
                    UUID.randomUUID(), empresaId, empresaJornadaConfig.tipoEscalaJornadaId(), empresaJornadaConfig.cargaHorariaDiaria(), empresaJornadaConfig.cargaHorariaSemanal(),
                    toleranciaPadrao, empresaJornadaConfig.intervaloPadrao(), empresaJornadaConfig.entradaPadrao(), empresaJornadaConfig.saidaPadrao(),
                    tempoDescansoEntreJornada, timezone, empresaJornadaConfig.gravaGeoObrigatoria(), empresaJornadaConfig.gravaPontoApenasEmGeofence(), empresaJornadaConfig.permiteAjustePonto(),
                    now);
        }

        // Banco horas: insert ou update
        var existenteBanco = empresaBancoHorasConfigRepository.findByEmpresaId(empresaId);
        if (existenteBanco.isPresent()) {
            empresaBancoHorasConfigRepository.updateByEmpresaId(empresaId, empresaBancoHorasConfig.ativo(), empresaBancoHorasConfig.totalDiasVencimento(), now);
        } else {
            empresaBancoHorasConfigRepository.insert(UUID.randomUUID(), empresaId, empresaBancoHorasConfig.ativo(), empresaBancoHorasConfig.totalDiasVencimento(), now);
        }

        // Geofences opcionais
        List<UsuarioGeofenceRequest> geofences =
                request.usuarioGeofence() != null ? request.usuarioGeofence() : Collections.emptyList();
        for (var usuarioGeofenceRequest : geofences) {
            var geofenceId = UUID.randomUUID();
            usuarioGeofenceRepository.insert(
                    geofenceId, empresaId, usuarioGeofenceRequest.descricao(), usuarioGeofenceRequest.latitude(), usuarioGeofenceRequest.longitude(),
                    usuarioGeofenceRequest.raioMetros() != null ? usuarioGeofenceRequest.raioMetros() : 100, usuarioGeofenceRequest.ativo(),
                    now, now);
            geofenceEmpresaConfigRepository.insert(UUID.randomUUID(), geofenceId, now);
        }

        auditoriaRegistroAsyncService.registrarSemDispositivoID(empresaId, ACAO_CONFIG_INICIAL, "Configuração inicial da empresa", null, null, true, null, now, httpRequest);
    }

    /** Verifica se a empresa já realizou a configuração inicial (possui registro em empresa_jornada_config). */
    public boolean isConfigInicialRealizada(UUID empresaId) {
        return empresaJornadaConfigRepository.existsByEmpresaId(empresaId).isPresent();
    }
}
