package com.rc.readcompass.jwt.filter;

import com.rc.readcompass.jwt.TokenType;
import com.rc.readcompass.jwt.dto.AuthDto;
import com.rc.readcompass.jwt.entity.CustomUserDetails;
import com.rc.readcompass.jwt.util.JWTUtil;
import com.rc.readcompass.jwt.util.JwtHeaders;
import com.rc.readcompass.user.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

@AllArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;

    // 재발급 요청은 만료된 access 토큰을 들고 와도 통과해야 하므로 이 필터를 건너뛴다.
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return "/api/users/reissue".equals(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {

        // Authorization: Bearer <token> 에서 토큰을 꺼낸다.
        // (기존 빌드된 프론트는 access 헤더로 보내므로 fallback 으로 함께 처리)
        String accessToken = resolveToken(request);

        if (accessToken == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // 한 번만 파싱. 만료 / 위조·형식오류를 모두 401 로 처리한다.
        Claims claims;
        try {
            claims = jwtUtil.getClaims(accessToken);
        } catch (ExpiredJwtException e) {
            unauthorized(response, "access token expired");
            return;
        } catch (JwtException | IllegalArgumentException e) {
            unauthorized(response, "invalid access token");
            return;
        }

        if (!TokenType.ACCESS.category().equals(claims.get("category", String.class))) {
            unauthorized(response, "invalid access token");
            return;
        }

        UUID userId     = UUID.fromString(claims.get("userId", String.class));
        String username = claims.get("username", String.class);
        String role     = claims.get("role", String.class);

        AuthDto authDto = new AuthDto();
        authDto.setId(userId);
        authDto.setUsername(username);
        authDto.setPassword(null);
        authDto.setUserRole(parseRole(role));

        CustomUserDetails customUserDetails = new CustomUserDetails(authDto);

        Authentication authToken = new UsernamePasswordAuthenticationToken(
            customUserDetails, null, customUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }

    /**
     * Authorization 헤더(Bearer 방식)를 우선 읽고, 없으면 기존 access 헤더를 읽는다.
     *   예) Authorization: Bearer eyJhbGci...   ->   eyJhbGci...
     */
    private String resolveToken(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            return auth.substring(7);   // "Bearer " (7글자) 제거
        }
        // 하위 호환: 기존 빌드된 프론트는 토큰을 access 헤더로 보낸다.
        return request.getHeader(JwtHeaders.LEGACY_ACCESS_HEADER);
    }

    private void unauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().print(message);
    }

    private UserRole parseRole(String role) {
        return UserRole.fromAuthority(role);
    }
}