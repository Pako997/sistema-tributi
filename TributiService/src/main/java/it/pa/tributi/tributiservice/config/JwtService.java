package it.pa.tributi.tributiservice.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import io.jsonwebtoken.security.Keys;

@Component
public class JwtService {

    private final SecretKey chiave;

    public JwtService(@Value("${jwt.secret}") String secret) {
        this.chiave = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public Optional<String> estraiCodiceFiscale(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(chiave)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Optional.ofNullable(claims.getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}