package com.rc.readcompass.config;

import com.rc.readcompass.jwt.filter.CustomLogoutFilter;
import com.rc.readcompass.jwt.filter.JWTFilter;
import com.rc.readcompass.jwt.filter.LoginFilter;
import com.rc.readcompass.jwt.repository.RefreshRepository;
import com.rc.readcompass.jwt.service.RefreshTokenService;
import com.rc.readcompass.jwt.util.CookieUtil;
import com.rc.readcompass.jwt.util.JWTUtil;
import com.rc.readcompass.oauth2.handler.OAuth2LoginFailureHandler;
import com.rc.readcompass.oauth2.handler.OAuth2LoginSuccessHandler;
import com.rc.readcompass.oauth2.service.CustomOAuth2UserService;
import jakarta.servlet.DispatcherType;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.security.autoconfigure.web.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  @Value("${app.jwt.access-expire-ms}")
  private long accessExpireMs;

  @Value("${app.jwt.refresh-expire-ms}")
  private long refreshExpireMs;

  private final AuthenticationConfiguration authenticationConfiguration;
  private final JWTUtil jwtUtil;
  private final RefreshRepository refreshRepository;
  private final RefreshTokenService refreshTokenService;
  private final CookieUtil cookieUtil;

  private final CustomOAuth2UserService customOAuth2UserService;
  private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
  private final OAuth2LoginFailureHandler oAuth2LoginFailureHandler;

  @Bean
  public HttpFirewall allowDoubleSlashFirewall() {
    StrictHttpFirewall firewall = new StrictHttpFirewall();
    firewall.setAllowUrlEncodedDoubleSlash(true);
    return firewall;
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
    return configuration.getAuthenticationManager();
  }

  @Bean
  public BCryptPasswordEncoder bCryptPasswordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public WebSecurityCustomizer webSecurityCustomizer() {
    return web -> {web.httpFirewall(allowDoubleSlashFirewall());};
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

    http.csrf(csrf -> csrf.disable())
        .formLogin(form -> form.disable())
        .httpBasic(basic -> basic.disable());

    http.authorizeHttpRequests(auth -> auth
        .anyRequest().permitAll());

//        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
//        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
//        .requestMatchers("/", "/index.html", "/assets/**", "/images/**", "/uploads/**", "/files/**",
//            "/*.ico", "/*.png").permitAll()
//        .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
//        // 인증 없이 열어야 하는 API: 회원가입, 로그인, 재발급, 로그아웃
//        .requestMatchers(HttpMethod.POST, "/api/users").permitAll()
//        .requestMatchers("/api/users/login", "/api/users/reissue",
//            "/api/users/logout").permitAll()
//        .requestMatchers("/api/admin/**").hasRole("ADMIN")
//        .anyRequest().authenticated()
//    );

    http.oauth2Login(oauth2 -> oauth2
        .userInfoEndpoint(userInfo -> userInfo
            .userService(customOAuth2UserService))  // 유저 정보 처리
        .successHandler(oAuth2LoginSuccessHandler)  // JWT 발급
        .failureHandler(oAuth2LoginFailureHandler)
    );

    http.addFilterBefore(new JWTFilter(jwtUtil), LoginFilter.class);

    http.addFilterAt(new LoginFilter(authenticationManager(authenticationConfiguration), jwtUtil, refreshTokenService, cookieUtil, accessExpireMs, refreshExpireMs), UsernamePasswordAuthenticationFilter.class);

    http.addFilterBefore(new CustomLogoutFilter(jwtUtil, refreshRepository, cookieUtil), LogoutFilter.class);

    http.exceptionHandling(ex -> ex
        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)));

    // 로그아웃은 CustomLogoutFilter(/api/users/logout)가 처리한다.

    // 세션 비활성화
    http.sessionManagement(session -> session
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

    return http.build();
  }
}