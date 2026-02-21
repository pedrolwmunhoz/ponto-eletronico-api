package com.pontoeletronico.api.domain.services.registro;

import com.pontoeletronico.api.domain.entity.registro.RegistroMetadados;
import com.pontoeletronico.api.domain.entity.registro.RegistroPonto;
import com.pontoeletronico.api.infrastructure.input.dto.registro.RegistroMetadadosRequest;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.EmpresaComplianceRepository;
import com.pontoeletronico.api.infrastructure.output.repository.registro.RegistroMetadadosRepository;
import com.pontoeletronico.api.util.CertificadoA1Utils;
import com.pontoeletronico.api.util.CertificadoSenhaCriptografiaService;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Grava metadados do registro de ponto e, se a empresa tiver certificado A1, assina digitalmente.
 */
@Service
public class MetadadosEAssinaturaRegistroPontoService {

    private final EmpresaComplianceRepository empresaComplianceRepository;
    private final RegistroMetadadosRepository registroMetadadosRepository;
    private final CertificadoSenhaCriptografiaService certificadoSenhaCriptografiaService;

    public MetadadosEAssinaturaRegistroPontoService(EmpresaComplianceRepository empresaComplianceRepository,
                                                     RegistroMetadadosRepository registroMetadadosRepository,
                                                     CertificadoSenhaCriptografiaService certificadoSenhaCriptografiaService) {
        this.empresaComplianceRepository = empresaComplianceRepository;
        this.registroMetadadosRepository = registroMetadadosRepository;
        this.certificadoSenhaCriptografiaService = certificadoSenhaCriptografiaService;
    }

    public void gravar(UUID empresaId, RegistroPonto registroPonto, RegistroMetadadosRequest request) {
        Double lat = request != null ? request.geoLatitude() : null;
        Double lon = request != null ? request.geoLongitude() : null;
        gravar(empresaId, registroPonto, lat, lon);
    }

    public void gravar(UUID empresaId, RegistroPonto registroPonto, Double geoLatitude, Double geoLongitude) {
        var now = LocalDateTime.now();
        String assinaturaBase64 = null;
        String certificadoSerial = null;
        LocalDateTime timestampAssinatura = null;

        var complianceOpt = empresaComplianceRepository.findByEmpresaId(empresaId);
        if (complianceOpt.isPresent()) {
            var c = complianceOpt.get();
            byte[] certEnc = c.getCertificado();
            String senhaCripto = c.getCertificadoSenhaCriptografada();
            if (certEnc != null && certEnc.length > 0 && senhaCripto != null && !senhaCripto.isBlank()) {
                byte[] pfxBytes = certificadoSenhaCriptografiaService.descriptografarBytes(certEnc);
                if (pfxBytes == null) pfxBytes = certEnc;
                String senha = certificadoSenhaCriptografiaService.descriptografar(senhaCripto);
                if (senha != null && pfxBytes != null && pfxBytes.length > 0) {
                    String payloadStr = registroPonto.getId() + "|" + registroPonto.getCreatedAt() + "|" + registroPonto.getUsuarioId()
                            + "|" + (geoLatitude != null ? geoLatitude : "") + "|" + (geoLongitude != null ? geoLongitude : "");
                    byte[] payload = payloadStr.getBytes(StandardCharsets.UTF_8);
                    var result = CertificadoA1Utils.assinarPayload(pfxBytes, senha.toCharArray(), payload);
                    if (result.isPresent()) {
                        assinaturaBase64 = result.get().assinaturaBase64();
                        certificadoSerial = result.get().certificadoSerial();
                        timestampAssinatura = now;
                    }
                }
            }
        }

        var m = new RegistroMetadados();
        m.setId(UUID.randomUUID());
        m.setRegistroId(registroPonto.getId());
        m.setGeoLatitude(geoLatitude);
        m.setGeoLongitude(geoLongitude);
        m.setAssinaturaDigital(assinaturaBase64);
        m.setCertificadoSerial(certificadoSerial);
        m.setTimestampAssinatura(timestampAssinatura);
        m.setUpdatedAt(now);
        registroMetadadosRepository.save(m);
    }
}
