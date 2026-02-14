package com.pontoeletronico.api.domain.services.empresa;

import com.pontoeletronico.api.domain.services.audit.AuditoriaRegistroAsyncService;
import com.pontoeletronico.api.exception.TipoNaoEncontradoException;
import com.pontoeletronico.api.domain.enums.MensagemErro;
import com.pontoeletronico.api.infrastructure.input.dto.empresa.EmpresaJornadaConfigRequest;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.EmpresaJornadaConfigRepository;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.TipoEscalaJornadaRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class EmpresaJornadaPadraoService {

    private static final String ACAO_JORNADA_PADRAO = "ATUALIZAR_JORNADA_PADRAO_EMPRESA";

    private final EmpresaJornadaConfigRepository empresaJornadaConfigRepository;
    private final TipoEscalaJornadaRepository tipoEscalaJornadaRepository;
    private final AuditoriaRegistroAsyncService auditoriaRegistroAsyncService;

    public EmpresaJornadaPadraoService(EmpresaJornadaConfigRepository empresaJornadaConfigRepository,
                                       TipoEscalaJornadaRepository tipoEscalaJornadaRepository,
                                       AuditoriaRegistroAsyncService auditoriaRegistroAsyncService) {
        this.empresaJornadaConfigRepository = empresaJornadaConfigRepository;
        this.tipoEscalaJornadaRepository = tipoEscalaJornadaRepository;
        this.auditoriaRegistroAsyncService = auditoriaRegistroAsyncService;
    }

    @Transactional
    /** Doc id 11: Atualizar jornada padrão da empresa. */
    public void atualizar(UUID empresaId, EmpresaJornadaConfigRequest request, HttpServletRequest httpRequest) {
        var now = LocalDateTime.now();
        if (tipoEscalaJornadaRepository.existsByIdAndAtivo(request.tipoEscalaJornadaId()).isEmpty()) {
            auditoriaRegistroAsyncService.registrarSemDispositivoID(empresaId, ACAO_JORNADA_PADRAO, "Atualizar jornada padrão da empresa", null, null, false, MensagemErro.TIPO_ESCALA_JORNADA_NAO_ENCONTRADO.getMensagem(), now, httpRequest);
            throw new TipoNaoEncontradoException(MensagemErro.TIPO_ESCALA_JORNADA_NAO_ENCONTRADO);
        }

        var timezone = request.timezone() != null && !request.timezone().isBlank() ? request.timezone() : "America/Sao_Paulo";

        var existente = empresaJornadaConfigRepository.findByEmpresaId(empresaId);

        Duration tempoDescansoEntreJornada = request.tempoDescansoEntreJornada() != null ? request.tempoDescansoEntreJornada() : Duration.ofHours(11);
        Duration toleranciaPadrao = request.toleranciaPadrao() != null ? request.toleranciaPadrao() : Duration.ZERO;
        if (existente.isPresent()) {
            int updated = empresaJornadaConfigRepository.updateByEmpresaId(
                    empresaId,
                    request.tipoEscalaJornadaId(), request.cargaHorariaDiaria(), request.cargaHorariaSemanal(),
                    toleranciaPadrao, request.intervaloPadrao(), request.entradaPadrao(), request.saidaPadrao(),
                    tempoDescansoEntreJornada, timezone, request.gravaGeoObrigatoria(), request.gravaPontoApenasEmGeofence(), request.permiteAjustePonto(),
                    now);
            if (updated > 0) {
                auditoriaRegistroAsyncService.registrarSemDispositivoID(empresaId, ACAO_JORNADA_PADRAO, "Atualizar jornada padrão da empresa", null, null, true, null, now, httpRequest);
            }
        } else {
            empresaJornadaConfigRepository.insert(
                    UUID.randomUUID(), empresaId,
                    request.tipoEscalaJornadaId(), request.cargaHorariaDiaria(), request.cargaHorariaSemanal(),
                    toleranciaPadrao, request.intervaloPadrao(), request.entradaPadrao(), request.saidaPadrao(),
                    tempoDescansoEntreJornada, timezone, request.gravaGeoObrigatoria(), request.gravaPontoApenasEmGeofence(), request.permiteAjustePonto(),
                    now);
            auditoriaRegistroAsyncService.registrarSemDispositivoID(empresaId, ACAO_JORNADA_PADRAO, "Atualizar jornada padrão da empresa", null, null, true, null, now, httpRequest);
        }
    }
}
