package com.pontoeletronico.api.domain.services.funcionario;

import com.pontoeletronico.api.domain.enums.MensagemErro;
import com.pontoeletronico.api.domain.services.audit.AuditoriaRegistroAsyncService;
import com.pontoeletronico.api.domain.services.usuario.UsuarioDesbloquearService;

import jakarta.servlet.http.HttpServletRequest;
import com.pontoeletronico.api.exception.FuncionarioNaoPertenceEmpresaException;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.IdentificacaoFuncionarioRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class FuncionarioDesbloquearService {

    private static final String ACAO_DESBLOQUEAR_FUNCIONARIO = "DESBLOQUEAR_FUNCIONARIO";

    private final IdentificacaoFuncionarioRepository identificacaoFuncionarioRepository;
    private final UsuarioDesbloquearService desbloquearUsuarioService;
    private final AuditoriaRegistroAsyncService auditoriaRegistroAsyncService;

    public FuncionarioDesbloquearService(IdentificacaoFuncionarioRepository identificacaoFuncionarioRepository,
                                                UsuarioDesbloquearService desbloquearUsuarioService,
                                                AuditoriaRegistroAsyncService auditoriaRegistroAsyncService) {
        this.identificacaoFuncionarioRepository = identificacaoFuncionarioRepository;
        this.desbloquearUsuarioService = desbloquearUsuarioService;
        this.auditoriaRegistroAsyncService = auditoriaRegistroAsyncService;
    }

    /** Doc id 15: Desbloquear funcionário bloqueado (brute force ou manual). */
    public void desbloquear(UUID empresaId, UUID funcionarioId, HttpServletRequest httpRequest) {
        var dataReferencia = LocalDateTime.now();
        identificacaoFuncionarioRepository.findByEmpresaIdAndFuncionarioIdAndAtivoTrue(empresaId, funcionarioId)
        .orElseThrow(() -> {
            registrarAuditoriaDesbloquearFuncionario(empresaId, false, MensagemErro.FUNCIONARIO_NAO_PERTENCE_EMPRESA.getMensagem(), dataReferencia, httpRequest);
            throw new FuncionarioNaoPertenceEmpresaException();
        });
        desbloquearUsuarioService.desbloquear(funcionarioId);
        registrarAuditoriaDesbloquearFuncionario(empresaId, true, null, dataReferencia, httpRequest);
    }

    private void registrarAuditoriaDesbloquearFuncionario(UUID empresaId, boolean sucesso, String mensagemErro, LocalDateTime dataReferencia, HttpServletRequest httpRequest) {
        auditoriaRegistroAsyncService.registrarSemDispositivoID(empresaId, ACAO_DESBLOQUEAR_FUNCIONARIO, "Desbloqueio de funcionário pela empresa", null, null, sucesso, mensagemErro, dataReferencia, httpRequest);
    }
}
