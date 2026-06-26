package com.rc.readcompass.user;

import com.rc.readcompass.oauth2.dto.AuthProvider;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 아주 간단한 회원가입 컨트롤러.
 * 로그인은 Spring Security 의 LoginFilter(/api/users/login)가 처리하므로 여기엔 없음.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 회원가입
    @PostMapping
    public ResponseEntity<?> signup(@RequestBody SignupRequest request) {

        // 중복 체크
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 존재하는 이메일입니다.");
        }
        if (userRepository.existsByNickname(request.getNickname())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 존재하는 닉네임입니다.");
        }

        // 저장
        User user = User.builder()
            .email(request.getEmail())
            .nickname(request.getNickname())
            .password(passwordEncoder.encode(request.getPassword()))
            .role(UserRole.USER)
            .provider(AuthProvider.LOCAL)
            .build();
        userRepository.save(user);

        // 응답
        SignupResponse response = new SignupResponse(
            user.getId().toString(),
            user.getEmail(),
            user.getNickname()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ===== 요청/응답 DTO (간단하게 내부 클래스로) =====
    @Getter
    @Setter
    public static class SignupRequest {
        private String email;
        private String nickname;
        private String password;
    }

    public record SignupResponse(String id, String email, String nickname) {
    }
}
