package com.pontoeletronico.api.domain.services.empresa;

import com.pontoeletronico.api.domain.enums.MensagemErro;
import com.pontoeletronico.api.domain.services.audit.AuditoriaRegistroAsyncService;
import com.pontoeletronico.api.exception.EmpresaNaoEncontradaException;
import com.pontoeletronico.api.infrastructure.input.dto.empresa.EmpresaEnderecoRequest;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.EmpresaDadosFiscalRepository;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.EmpresaEnderecoRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class EmpresaAtualizarEnderecoService {

    private static final String ACAO_ATUALIZAR_ENDERECO = "ATUALIZAR_ENDERECO_EMPRESA";

    private final EmpresaEnderecoRepository empresaEnderecoRepository;
    private final EmpresaDadosFiscalRepository empresaDadosFiscalRepository;
    private final AuditoriaRegistroAsyncService auditoriaRegistroAsyncService;

    public EmpresaAtualizarEnderecoService(EmpresaEnderecoRepository empresaEnderecoRepository,
                                          EmpresaDadosFiscalRepository empresaDadosFiscalRepository,
                                          AuditoriaRegistroAsyncService auditoriaRegistroAsyncService) {
        this.empresaEnderecoRepository = empresaEnderecoRepository;
        this.empresaDadosFiscalRepository = empresaDadosFiscalRepository;
        this.auditoriaRegistroAsyncService = auditoriaRegistroAsyncService;
    }

    @Transactional
    /** Doc id 8: Atualizar endereço da empresa. */
    public void atualizar(UUID empresaId, EmpresaEnderecoRequest request, HttpServletRequest httpRequest) {
        var cepNormalizado = request.cep().replaceAll("\\D", "");
        var uf = request.uf().toUpperCase();
        var now = LocalDateTime.now();

        var endereco = empresaEnderecoRepository.findByEmpresaId(empresaId);
        if (endereco.isEmpty()) {
            if (empresaDadosFiscalRepository.existsByEmpresaId(empresaId).isEmpty()) {
                auditoriaRegistroAsyncService.registrarSemDispositivoID(empresaId, ACAO_ATUALIZAR_ENDERECO, "Atualizar endereço da empresa", null, null, false, MensagemErro.EMPRESA_NAO_ENCONTRADA.getMensagem(), now, httpRequest);
                throw new EmpresaNaoEncontradaException();
            }
            empresaEnderecoRepository.insert(
                    UUID.randomUUID(),
                    empresaId,
                    request.rua(), request.numero(), request.complemento(),
                    request.bairro(), request.cidade(), uf, cepNormalizado,
                    now);
        } else {
            empresaEnderecoRepository.updateByEmpresaId(
                    empresaId,
                    request.rua(), request.numero(), request.complemento(),
                    request.bairro(), request.cidade(), uf, cepNormalizado,
                    now);
        }

        auditoriaRegistroAsyncService.registrarSemDispositivoID(empresaId, ACAO_ATUALIZAR_ENDERECO, "Atualizar endereço da empresa", null, null, true, null, now, httpRequest);
    }
}
