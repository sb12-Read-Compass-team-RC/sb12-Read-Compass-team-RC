package com.rc.readcompass.user.Service;

import com.rc.readcompass.exception.ErrorCode;
import com.rc.readcompass.exception.base.CustomException;
import com.rc.readcompass.oauth2.dto.AuthProvider;
import com.rc.readcompass.user.entity.UserRole;
import com.rc.readcompass.user.Mapper.UserMapper;
import com.rc.readcompass.user.Repository.UserRepository;
import com.rc.readcompass.user.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.rc.readcompass.user.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    // POST /api/users - 회원가입
    @Transactional
    public UserResponse register(UserRegisterRequest request) {
        if (userRepository.existsByEmailAndDeletedFalse(request.email())) {
            throw new CustomException(ErrorCode.USER_ALREADY_EXISTS).addDetail("이미 존재하는 이메일입니다.");
        }
        if (userRepository.existsByNicknameAndDeletedFalse(request.nickname())) {
            throw new CustomException(ErrorCode.USER_ALREADY_EXISTS).addDetail("이미 존재하는 닉네임입니다.");
        }

        User user = User.builder()
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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return userMapper.toResponse(user);
    }

    // PATCH /api/users/{userId} - 사용자 정보 수정
    @Transactional
    public UserResponse updateUser(UUID userId, UUID requesterId, UserUpdateRequest request) {
        if (!userId.equals(requesterId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "수정 권한이 없습니다.");
        }

        User user = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        // 닉네임이 실제로 바뀌는 경우에만, 활성 사용자 중 중복이 있는지 검사한다.
        if (!user.getNickname().equals(request.nickname())
                && userRepository.existsByNicknameAndDeletedFalse(request.nickname())) {
            throw new CustomException(ErrorCode.USER_ALREADY_EXISTS).addDetail("이미 존재하는 닉네임입니다.");
        }

        user.updateNickname(request.nickname());
        return userMapper.toResponse(user);
    }

    // DELETE /api/users/{userId} - 논리 삭제
    // DELETE /api/users/{userId} - 논리 삭제
    @Transactional
    public void softDeleteUser(UUID userId, UUID requesterId) {
        if (!userId.equals(requesterId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "탈퇴 권한이 없습니다.");
        }

        User user = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        user.softDelete();
    }

    // DELETE /api/users/{userId}/hard - 물리 삭제
    @Transactional
    public void hardDeleteUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        userRepository.delete(user);
    }
}
