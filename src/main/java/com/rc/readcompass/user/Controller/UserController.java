package com.rc.readcompass.user.Controller;

import com.querydsl.core.types.Order;
import com.rc.readcompass.common.PeriodType;
import com.rc.readcompass.common.slice.SliceCursorPageResponse;
import com.rc.readcompass.user.Service.PowerUserService;
import com.rc.readcompass.user.Service.UserService;
import com.rc.readcompass.user.dto.PowerUserDto;
import com.rc.readcompass.user.dto.UserRegisterRequest;
import com.rc.readcompass.user.dto.UserResponse;
import com.rc.readcompass.user.dto.UserUpdateRequest;
import com.rc.readcompass.jwt.entity.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

/**
 * 사용자 관리 컨트롤러.
 * 로그인은 Spring Security 의 LoginFilter(/api/users/login)가 처리하므로 여기엔 없음.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final PowerUserService powerUserService;

    // 회원가입
    @PostMapping
    public ResponseEntity<UserResponse> register(
            @Valid @RequestBody UserRegisterRequest request) {

        UserResponse response = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 파워 유저 목록 조회 (기간별 랭킹)
    // 랭킹 데이터는 별도 배치 프로젝트가 tb_user_rankings 에 적재하며, 여기서는 최신 스냅샷을 조회만 한다.
    @GetMapping("/power")
    public SliceCursorPageResponse<PowerUserDto> getPowerUsers(
            @RequestParam(defaultValue = "DAILY") PeriodType period,
            @RequestParam(defaultValue = "ASC") Order direction,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant after,
            @RequestParam(defaultValue = "50") int limit
    ) {
        return powerUserService.getPowerUsers(period, direction, cursor, after, limit);
    }

    // 사용자 정보 조회
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(userService.getUser(userId));
    }

    // 닉네임 수정
    @PatchMapping("/{userId}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable UUID userId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UserUpdateRequest request) {

        UserResponse response = userService.updateUser(userId, userDetails.getUserId(), request);
        return ResponseEntity.ok(response);
    }

    // 논리 삭제
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> softDeleteUser(
            @PathVariable UUID userId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        userService.softDeleteUser(userId, userDetails.getUserId());
        return ResponseEntity.noContent().build();
    }
}
