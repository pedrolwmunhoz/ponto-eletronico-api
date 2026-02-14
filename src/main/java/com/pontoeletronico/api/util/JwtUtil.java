package com.pontoeletronico.api.util;

import com.pontoeletronico.api.exception.AutorizacaoInvalidaException;
import com.pontoeletronico.api.exception.TokenJwtInvalidoException;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class JwtUtil {

    private final JwtDecoder jwtDecoder;

    public JwtUtil(JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }

    public UUID extractUserIdFromToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        try {
            String subject = jwtDecoder.decode(token).getSubject();
            return UUID.fromString(subject);
        } catch (Exception e) {
            throw new TokenJwtInvalidoException();
        }
    }

    public String extractScopeFromToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return jwtDecoder.decode(token).getClaimAsString("scope");
    }
}
