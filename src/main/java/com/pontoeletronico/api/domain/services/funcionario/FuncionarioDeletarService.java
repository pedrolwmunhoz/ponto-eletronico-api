package com.pontoeletronico.api.domain.services.funcionario;

import com.pontoeletronico.api.domain.enums.MensagemErro;
import com.pontoeletronico.api.domain.services.audit.AuditoriaRegistroAsyncService;
import com.pontoeletronico.api.domain.services.empresa.MetricasDiariaEmpresaContadorService;
import com.pontoeletronico.api.exception.FuncionarioNaoPertenceEmpresaException;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.IdentificacaoFuncionarioRepository;
import com.pontoeletronico.api.infrastructure.output.repository.usuario.UsersRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class FuncionarioDeletarService {

    private static final String ACAO_DELETAR_FUNCIONARIO = "DELETAR_FUNCIONARIO";

    private final IdentificacaoFuncionarioRepository identificacaoFuncionarioRepository;
    private final UsersRepository usersRepository;
    private final AuditoriaRegistroAsyncService auditoriaRegistroAsyncService;
    private final MetricasDiariaEmpresaContadorService metricasDiariaEmpresaContadorService;

    public FuncionarioDeletarService(IdentificacaoFuncionarioRepository identificacaoFuncionarioRepository,
                                    UsersRepository usersRepository,
                                    AuditoriaRegistroAsyncService auditoriaRegistroAsyncService,
                                    MetricasDiariaEmpresaContadorService metricasDiariaEmpresaContadorService) {
        this.identificacaoFuncionarioRepository = identificacaoFuncionarioRepository;
        this.usersRepository = usersRepository;
        this.auditoriaRegistroAsyncService = auditoriaRegistroAsyncService;
        this.metricasDiariaEmpresaContadorService = metricasDiariaEmpresaContadorService;
    }

    @Transactional
    /** Doc id 17: Deletar funcionário. */
    public void deletar(UUID empresaId, UUID funcionarioId, HttpServletRequest httpRequest) {
        var dataRef = LocalDateTime.now();
        identificacaoFuncionarioRepository.findByEmpresaIdAndFuncionarioIdAndAtivoTrue(empresaId, funcionarioId)
        .orElseThrow(() -> {
            auditoriaRegistroAsyncService.registrarSemDispositivoID(empresaId, ACAO_DELETAR_FUNCIONARIO, "Deletar funcionário", null, null, false, MensagemErro.FUNCIONARIO_NAO_PERTENCE_EMPRESA.getMensagem(), dataRef, httpRequest);
            throw new FuncionarioNaoPertenceEmpresaException();
        });

        var rows = usersRepository.desativarUsuario(funcionarioId, dataRef);
        if (rows == 0) {
            auditoriaRegistroAsyncService.registrarSemDispositivoID(empresaId, ACAO_DELETAR_FUNCIONARIO, "Deletar funcionário", null, null, false, "Funcionário já inativo.", dataRef, httpRequest);
            throw new FuncionarioNaoPertenceEmpresaException();
        }

        auditoriaRegistroAsyncService.registrarSemDispositivoID(empresaId, ACAO_DELETAR_FUNCIONARIO, "Deletar funcionário", null, null, true, null, dataRef, httpRequest);
        metricasDiariaEmpresaContadorService.decrementarQuantidadeFuncionarios(empresaId);
    }
}
