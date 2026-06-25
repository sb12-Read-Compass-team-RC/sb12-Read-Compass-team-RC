package com.rc.readcompass.jwt.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JWTUtil {

    private SecretKey secretKey;

    public JWTUtil(@Value("${spring.jwt.secret}") String secret) {
        this.secretKey = new SecretKeySpec(
            secret.getBytes(StandardCharsets.UTF_8),
            Jwts.SIG.HS256.key().build().getAlgorithm()
        );
    }

    /**
     * 토큰을 한 번만 파싱해 Claims 를 반환한다.
     * 서명 검증 실패/형식 오류 시 JwtException, 만료 시 ExpiredJwtException 을 던진다.
     * (호출부에서 한 번 호출해 재사용하면 요청당 4~5회 파싱하던 것을 1회로 줄일 수 있다.)
     */
    public Claims getClaims(String token) {
        return Jwts.parser().verifyWith(secretKey).build()
            .parseSignedClaims(token).getPayload();
    }

    public UUID getUserId(String token) {
        return UUID.fromString(getClaims(token).get("userId", String.class));
    }

    public String getUsername(String token) {
        return getClaims(token).get("username", String.class);
    }

    public String getRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    public Boolean isExpired(String token) {
        return getClaims(token).getExpiration().before(new Date());
    }

    public String getCategory(String token) {
        return getClaims(token).get("category", String.class);
    }

    public String createJwt(String category, UUID userId, String username, String role, Long expiredMs) {
        return Jwts.builder()
            .id(UUID.randomUUID().toString())
            .claim("userId", userId)
            .claim("category", category)
            .claim("username", username)
            .claim("role", role)
            .issuedAt(new Date(System.currentTimeMillis()))
            .expiration(new Date(System.currentTimeMillis() + expiredMs))
            .signWith(secretKey)
            .compact();
    }
}