package com.rc.readcompass.user;

import com.rc.readcompass.common.domain.BaseUpdatableEntity;
import com.rc.readcompass.oauth2.dto.AuthProvider;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.type.PostgreSQLEnumJdbcType;

@Entity
@Table(name = "tb_users")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseUpdatableEntity {

    // schema 컬럼명이 'username'이지만 도메인/API 상 닉네임으로 사용
    @Column(name = "username", nullable = false, length = 50, unique = true)
    private String nickname;

    @Column(nullable = false, length = 100, unique = true)
    private String email;

    @Column(length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "user_role", nullable = false)
    @Builder.Default
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private UserRole role = UserRole.USER;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "auth_provider")
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private AuthProvider provider;

    @Column(length = 255)
    private String providerId;

    @Column
    private Instant lastLoginAt;

    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;

    @Column
    private Instant deletedAt;

    // =====================================================
    // 도메인 메서드
    // =====================================================

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateLastLoginAt(Instant loginAt) {
        this.lastLoginAt = loginAt;
    }

    /**
     * 논리 삭제 처리.
     * 삭제 후 1일 경과 시 물리 삭제 배치가 수행됨.
     */
    public void softDelete() {
        this.deleted = true;
        this.deletedAt = Instant.now();
    }
}
