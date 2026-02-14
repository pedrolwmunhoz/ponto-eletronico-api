package com.pontoeletronico.api.domain.services.auth;

import com.pontoeletronico.api.infrastructure.output.repository.auth.HistoricoLoginRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class HistoricoLoginRegistroService {

    private final HistoricoLoginRepository historicoLoginRepository;

    public HistoricoLoginRegistroService(HistoricoLoginRepository historicoLoginRepository) {
        this.historicoLoginRepository = historicoLoginRepository;
    }

    @Transactional
    public void registrar(UUID usuarioId, UUID credencialId, LocalDateTime dataLogin, UUID dispositivoId, boolean sucesso, LocalDateTime createdAt) {
        historicoLoginRepository.insert(
                UUID.randomUUID(), usuarioId, credencialId, dataLogin, dispositivoId, sucesso, createdAt);
    }
}
