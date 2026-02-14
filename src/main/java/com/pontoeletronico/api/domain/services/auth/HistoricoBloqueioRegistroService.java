package com.pontoeletronico.api.domain.services.auth;

import com.pontoeletronico.api.infrastructure.output.repository.auth.HistoricoBloqueioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Registra bloqueio por brute force em transação separada (REQUIRES_NEW) para persistir
 * mesmo quando a transação principal faz rollback por exceção.
 */
@Service
public class HistoricoBloqueioRegistroService {

    private final HistoricoBloqueioRepository historicoBloqueioRepository;

    public HistoricoBloqueioRegistroService(HistoricoBloqueioRepository historicoBloqueioRepository) {
        this.historicoBloqueioRepository = historicoBloqueioRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarBloqueioBruteForce(UUID usuarioId, LocalDateTime dataBloqueio, String motivoBloqueio, LocalDateTime createdAt) {
        historicoBloqueioRepository.insert(
                UUID.randomUUID(), usuarioId, dataBloqueio, motivoBloqueio, createdAt);
    }
}
