package com.pontoeletronico.api.domain.services.auth;

import com.pontoeletronico.api.domain.services.audit.AuditoriaRegistroAsyncService;
import com.pontoeletronico.api.exception.DispositivoDiferenteException;
import com.pontoeletronico.api.exception.DispositivoNaoIdentificadoException;
import com.pontoeletronico.api.exception.RefreshTokenInvalidoException;
import com.pontoeletronico.api.exception.UsuarioNaoEncontradoException;
import com.pontoeletronico.api.infrastructure.input.dto.auth.RefreshRequest;
import com.pontoeletronico.api.infrastructure.input.dto.auth.RefreshResponse;
import com.pontoeletronico.api.infrastructure.output.repository.auth.DispositivoRepository;
import com.pontoeletronico.api.infrastructure.output.repository.auth.SessaoAtivaRepository;
import com.pontoeletronico.api.infrastructure.output.repository.empresa.TipoUsuarioRepository;
import com.pontoeletronico.api.infrastructure.output.repository.usuario.UsersRepository;
import com.pontoeletronico.api.util.HttpRequestUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private static final String ACAO_REFRESH_TOKEN = "REFRESH_TOKEN";

    private final SessaoAtivaRepository sessaoAtivaRepository;
    private final UsersRepository usersRepository;
    private final DispositivoRepository dispositivoRepository;
    private final AuditoriaRegistroAsyncService auditoriaRegistroAsyncService;
    private final JwtEncoder jwtEncoder;
    private final TipoUsuarioRepository tipoUsuarioRepository;

    @Value("${app.jwt.expiration-ms:900000}")
    private long jwtExpirationMs;

    @Value("${app.jwt.refresh-expiration-ms:86400000}")
    private long refreshExpirationMs;

    public RefreshTokenService(SessaoAtivaRepository sessaoAtivaRepository, UsersRepository usersRepository,
                              DispositivoRepository dispositivoRepository,
                              AuditoriaRegistroAsyncService auditoriaRegistroAsyncService,
                              JwtEncoder jwtEncoder,
                              TipoUsuarioRepository tipoUsuarioRepository) {
        this.sessaoAtivaRepository = sessaoAtivaRepository;
        this.usersRepository = usersRepository;
        this.dispositivoRepository = dispositivoRepository;
        this.auditoriaRegistroAsyncService = auditoriaRegistroAsyncService;
        this.jwtEncoder = jwtEncoder;
        this.tipoUsuarioRepository = tipoUsuarioRepository;
    }

    /** Doc id 5: Refresh token: recebe refresh token do usuário. */
    @Transactional
    public RefreshResponse refresh(RefreshRequest request, HttpServletRequest httpRequest) {
        var dataReferencia = LocalDateTime.now();
        var sessao = sessaoAtivaRepository.findByTokenAndAtivoAndNaoExpirado(request.refreshToken(), dataReferencia)
                .orElseThrow(RefreshTokenInvalidoException::new);

        validarMesmoDispositivo(sessao.getUsuarioId(), sessao.getDispositivoId(), httpRequest);

        var user = usersRepository.findByIdQuery(sessao.getUsuarioId())
                .orElseThrow(UsuarioNaoEncontradoException::new);

        var jwtExpiresAt = Instant.now().plusSeconds(jwtExpirationMs);
        var refreshExpiresAt = Instant.now().plusSeconds(refreshExpirationMs);
        
        var scope = user.getTipoUsuarioId() != null && tipoUsuarioRepository.findDescricaoById(user.getTipoUsuarioId()) != null
                ? tipoUsuarioRepository.findDescricaoById(user.getTipoUsuarioId()) : "BASIC";
        var jwt = gerarJwt(sessao.getUsuarioId().toString(), user.getUsername(), scope, jwtExpiresAt);
        var novoRefreshToken = UUID.randomUUID().toString().replace("-", "") + Base64.getEncoder()
                .encodeToString((sessao.getUsuarioId() + ":" + refreshExpiresAt.toEpochMilli()).getBytes(StandardCharsets.UTF_8));

        sessaoAtivaRepository.desativarPorId(sessao.getId(), dataReferencia);
        sessaoAtivaRepository.insert(
                UUID.randomUUID(), sessao.getUsuarioId(), sessao.getCredencialId(), novoRefreshToken,
                sessao.getDispositivoId(), true, LocalDateTime.now().plusSeconds(refreshExpirationMs), null, dataReferencia);

        registrarAuditoriaRefreshToken(sessao.getUsuarioId(), sessao.getDispositivoId(), true, null, dataReferencia, httpRequest);

        var formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        var zone = java.time.ZoneId.of("America/Sao_Paulo");
        return new RefreshResponse(
                jwt,
                formatter.format(jwtExpiresAt.atZone(zone)),
                novoRefreshToken,
                formatter.format(refreshExpiresAt.atZone(zone))
        );
    }

    private void registrarAuditoriaRefreshToken(UUID usuarioId, UUID dispositivoId, boolean sucesso, String mensagemErro, LocalDateTime dataReferencia, HttpServletRequest httpRequest) {
        auditoriaRegistroAsyncService.registrarSemDispositivoID(usuarioId, ACAO_REFRESH_TOKEN, "Refresh de token", null, null, sucesso, mensagemErro, dataReferencia, httpRequest);
    }

    private void validarMesmoDispositivo(UUID usuarioId, UUID dispositivoIdSessao, HttpServletRequest httpRequest) {
        if (dispositivoIdSessao == null) {
            return;
        }
        if (httpRequest == null) {
            throw new DispositivoNaoIdentificadoException();
        }
        var ipAddress = HttpRequestUtils.obterIpAddress(httpRequest);
        var userAgent = HttpRequestUtils.obterUserAgent(httpRequest);
        if ((ipAddress == null || ipAddress.isBlank()) && (userAgent == null || userAgent.isBlank())) {
            throw new DispositivoNaoIdentificadoException();
        }
        var dispositivoIdAtual = dispositivoRepository.findIdByUsuarioAndIpAndUserAgent(usuarioId, ipAddress, userAgent)
                .orElseThrow(DispositivoDiferenteException::new);
        if (!dispositivoIdAtual.equals(dispositivoIdSessao)) {
            throw new DispositivoDiferenteException();
        }
    }

    private String gerarJwt(String usuarioId, String username, String scope, Instant expiresAt) {
        var claims = org.springframework.security.oauth2.jwt.JwtClaimsSet.builder()
                .issuer("ponto-eletronico-api")
                .subject(usuarioId)
                .claim("username", username)
                .claim("scope", scope)
                .issuedAt(Instant.now())
                .expiresAt(expiresAt)
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}