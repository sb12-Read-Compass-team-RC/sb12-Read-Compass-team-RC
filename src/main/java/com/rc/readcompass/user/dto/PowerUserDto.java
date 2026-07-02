package com.rc.readcompass.user.dto;

import com.rc.readcompass.common.PeriodType;

import java.time.Instant;
import java.util.UUID;

/**
 * 파워 유저 랭킹 응답 DTO.
 * 랭킹 스냅샷(tb_user_rankings)에 저장되는 값만 노출한다.
 * 세부 집계값(리뷰 점수 합 / 좋아요 수 / 댓글 수)은 배치가 최종 score 계산에만 쓰고
 * 저장하지 않으므로, 응답에서도 제공하지 않는다.
 *
 * (QueryDSL Projections/생성자 매핑이 이 필드 순서에 의존하므로 변경 시 주의)
 */
public record PowerUserDto(
        UUID userId,
        String nickname,
        PeriodType period,
        Instant createdAt,
        Long rank,
        Double score
) {}
