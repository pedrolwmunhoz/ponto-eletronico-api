package com.pontoeletronico.api.domain.services.auth;

import com.pontoeletronico.api.domain.enums.MensagemErro;
import com.pontoeletronico.api.exception.CredencialInvalidaException;
import com.pontoeletronico.api.exception.SenhaExpiradaException;
import com.pontoeletronico.api.exception.TentativasExcedidasException;
import com.pontoeletronico.api.exception.UsuarioBloqueadoException;
import com.pontoeletronico.api.exception.UsuarioInativoException;
import com.pontoeletronico.api.infrastructure.input.dto.auth.LoginRequest;
import com.pontoeletronico.api.infrastructure.input.dto.auth.LoginResponse;
import com.pontoeletronico.api.infrastructure.output.repository.auth.AuthLoginProjection;
import com.pontoeletronico.api.infrastructure.output.repository.auth.AuthRepository;
import com.pontoeletronico.api.infrastructure.output.repository.auth.HistoricoLoginRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.UUID;

@Service
public class AuthService {

    private final AuthRepository authRepository;
    private final HistoricoLoginRepository historicoLoginRepository;
    private final HistoricoBloqueioRegistroService historicoBloqueioRegistroService;
    private final LoginRegistroAsyncService loginRegistroAsyncService;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;

    @Value("${app.jwt.expiration-ms:900000}")
    private long jwtExpirationMs;

    @Value("${app.jwt.refresh-expiration-ms:86400000}")
    private long refreshExpirationMs;

    @Value("${app.brute-force.max-tentativas:5}")
    private int maxTentativas;

    @Value("${app.brute-force.janela-minutos:15}")
    private int janelaMinutos;

    public AuthService(AuthRepository authRepository,
                       HistoricoLoginRepository historicoLoginRepository,
                       HistoricoBloqueioRegistroService historicoBloqueioRegistroService,
                       LoginRegistroAsyncService loginRegistroAsyncService,
                       PasswordEncoder passwordEncoder,
                       JwtEncoder jwtEncoder) {
        this.authRepository = authRepository;
        this.historicoLoginRepository = historicoLoginRepository;
        this.historicoBloqueioRegistroService = historicoBloqueioRegistroService;
        this.loginRegistroAsyncService = loginRegistroAsyncService;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
    }

    /** Doc id 1: Login de usuário. */
    public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        var dataReferencia = LocalDateTime.now();
        AuthLoginProjection credencial = authRepository.findCredencialParaLogin(request.valor(), request.tipoCredencial().getName())
                .orElseThrow(CredencialInvalidaException::new);

        if (Boolean.TRUE.equals(credencial.getBloqueio())) {
            loginRegistroAsyncService.registrar(credencial, httpRequest, null, null, false, MensagemErro.USUARIO_BLOQUEADO.getMensagem(), dataReferencia);
            throw new UsuarioBloqueadoException();
        }

        var desde = dataReferencia.minusMinutes(janelaMinutos);
        if (credencial.getBloqueio().equals(true)) {
            loginRegistroAsyncService.registrar(credencial, httpRequest, null, null, false, MensagemErro.TENTATIVAS_EXCEDIDAS.getMensagem(), dataReferencia);
            throw new TentativasExcedidasException();
        }

        if (!passwordEncoder.matches(request.senha(), credencial.getSenhaHash())) {
            loginRegistroAsyncService.registrar(credencial, httpRequest, null, null, false, MensagemErro.CREDENCIAL_INVALIDA.getMensagem(), dataReferencia);
            var quantidadeFalhasRecentes = historicoLoginRepository.countFalhasRecentes(credencial.getCredencialId(), desde);
            if (quantidadeFalhasRecentes >= maxTentativas && !credencial.getBloqueio().equals(true)) {
                historicoBloqueioRegistroService.registrarBloqueioBruteForce(
                        credencial.getUsuarioId(), dataReferencia, MensagemErro.TENTATIVAS_EXCEDIDAS.getMensagem(), dataReferencia);
                loginRegistroAsyncService.registrar(credencial, httpRequest, null, null, false, MensagemErro.TENTATIVAS_EXCEDIDAS.getMensagem(), dataReferencia);
                throw new TentativasExcedidasException();
            }
            throw new CredencialInvalidaException();
        }

        if (!Boolean.TRUE.equals(credencial.getAtivo())) {
            loginRegistroAsyncService.registrar(credencial, httpRequest, null, null, false, "Usuário inativo", dataReferencia);
            throw new UsuarioInativoException();
        }

        if (Boolean.TRUE.equals(credencial.getSenhaExpirada())) {
            loginRegistroAsyncService.registrar(credencial, httpRequest, null, null, false, MensagemErro.SENHA_EXPIRADA.getMensagem(), dataReferencia);
            throw new SenhaExpiradaException();
        }

        var jwtExpiresAt = Instant.now().plusSeconds(jwtExpirationMs);
        var refreshExpiresAt = dataReferencia.plusSeconds(refreshExpirationMs);
        var scope = credencial.getTipoDescricao() != null ? credencial.getTipoDescricao() : "BASIC";
        var jwt = gerarJwt(credencial.getUsuarioId().toString(), credencial.getUsername(), scope, jwtExpiresAt);
        var refreshToken = UUID.randomUUID().toString().replace("-", "") + Base64.getEncoder().encodeToString(
                (credencial.getUsuarioId() + ":" + Instant.now().plusSeconds(refreshExpirationMs).toEpochMilli()).getBytes(StandardCharsets.UTF_8));

        loginRegistroAsyncService.registrar(credencial, httpRequest, refreshToken, refreshExpiresAt, true, null, dataReferencia);

        var formatter = DateTimeFormatter.ISO_INSTANT;
        return new LoginResponse(
                jwt,
                formatter.format(jwtExpiresAt),
                refreshToken,
                DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss").format(refreshExpiresAt)
        );
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
