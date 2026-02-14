package com.pontoeletronico.api.domain.services.empresa;

import com.pontoeletronico.api.domain.services.audit.AuditoriaRegistroAsyncService;
import com.pontoeletronico.api.infrastructure.input.dto.empresa.EmpresaBancoHorasConfigRequest;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.EmpresaBancoHorasConfigRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class EmpresaBancoHorasConfigService {

    private static final String ACAO_BANCO_HORAS_CONFIG = "ATUALIZAR_BANCO_HORAS_CONFIG_EMPRESA";

    private final EmpresaBancoHorasConfigRepository empresaBancoHorasConfigRepository;
    private final AuditoriaRegistroAsyncService auditoriaRegistroAsyncService;

    public EmpresaBancoHorasConfigService(EmpresaBancoHorasConfigRepository empresaBancoHorasConfigRepository,
                                         AuditoriaRegistroAsyncService auditoriaRegistroAsyncService) {
        this.empresaBancoHorasConfigRepository = empresaBancoHorasConfigRepository;
        this.auditoriaRegistroAsyncService = auditoriaRegistroAsyncService;
    }

    @Transactional
    /** Atualizar configuração de banco de horas da empresa. Insere se não existir. */
    public void atualizar(UUID empresaId, EmpresaBancoHorasConfigRequest request, HttpServletRequest httpRequest) {
        var now = LocalDateTime.now();
        var existente = empresaBancoHorasConfigRepository.findByEmpresaId(empresaId);

        if (existente.isPresent()) {
            empresaBancoHorasConfigRepository.updateByEmpresaId(
                    empresaId, request.ativo(), request.totalDiasVencimento(), now);
        } else {
            empresaBancoHorasConfigRepository.insert(
                    UUID.randomUUID(), empresaId, request.ativo(), request.totalDiasVencimento(), now);
        }
        auditoriaRegistroAsyncService.registrarSemDispositivoID(empresaId, ACAO_BANCO_HORAS_CONFIG,
                "Atualizar configuração de banco de horas da empresa", null, null, true, null, now, httpRequest);
    }
}
