package com.rc.readcompass.oauth2.service;

import com.rc.readcompass.oauth2.dto.AuthProvider;
import com.rc.readcompass.oauth2.entity.CustomOAuth2User;
import com.rc.readcompass.oauth2.dto.GoogleResponse;
import com.rc.readcompass.oauth2.dto.NaverResponse;
import com.rc.readcompass.oauth2.dto.OAuth2Response;
import com.rc.readcompass.user.User;
import com.rc.readcompass.user.UserRepository;
import com.rc.readcompass.user.UserRole;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * OAuth2 로그인 흐름:
 * 1. provider 에서 토큰 교환 완료
 * 2. Spring Security 가 이 서비스를 호출하여 유저 정보 로드
 * 3. DB에서 email 조회 → 없으면 신규 가입, 있으면 마지막 로그인 갱신
 * 4. CustomOAuth2User 반환 → SuccessHandler 에서 JWT 발급
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

  private final UserRepository userRepository;

  @Override
  @Transactional
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

    // 1. 기본 서비스로 provider API 호출하여 attributes 획득
    OAuth2User oAuth2User = super.loadUser(userRequest);

    // 2. registrationId = "google" | "naver"
    String registrationId = userRequest.getClientRegistration().getRegistrationId();

    // 3. provider별 파싱
    OAuth2Response oAuth2Response;
    if (registrationId.equals("naver")) {
      oAuth2Response = new NaverResponse(oAuth2User.getAttributes());

    } else if (registrationId.equals("google")) {
      oAuth2Response = new GoogleResponse(oAuth2User.getAttributes());

    } else {
      oAuth2Response = null;
      return null;
    }

    String email = oAuth2Response.getEmail();
    if (email == null || email.isBlank()) {
      throw new OAuth2AuthenticationException("OAuth2 provider에서 이메일을 받지 못했습니다.");
    }

    // 4. DB 조회 → 신규 가입 or 업데이트
    User user = userRepository.findByEmail(email)
        .map(existing -> updateExistingUser(existing))
        .orElseGet(() -> registerNewUser(oAuth2Response));

    return new CustomOAuth2User(user, oAuth2Response);
  }

  // =====================================================
  // Private helpers
  // =====================================================

  private User registerNewUser(OAuth2Response userInfo) {
    String baseNickname = userInfo.getName() != null ? userInfo.getName() : "user";
    String uniqueNickname = resolveUniqueNickname(baseNickname);

    AuthProvider provider = AuthProvider.valueOf(userInfo.getProvider().toUpperCase());

    User newUser = User.builder()
        .email(userInfo.getEmail())
        .nickname(uniqueNickname)
        .password(null)          // 소셜 로그인은 패스워드 없음
        .role(UserRole.USER)
        .provider(provider)
        .providerId(userInfo.getProviderId())
        .lastLoginAt(Instant.now())
        .build();

    log.info("신규 OAuth2 유저 가입: email={}, provider={}", userInfo.getEmail(), provider);
    return userRepository.save(newUser);
  }

  private User updateExistingUser(User user) {
    user.updateLastLoginAt(Instant.now());
    return user;  // @Transactional 이므로 dirty checking으로 반영됨
  }

  /**
   * 닉네임 중복 시 숫자 suffix 를 붙여 고유하게 만든다.
   * ex) 홍길동 → 홍길동2 → 홍길동3 ...
   */
  private String resolveUniqueNickname(String base) {
    // 50자 제한(컬럼) 고려하여 기본 닉네임 자름
    String trimmed = base.length() > 40 ? base.substring(0, 40) : base;

    if (!userRepository.existsByNickname(trimmed)) {
      return trimmed;
    }
    int suffix = 2;
    while (true) {
      String candidate = trimmed + suffix;
      if (!userRepository.existsByNickname(candidate)) {
        return candidate;
      }
      suffix++;
    }
  }
}