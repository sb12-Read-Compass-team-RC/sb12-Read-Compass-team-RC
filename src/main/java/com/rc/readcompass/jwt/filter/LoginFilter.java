package com.rc.readcompass.jwt.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rc.readcompass.common.Define;
import com.rc.readcompass.jwt.entity.CustomUserDetails;
import com.rc.readcompass.jwt.service.RefreshTokenService;
import com.rc.readcompass.jwt.util.CookieUtil;
import com.rc.readcompass.jwt.util.JWTUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    // 토큰 만료 시간(ms)은 환경별 application-*.yml 에서 주입받아 SecurityConfig 가 생성자로 전달한다.
    private final long accessExpireMs;
    private final long refreshExpireMs;

    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final CookieUtil cookieUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public LoginFilter(AuthenticationManager authenticationManager,
        JWTUtil jwtUtil,
        RefreshTokenService refreshTokenService,
        CookieUtil cookieUtil,
        long accessExpireMs,
        long refreshExpireMs) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
        this.cookieUtil = cookieUtil;
        this.accessExpireMs = accessExpireMs;
        this.refreshExpireMs = refreshExpireMs;
        setFilterProcessesUrl("/api/users/login");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
        throws AuthenticationException {
        try {
            Map<String, String> body =
                objectMapper.readValue(request.getInputStream(), Map.class);

            String email = body.get("email");
            String password = body.get("password");

            UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(email, password, null);

            return authenticationManager.authenticate(authToken);
        } catch (IOException e) {
            throw new RuntimeException("로그인 요청 파싱 실패", e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
        FilterChain chain, Authentication authentication) throws IOException {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        UUID userId = userDetails.getUserId();
        String username = userDetails.getUsername();
        String role = userDetails.getAuthorities().stream()
            .findFirst()
            .map(a -> a.getAuthority())
            .orElse("ROLE_USER");

        // 토큰 발급
        String access = jwtUtil.createJwt("access", userId, username, role, accessExpireMs);
        String refresh = jwtUtil.createJwt(Define.refresh, userId, username, role, refreshExpireMs);

        // Refresh 저장 — rotate(): 유저의 기존 토큰 제거 후 새로 저장 (OAuth 로그인과 동일하게 통일)
        Instant expiry = Instant.now().plusMillis(refreshExpireMs);
        refreshTokenService.rotate(userId, refresh, expiry);

        // 응답: Authorization: Bearer <access> (표준) + refresh 쿠키
        // access 헤더도 함께 내려 기존 빌드된 프론트와 호환을 유지한다.
        response.setHeader("Authorization", "Bearer " + access);
        response.setHeader("access", access);
        response.addCookie(cookieUtil.createRefresh(refresh));

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), Map.of(
            "id", userId.toString(),
            "nickname", username,
            "role", role
        ));
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
        AuthenticationException failed) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"message\":\"이메일 또는 비밀번호가 올바르지 않습니다.\"}");
    }
}