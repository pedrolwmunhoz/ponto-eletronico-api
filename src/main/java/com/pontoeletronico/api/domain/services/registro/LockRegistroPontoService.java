package com.pontoeletronico.api.domain.services.registro;

import com.pontoeletronico.api.exception.RegistroNaoEncontradoException;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.FuncionarioRegistroLockRepository;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.IdentificacaoFuncionarioRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Adquire lock na linha funcionario_registro_lock para serializar registro de ponto.
 * Consulta a tabela antes de qualquer tipo de registro; bloqueia concorrência por funcionário.
 */
@Service
public class LockRegistroPontoService {

    private final FuncionarioRegistroLockRepository funcionarioRegistroLockRepository;
    private final IdentificacaoFuncionarioRepository identificacaoFuncionarioRepository;

    public LockRegistroPontoService(FuncionarioRegistroLockRepository funcionarioRegistroLockRepository,
                                    IdentificacaoFuncionarioRepository identificacaoFuncionarioRepository) {
        this.funcionarioRegistroLockRepository = funcionarioRegistroLockRepository;
        this.identificacaoFuncionarioRepository = identificacaoFuncionarioRepository;
    }

    /**
     * Adquire lock para o funcionário. Deve ser chamado no início de toda operação de registro de ponto.
     * Bloqueia até obter o lock (SELECT FOR UPDATE).
     */
    public void adquirirLock(UUID funcionarioId) {
        var lock = funcionarioRegistroLockRepository.findByFuncionarioIdForUpdate(funcionarioId);
        if (lock.isEmpty()) {
            var identificacao = identificacaoFuncionarioRepository.findFirstByFuncionarioIdAndAtivoTrue(funcionarioId)
                    .orElseThrow(() -> new RegistroNaoEncontradoException("Funcionário não vinculado à empresa"));
            funcionarioRegistroLockRepository.insert(funcionarioId, identificacao.getEmpresaId());
            funcionarioRegistroLockRepository.findByFuncionarioIdForUpdate(funcionarioId)
                    .orElseThrow(() -> new RegistroNaoEncontradoException("Funcionário não vinculado à empresa"));
        }
    }
}
