package com.rc.readcompass.oauth2.service;

import com.rc.readcompass.oauth2.dto.AuthProvider;
import com.rc.readcompass.oauth2.entity.CustomOAuth2User;
import com.rc.readcompass.oauth2.dto.GoogleResponse;
import com.rc.readcompass.oauth2.dto.NaverResponse;
import com.rc.readcompass.oauth2.dto.OAuth2Response;
import com.rc.readcompass.user.entity.User;
import com.rc.readcompass.user.Repository.UserRepository;
import com.rc.readcompass.user.entity.UserRole;
import java.time.Instant;
import java.util.UUID;
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
        .map(existing -> {
          // 논리 삭제(회원탈퇴)된 사용자는 로그인할 수 없다.
          if (existing.isDeleted()) {
            throw new OAuth2AuthenticationException("탈퇴한 사용자입니다.");
          }
          return updateExistingUser(existing);
        })
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
   * 소셜 로그인 닉네임은 이름 뒤에 랜덤 코드를 붙여 고유하게 만든다.
   * ex) 장현우 → 장현우_a1b2c3
   */
  private String resolveUniqueNickname(String base) {
    // 50자 컬럼 제한 고려: 뒤에 붙일 코드("_" + 6자리 = 7자) 공간 확보
    String trimmed = base.length() > 40 ? base.substring(0, 40) : base;

    for (int attempt = 0; attempt < 10; attempt++) {
      String code = UUID.randomUUID().toString().replace("-", "").substring(0, 6);
      String candidate = trimmed + "_" + code;
      if (!userRepository.existsByNicknameAndDeletedFalse(candidate)) {
        return candidate;
      }
    }
    // 극히 드문 충돌 대비 fallback (더 긴 코드)
    return trimmed + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
  }
}