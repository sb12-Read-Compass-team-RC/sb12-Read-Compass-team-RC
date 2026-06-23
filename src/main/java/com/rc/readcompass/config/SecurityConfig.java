package com.rc.readcompass.config;


import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.security.autoconfigure.web.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  @Bean
  public HttpFirewall allowDoubleSlashFirewall() {
    StrictHttpFirewall firewall = new StrictHttpFirewall();
    firewall.setAllowUrlEncodedDoubleSlash(true);
    return firewall;
  }

  @Bean
  public WebSecurityCustomizer webSecurityCustomizer() {
    return web -> {
      web.httpFirewall(allowDoubleSlashFirewall());
      // ignoring() 완전히 제거 → filterChain의 permitAll()로 이동
    };
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

    http.csrf(csrf -> csrf.disable())
        .formLogin(form -> form.disable())
        .httpBasic(basic -> basic.disable());

    http.authorizeHttpRequests(auth -> auth
        // 정적 리소스 및 SPA 진입점
        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
        .requestMatchers("/assets/**", "/images/**", "/uploads/**", "/*.ico", "/*.png").permitAll()
        // OAuth2 / 로그인 흐름
        .requestMatchers("/oauth2/**", "/login/oauth2/**", "/login").permitAll()
        // 공개 API
        .requestMatchers("/api/public/**").permitAll()
        // 인증 필요 API
        .requestMatchers("/api/my").hasAnyRole("ADMIN", "USER")
        .requestMatchers("/api/admin/**").hasRole("ADMIN")
        .anyRequest().permitAll()
    );

    http.exceptionHandling(ex -> ex
        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)));

    http.logout(logout -> logout
        .logoutUrl("/logout")
        .addLogoutHandler((request, response, authentication) -> {
          Cookie cookie = new Cookie("Authorization", null);
          cookie.setPath("/");
          cookie.setHttpOnly(true);
          cookie.setMaxAge(0);
          response.addCookie(cookie);
        })
        .logoutSuccessHandler((request, response, authentication) ->
            response.setStatus(HttpStatus.OK.value())));

    http.sessionManagement(session -> session
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

    return http.build();
  }
}