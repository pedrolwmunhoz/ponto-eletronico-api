package com.pontoeletronico.api.domain.services.perfil;

import com.pontoeletronico.api.domain.services.audit.AuditoriaRegistroAsyncService;
import com.pontoeletronico.api.exception.UsuarioNaoEncontradoException;
import com.pontoeletronico.api.infrastructure.input.dto.perfil.EmpresaPerfilResponse;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.EmpresaPerfilProjection;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.EmpresaPerfilRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class EmpresaPerfilService {

    private static final String ACAO_PERFIL_EMPRESA = "ACESSO_PERFIL_EMPRESA";

    private final EmpresaPerfilRepository empresaPerfilRepository;
    private final AuditoriaRegistroAsyncService auditoriaRegistroAsyncService;

    public EmpresaPerfilService(EmpresaPerfilRepository empresaPerfilRepository,
                                AuditoriaRegistroAsyncService auditoriaRegistroAsyncService) {
        this.empresaPerfilRepository = empresaPerfilRepository;
        this.auditoriaRegistroAsyncService = auditoriaRegistroAsyncService;
    }

    /** Converte string do banco (ISO-8601 ou nanossegundos numéricos) para Duration. */
    private static Duration parseDuration(String raw) {
        if (raw == null || raw.isBlank()) return Duration.ZERO;
        String s = raw.trim();
        if (s.startsWith("PT") || s.startsWith("-PT")) return Duration.parse(s);
        try {
            long nanos = Long.parseLong(s);
            return Duration.ofNanos(nanos);
        } catch (NumberFormatException e) {
            return Duration.ZERO;
        }
    }

    /** Doc id 27: Recuperar informações da empresa. */
    public EmpresaPerfilResponse buscar(UUID empresaId, HttpServletRequest httpRequest) {
        EmpresaPerfilProjection p = empresaPerfilRepository.findPerfilByEmpresaId(empresaId)
                .orElseThrow(UsuarioNaoEncontradoException::new);
        auditoriaRegistroAsyncService.registrarSemDispositivoID(empresaId, ACAO_PERFIL_EMPRESA, "Acesso ao perfil da empresa", null, null, true, null, LocalDateTime.now(), httpRequest);
        return new EmpresaPerfilResponse(
                p.getUsername(),
                p.getCnpj(),
                p.getRazaoSocial(),
                p.getEmail(),
                p.getTelefoneId(),
                p.getCodigoPais(),
                p.getDdd(),
                p.getNumero(),
                p.getRua(),
                p.getNumeroEndereco(),
                p.getComplemento(),
                p.getBairro(),
                p.getCidade(),
                p.getUf(),
                p.getCep(),
                p.getTimezone(),
                parseDuration(p.getCargaDiariaPadrao()),
                parseDuration(p.getCargaSemanalPadrao()),
                parseDuration(p.getToleranciaPadrao()),
                parseDuration(p.getIntervaloPadrao()),
                p.getControlePontoObrigatorio(),
                p.getTipoModeloPonto(),
                p.getTempoRetencao(),
                p.getAuditoriaAtiva(),
                p.getAssinaturaDigitalObrigatoria(),
                p.getGravarGeolocalizacaoObrigatoria(),
                p.getPermitirAjustePontoDireto(),
                p.getDataExpiracaoCertificado()
        );
    }
}
