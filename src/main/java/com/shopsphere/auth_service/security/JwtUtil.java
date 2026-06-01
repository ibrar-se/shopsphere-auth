package com.shopsphere.auth_service.security;

import com.shopsphere.auth_service.model.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKey;

    private final long ACCESS_TOKEN_VALIDITY = 1000 * 60 * 15;

    // =========================
    // Generate Signing Key
    // =========================
    private SecretKey getSigningKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // =========================
    // Generate JWT Token
    // =========================
    public String generateToken(String email, Role role, Long userId) {

        return Jwts.builder()
                .subject(email)
                .claim("role", "ROLE_" + role.name())
                .claim("userId", userId)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_VALIDITY))
                .signWith(getSigningKey())
                .compact();
    }

    // =========================
    // Extract All Claims
    // =========================
    private Claims extractAllClaims(String token) {

        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // =========================
    // Extract Single Claim
    // =========================
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {

        Claims claims = extractAllClaims(token);

        return claimsResolver.apply(claims);
    }

    // =========================
    // Extract Username (Email)
    // =========================
    public String extractUsername(String token) {

        return extractClaim(token, Claims::getSubject);
    }

    // =========================
    // Extract Expiration
    // =========================
    public Date extractExpiration(String token) {

        return extractClaim(token, Claims::getExpiration);
    }

    // =========================
    // Check Token Expired
    // =========================
    private boolean isTokenExpired(String token) {

        return extractExpiration(token).before(new Date());
    }

    // =========================
    // Validate Token
    // =========================
    public boolean validateToken(String token, String email) {

        final String extractedEmail = extractUsername(token);

        return extractedEmail.equals(email)
                && !isTokenExpired(token);
    }
}