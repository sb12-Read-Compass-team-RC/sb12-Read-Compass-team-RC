package com.rc.readcompass.user.Service;

import com.querydsl.core.types.Order;
import com.rc.readcompass.common.PeriodType;
import com.rc.readcompass.common.slice.SliceCursorPageResponse;
import com.rc.readcompass.user.Repository.UserRankingRepository;
import com.rc.readcompass.user.dto.PowerUserDto;
import com.rc.readcompass.user.dto.PowerUserSearchRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * 파워 유저 랭킹 조회 서비스.
 * 랭킹 데이터 산출(배치)은 별도 배치 프로젝트에서 tb_user_rankings 에 스냅샷을 적재하며,
 * 여기서는 최신 스냅샷을 읽어 목록으로 내려주기만 한다.
 * (인기 도서 PopularBookService / 인기 리뷰 ReviewRankingService 와 동일한 역할)
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PowerUserService {

    private final UserRankingRepository userRankingRepository;

    public SliceCursorPageResponse<PowerUserDto> getPowerUsers(
            PeriodType period,
            Order direction,
            String cursor,
            Instant after,
            int limit
    ) {
        PowerUserSearchRequest request = new PowerUserSearchRequest(
                period == null ? PeriodType.DAILY : period,
                direction == null ? Order.ASC : direction,
                cursor,
                after,
                limit <= 0 ? 50 : limit
        );
        return userRankingRepository.searchCursor(request);
    }
}
