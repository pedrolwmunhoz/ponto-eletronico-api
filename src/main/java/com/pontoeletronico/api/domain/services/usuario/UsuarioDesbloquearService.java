package com.pontoeletronico.api.domain.services.usuario;

import com.pontoeletronico.api.exception.BloqueioNaoEncontradoException;
import com.pontoeletronico.api.infrastructure.output.repository.auth.HistoricoBloqueioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UsuarioDesbloquearService {

    private final HistoricoBloqueioRepository historicoBloqueioRepository;

    public UsuarioDesbloquearService(HistoricoBloqueioRepository historicoBloqueioRepository) {
        this.historicoBloqueioRepository = historicoBloqueioRepository;
    }

    @Transactional
    public void desbloquear(UUID usuarioId) {
        var dataDesativacao = LocalDateTime.now();
        var rows = historicoBloqueioRepository.desativarPorUsuarioId(usuarioId, dataDesativacao);
        if (rows == 0) {
            throw new BloqueioNaoEncontradoException();
        }
    }
}
