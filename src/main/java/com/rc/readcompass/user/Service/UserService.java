package com.rc.readcompass.user.Service;

import com.rc.readcompass.common.PeriodType;
import com.rc.readcompass.oauth2.dto.AuthProvider;
import com.rc.readcompass.user.Entity.UserRole;
import com.rc.readcompass.user.Mapper.UserMapper;
import com.rc.readcompass.user.Repository.UserRankingRepository;
import com.rc.readcompass.user.Repository.UserRepository;
import com.rc.readcompass.user.Entity.UserRanking;
import com.rc.readcompass.user.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserRankingRepository userRankingRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    // POST /api/users - 회원가입
    @Transactional
    public UserResponse register(UserRegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }
        if (userRepository.existsByNickname(request.nickname())) {
            throw new IllegalArgumentException("이미 존재하는 닉네임입니다.");
        }

        UserRole.User user = UserRole.User.builder()
                .email(request.email())
                .nickname(request.nickname())
                .password(passwordEncoder.encode(request.password()))
                .role(UserRole.USER)
                .provider(AuthProvider.LOCAL)
                .build();
        userRepository.save(user);

        return userMapper.toResponse(user);
    }

    // GET /api/users/{userId} - 사용자 조회
    @Transactional(readOnly = true)
    public UserResponse getUser(UUID userId) {
        UserRole.User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return userMapper.toResponse(user);
    }

    // PATCH /api/users/{userId} - 사용자 정보 수정
    @Transactional
    public UserResponse updateUser(UUID userId, UserUpdateRequest request) {
        UserRole.User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        user.updateNickname(request.nickname());
        return userMapper.toResponse(user);
    }

    // DELETE /api/users/{userId} - 논리 삭제
    @Transactional
    public void softDeleteUser(UUID userId) {
        UserRole.User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        user.softDelete();
    }

    // DELETE /api/users/{userId}/hard - 물리 삭제
    @Transactional
    public void hardDeleteUser(UUID userId) {
        UserRole.User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        userRepository.delete(user);
    }

    // GET /api/users/power - 파워유저 목록 조회
    @Transactional(readOnly = true)
    public CursorPageResponsePowerUserDto getPowerUsers(
            PeriodType period,
            String cursor,
            Instant after,
            int limit
    ) {
        List<UserRanking> rankings = userRankingRepository.findAll();
        List<PowerUserDto> content = rankings.stream()
                .map(userMapper::toPowerUserDto)
                .toList();

        return userMapper.toCursorPageResponse(
                content,
                null,
                null,
                limit,
                (long) content.size(),
                false
        );
    }
}
