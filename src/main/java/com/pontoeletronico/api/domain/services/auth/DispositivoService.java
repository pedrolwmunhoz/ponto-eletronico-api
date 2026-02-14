package com.pontoeletronico.api.domain.services.auth;

import com.pontoeletronico.api.exception.RegistroPontoInvalidoException;
import com.pontoeletronico.api.infrastructure.output.repository.auth.DispositivoRepository;
import com.pontoeletronico.api.util.HttpRequestUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class DispositivoService {

    private static final int IP_ADDRESS_MAX_LENGTH = 45;
    private static final int USER_AGENT_MAX_LENGTH = 2000;
    private static final Pattern IPV4_PATTERN = Pattern.compile(
            "^(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)){3}$");
    private static final Pattern IPV6_PATTERN = Pattern.compile("^[0-9a-fA-F:.]+$");

    private final DispositivoRepository dispositivoRepository;

    public DispositivoService(DispositivoRepository dispositivoRepository) {
        this.dispositivoRepository = dispositivoRepository;
    }

    public UUID obterOuCriar(UUID usuarioId, HttpServletRequest httpRequest) {
        if (httpRequest == null) {
            throw new RegistroPontoInvalidoException("Request não informado para identificação do dispositivo");
        }
        var ipAddress = HttpRequestUtils.obterIpAddress(httpRequest);
        var userAgent = HttpRequestUtils.obterUserAgent(httpRequest);
        return obterOuCriar(usuarioId, ipAddress, userAgent);
    }

    @Transactional
    public UUID obterOuCriar(UUID usuarioId, String ipAddress, String userAgent) {
        if ((ipAddress == null || ipAddress.isBlank()) && (userAgent == null || userAgent.isBlank())) {
            throw new RegistroPontoInvalidoException("IP e user-agent não informados para identificação do dispositivo");
        }
        if (ipAddress != null && !ipAddress.isBlank()) {
            if (ipAddress.length() > IP_ADDRESS_MAX_LENGTH) {
                throw new RegistroPontoInvalidoException("Formato de IP inválido");
            }
            if (!IPV4_PATTERN.matcher(ipAddress.trim()).matches() && !IPV6_PATTERN.matcher(ipAddress.trim()).matches()) {
                throw new RegistroPontoInvalidoException("Formato de IP inválido");
            }
        }
        if (userAgent != null && userAgent.length() > USER_AGENT_MAX_LENGTH) {
            throw new RegistroPontoInvalidoException("User-agent excede tamanho máximo permitido");
        }
        var ip = ipAddress != null ? ipAddress.trim() : null;
        var ua = userAgent != null ? userAgent.trim() : null;
        return dispositivoRepository.findIdByUsuarioAndIpAndUserAgent(usuarioId, ip, ua)
                .orElseGet(() -> {
                    var id = UUID.randomUUID();
                    dispositivoRepository.insert(id, usuarioId, ip, ua);
                    return id;
                });
    }
}
