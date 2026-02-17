package com.pontoeletronico.api.domain.services.funcionario;

import com.pontoeletronico.api.domain.enums.MensagemErro;
import com.pontoeletronico.api.domain.services.audit.AuditoriaRegistroAsyncService;
import com.pontoeletronico.api.exception.ConflitoException;
import com.pontoeletronico.api.exception.CredencialNaoEncontradaException;
import com.pontoeletronico.api.exception.FuncionarioNaoPertenceEmpresaException;
import com.pontoeletronico.api.exception.TipoCredencialNaoEncontradoException;
import com.pontoeletronico.api.infrastructure.input.dto.empresa.FuncionarioResetarEmailRequest;
import com.pontoeletronico.api.infrastructure.output.repository.auth.TipoCredentialRepository;
import com.pontoeletronico.api.infrastructure.output.repository.auth.TipoCategoriaCredentialRepository;
import com.pontoeletronico.api.infrastructure.output.repository.auth.UserCredentialRepository;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.IdentificacaoFuncionarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class FuncionarioResetarEmailService {

    private static final String TIPO_CREDENCIAL_EMAIL = "EMAIL";
    private static final String CATEGORIA_CREDENCIAL_PRIMARIO = "PRIMARIO";
    private static final String ACAO_RESETAR_EMAIL_FUNCIONARIO = "RESETAR_EMAIL_FUNCIONARIO";

    private final IdentificacaoFuncionarioRepository identificacaoFuncionarioRepository;
    private final TipoCredentialRepository tipoCredentialRepository;
    private final TipoCategoriaCredentialRepository tipoCategoriaCredentialRepository;
    private final UserCredentialRepository userCredentialRepository;
    private final AuditoriaRegistroAsyncService auditoriaRegistroAsyncService;

    public FuncionarioResetarEmailService(IdentificacaoFuncionarioRepository identificacaoFuncionarioRepository,
                                          TipoCredentialRepository tipoCredentialRepository,
                                          TipoCategoriaCredentialRepository tipoCategoriaCredentialRepository,
                                          UserCredentialRepository userCredentialRepository,
                                          AuditoriaRegistroAsyncService auditoriaRegistroAsyncService) {
        this.identificacaoFuncionarioRepository = identificacaoFuncionarioRepository;
        this.tipoCredentialRepository = tipoCredentialRepository;
        this.tipoCategoriaCredentialRepository = tipoCategoriaCredentialRepository;
        this.userCredentialRepository = userCredentialRepository;
        this.auditoriaRegistroAsyncService = auditoriaRegistroAsyncService;
    }

    @Transactional
    /** Doc id 14: Resetar email de funcionário. */
    public void resetar(UUID empresaId, UUID funcionarioId, FuncionarioResetarEmailRequest request, HttpServletRequest httpRequest) {
        var dataRef = LocalDateTime.now();
        identificacaoFuncionarioRepository.findByEmpresaIdAndFuncionarioIdAndAtivoTrue(empresaId, funcionarioId)
        .orElseThrow(() -> {
            auditoriaRegistroAsyncService.registrarSemDispositivoID(empresaId, ACAO_RESETAR_EMAIL_FUNCIONARIO, "Reset email funcionário", null, null, false, MensagemErro.FUNCIONARIO_NAO_PERTENCE_EMPRESA.getMensagem(), dataRef, httpRequest);
            throw new FuncionarioNaoPertenceEmpresaException();
        });

        var tipoEmailId = tipoCredentialRepository.findIdByDescricao(TIPO_CREDENCIAL_EMAIL);
        if (tipoEmailId == null) {
            auditoriaRegistroAsyncService.registrarSemDispositivoID(empresaId, ACAO_RESETAR_EMAIL_FUNCIONARIO, "Reset email funcionário", null, null, false, MensagemErro.TIPO_CREDENCIAL_NAO_ENCONTRADO.getMensagem(), dataRef, httpRequest);
            throw new TipoCredencialNaoEncontradoException();
        }

        var emailNormalizado = request.emailNovo().trim().toLowerCase();
        var credencialComValor = userCredentialRepository.findByValorAndTipoCredencialId(emailNormalizado, tipoEmailId);
        if (credencialComValor.isPresent() && !credencialComValor.get().getUsuarioId().equals(funcionarioId)) {
            auditoriaRegistroAsyncService.registrarSemDispositivoID(empresaId, ACAO_RESETAR_EMAIL_FUNCIONARIO, "Reset email funcionário", null, null, false, MensagemErro.EMAIL_JA_CADASTRADO.getMensagem(), dataRef, httpRequest);
            throw new ConflitoException(MensagemErro.EMAIL_JA_CADASTRADO.getMensagem());
        }

        var categoriaPrimarioId = tipoCategoriaCredentialRepository.findIdByDescricao(CATEGORIA_CREDENCIAL_PRIMARIO);
        if (categoriaPrimarioId == null) {
            auditoriaRegistroAsyncService.registrarSemDispositivoID(empresaId, ACAO_RESETAR_EMAIL_FUNCIONARIO, "Reset email funcionário", null, null, false, MensagemErro.TIPO_CREDENCIAL_NAO_ENCONTRADO.getMensagem(), dataRef, httpRequest);
            throw new TipoCredencialNaoEncontradoException();
        }
        var credencialId = userCredentialRepository.findCredencialIdByUsuarioTipoCategoria(funcionarioId, tipoEmailId, categoriaPrimarioId);
        if (credencialId.isEmpty()) {
            auditoriaRegistroAsyncService.registrarSemDispositivoID(empresaId, ACAO_RESETAR_EMAIL_FUNCIONARIO, "Reset email funcionário", null, null, false, MensagemErro.CREDENCIAL_NAO_ENCONTRADA.getMensagem(), dataRef, httpRequest);
            throw new CredencialNaoEncontradaException();
        }

        var rows = userCredentialRepository.updateValor(credencialId.get(), funcionarioId, emailNormalizado);
        if (rows == 0) {
            auditoriaRegistroAsyncService.registrarSemDispositivoID(empresaId, ACAO_RESETAR_EMAIL_FUNCIONARIO, "Reset email funcionário", null, null, false, MensagemErro.CREDENCIAL_NAO_ENCONTRADA.getMensagem(), dataRef, httpRequest);
            throw new CredencialNaoEncontradaException();
        }

        auditoriaRegistroAsyncService.registrarSemDispositivoID(empresaId, ACAO_RESETAR_EMAIL_FUNCIONARIO, "Reset email funcionário", null, null, true, null, dataRef, httpRequest);
    }
}
