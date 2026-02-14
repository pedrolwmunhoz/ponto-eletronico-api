package com.pontoeletronico.api.domain.services.auth;

import com.pontoeletronico.api.domain.services.audit.AuditoriaRegistroAsyncService;
import jakarta.servlet.http.HttpServletRequest;
import com.pontoeletronico.api.exception.CodigoRecuperacaoInvalidoException;
import com.pontoeletronico.api.exception.TipoTokenRecuperacaoNaoEncontradoException;
import com.pontoeletronico.api.infrastructure.input.dto.auth.ValidarCodigoRequest;
import com.pontoeletronico.api.infrastructure.input.dto.auth.ValidarCodigoResponse;
import com.pontoeletronico.api.infrastructure.output.repository.auth.CredencialTokenRecuperacaoRepository;
import com.pontoeletronico.api.infrastructure.output.repository.auth.TipoTokenRecuperacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ValidarCodigoService {

    private static final String ACAO_VALIDAR_CODIGO = "VALIDAR_CODIGO";
    private static final int EXPIRACAO_TOKEN_MINUTOS = 15;

    private final CredencialTokenRecuperacaoRepository credencialTokenRecuperacaoRepository;
    private final TipoTokenRecuperacaoRepository tipoTokenRecuperacaoRepository;
    private final AuditoriaRegistroAsyncService auditoriaRegistroAsyncService;

    public ValidarCodigoService(CredencialTokenRecuperacaoRepository credencialTokenRecuperacaoRepository,
                                TipoTokenRecuperacaoRepository tipoTokenRecuperacaoRepository,
                                AuditoriaRegistroAsyncService auditoriaRegistroAsyncService) {
        this.credencialTokenRecuperacaoRepository = credencialTokenRecuperacaoRepository;
        this.tipoTokenRecuperacaoRepository = tipoTokenRecuperacaoRepository;
        this.auditoriaRegistroAsyncService = auditoriaRegistroAsyncService;
    }

    /** Doc id 3: Validar código de recuperação. */
    @Transactional
    public ValidarCodigoResponse validar(ValidarCodigoRequest request, HttpServletRequest httpRequest) {
        var tipoCodigoId = tipoTokenRecuperacaoRepository.findIdByDescricao("CODIGO_EMAIL");
        var tipoTokenResetId = tipoTokenRecuperacaoRepository.findIdByDescricao("TOKEN_RESET");
        if (tipoCodigoId == null || tipoTokenResetId == null) {
            throw new TipoTokenRecuperacaoNaoEncontradoException();
        }
        var dataReferencia = LocalDateTime.now();
        var registro = credencialTokenRecuperacaoRepository.findByTokenAndTipoAndAtivoAndNaoExpirado(
                        request.codigo().trim(), tipoCodigoId, dataReferencia)
                .orElseThrow(CodigoRecuperacaoInvalidoException::new);

        credencialTokenRecuperacaoRepository.desativar(registro.getId(), dataReferencia);

        var novoToken = UUID.randomUUID().toString().replace("-", "");
        var novaExpiracao = dataReferencia.plusMinutes(EXPIRACAO_TOKEN_MINUTOS);
        credencialTokenRecuperacaoRepository.insert(
                UUID.randomUUID(), registro.getUsuarioId(), tipoTokenResetId, novoToken, novaExpiracao, dataReferencia);

        registrarAuditoriaValidarCodigo(registro.getUsuarioId(), true, null, dataReferencia, httpRequest);
        return new ValidarCodigoResponse(novoToken);
    }

    private void registrarAuditoriaValidarCodigo(UUID usuarioId, boolean sucesso, String mensagemErro, LocalDateTime dataReferencia, HttpServletRequest httpRequest) {
        auditoriaRegistroAsyncService.registrarSemDispositivoID(usuarioId, ACAO_VALIDAR_CODIGO, "Validação de código de recuperação", null, null, sucesso, mensagemErro, dataReferencia, httpRequest);
    }
}
