package com.pontoeletronico.api.domain.services.funcionario;

import com.pontoeletronico.api.domain.enums.MensagemErro;
import com.pontoeletronico.api.domain.services.audit.AuditoriaRegistroAsyncService;
import com.pontoeletronico.api.exception.FuncionarioNaoPertenceEmpresaException;
import com.pontoeletronico.api.infrastructure.input.dto.empresa.FuncionarioResetarSenhaRequest;
import com.pontoeletronico.api.infrastructure.output.repository.auth.UserPasswordRepository;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.IdentificacaoFuncionarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class FuncionarioResetarSenhaService {

    private static final String ACAO_RESETAR_SENHA_FUNCIONARIO = "RESETAR_SENHA_FUNCIONARIO";

    private final IdentificacaoFuncionarioRepository identificacaoFuncionarioRepository;
    private final UserPasswordRepository userPasswordRepository;
    private final AuditoriaRegistroAsyncService auditoriaRegistroAsyncService;
    private final PasswordEncoder passwordEncoder;

    public FuncionarioResetarSenhaService(IdentificacaoFuncionarioRepository identificacaoFuncionarioRepository,
                                          UserPasswordRepository userPasswordRepository,
                                          AuditoriaRegistroAsyncService auditoriaRegistroAsyncService,
                                          PasswordEncoder passwordEncoder) {
        this.identificacaoFuncionarioRepository = identificacaoFuncionarioRepository;
        this.userPasswordRepository = userPasswordRepository;
        this.auditoriaRegistroAsyncService = auditoriaRegistroAsyncService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    /** Doc id 13: Resetar senha de funcionário. */
    public void resetar(UUID empresaId, UUID funcionarioId, FuncionarioResetarSenhaRequest request, HttpServletRequest httpRequest) {
        var dataRef = LocalDateTime.now();
        identificacaoFuncionarioRepository.findByEmpresaIdAndFuncionarioIdAndAtivoTrue(empresaId, funcionarioId)
        .orElseThrow(() -> {
            auditoriaRegistroAsyncService.registrarSemDispositivoID(empresaId, ACAO_RESETAR_SENHA_FUNCIONARIO, "Reset senha funcionário", null, null, false, MensagemErro.FUNCIONARIO_NAO_PERTENCE_EMPRESA.getMensagem(), dataRef, httpRequest);
            throw new FuncionarioNaoPertenceEmpresaException();
        });

        userPasswordRepository.desativarByUsuarioId(funcionarioId, dataRef);
        userPasswordRepository.insert(UUID.randomUUID(), funcionarioId, passwordEncoder.encode(request.senhaNova()), dataRef);
        auditoriaRegistroAsyncService.registrarSemDispositivoID(empresaId, ACAO_RESETAR_SENHA_FUNCIONARIO, "Reset senha funcionário", null, null, true, null, dataRef, httpRequest);
    }
}
