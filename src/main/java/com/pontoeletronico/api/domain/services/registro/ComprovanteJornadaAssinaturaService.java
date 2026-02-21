package com.pontoeletronico.api.domain.services.registro;

import com.pontoeletronico.api.exception.FuncionarioNaoEncontradoException;
import com.pontoeletronico.api.infrastructure.input.dto.registro.AssinarComprovanteJornadaResponse;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.EmpresaComplianceRepository;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.IdentificacaoFuncionarioRepository;
import com.pontoeletronico.api.util.CertificadoA1Utils;
import com.pontoeletronico.api.util.CertificadoSenhaCriptografiaService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

/**
 * Assina o payload do comprovante PDF da jornada com o certificado A1 da empresa.
 * Não persiste nada; apenas retorna assinatura, serial e timestamp para o front embutir no PDF.
 */
@Service
public class ComprovanteJornadaAssinaturaService {

    private final IdentificacaoFuncionarioRepository identificacaoFuncionarioRepository;
    private final EmpresaComplianceRepository empresaComplianceRepository;
    private final CertificadoSenhaCriptografiaService certificadoSenhaCriptografiaService;

    public ComprovanteJornadaAssinaturaService(IdentificacaoFuncionarioRepository identificacaoFuncionarioRepository,
                                               EmpresaComplianceRepository empresaComplianceRepository,
                                               CertificadoSenhaCriptografiaService certificadoSenhaCriptografiaService) {
        this.identificacaoFuncionarioRepository = identificacaoFuncionarioRepository;
        this.empresaComplianceRepository = empresaComplianceRepository;
        this.certificadoSenhaCriptografiaService = certificadoSenhaCriptografiaService;
    }

    /**
     * Assina o payload (ex.: hash do PDF) com o certificado da empresa do funcionário.
     * Retorna assinatura, serial e timestamp; se a empresa não tiver certificado, retorna todos nulos.
     */
    public AssinarComprovanteJornadaResponse assinar(UUID funcionarioId, String payloadBase64) {
        var identificacao = identificacaoFuncionarioRepository.findFirstByFuncionarioIdAndAtivoTrue(funcionarioId)
                .orElseThrow(FuncionarioNaoEncontradoException::new);
        UUID empresaId = identificacao.getEmpresaId();

        byte[] payload;
        try {
            payload = payloadBase64 == null || payloadBase64.isBlank() ? null : Base64.getDecoder().decode(payloadBase64);
        } catch (IllegalArgumentException e) {
            payload = null;
        }
        if (payload == null || payload.length == 0) {
            return new AssinarComprovanteJornadaResponse(null, null, null);
        }

        var complianceOpt = empresaComplianceRepository.findByEmpresaId(empresaId);
        if (complianceOpt.isEmpty()) {
            return new AssinarComprovanteJornadaResponse(null, null, null);
        }
        var c = complianceOpt.get();
        byte[] certEnc = c.getCertificado();
        String senhaCripto = c.getCertificadoSenhaCriptografada();
        if (certEnc == null || certEnc.length == 0 || senhaCripto == null || senhaCripto.isBlank()) {
            return new AssinarComprovanteJornadaResponse(null, null, null);
        }

        byte[] pfxBytes = certificadoSenhaCriptografiaService.descriptografarBytes(certEnc);
        if (pfxBytes == null) pfxBytes = certEnc;
        String senha = certificadoSenhaCriptografiaService.descriptografar(senhaCripto);
        if (senha == null || pfxBytes == null || pfxBytes.length == 0) {
            return new AssinarComprovanteJornadaResponse(null, null, null);
        }

        var now = LocalDateTime.now();
        var result = CertificadoA1Utils.assinarPayload(pfxBytes, senha.toCharArray(), payload);
        if (result.isEmpty()) {
            return new AssinarComprovanteJornadaResponse(null, null, null);
        }
        return new AssinarComprovanteJornadaResponse(
                result.get().assinaturaBase64(),
                result.get().certificadoSerial(),
                now
        );
    }
}
