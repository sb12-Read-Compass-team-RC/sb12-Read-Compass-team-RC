package com.rc.readcompass.user.Controller;

import com.rc.readcompass.oauth2.dto.AuthProvider;
import com.rc.readcompass.user.UserRepository;
import com.rc.readcompass.user.UserRole;
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
import com.rc.readcompass.user.User;
import com.rc.readcompass.user.Service.UserService;
import com.rc.readcompass.user.dto.UserResponse;
import com.rc.readcompass.user.dto.UserUpdateRequest;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.web.bind.annotation.DeleteMapping;

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
    private final UserService userService;

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

    //닉네임 수정
    @PatchMapping("/{userId}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable UUID userId,
            @RequestHeader("Deokhugam-Request-User-ID") UUID requesterId,
            @Valid @RequestBody UserUpdateRequest request) {

        UserResponse response = userService.updateUser(userId, requesterId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> softDeleteUser(
            @PathVariable UUID userId,
            @RequestHeader("Deokhugam-Request-User-ID") UUID requesterId) {

        userService.softDeleteUser(userId, requesterId);
        return ResponseEntity.noContent().build();
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
