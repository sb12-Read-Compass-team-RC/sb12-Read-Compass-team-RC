package com.rc.readcompass.user.Repository;

import com.rc.readcompass.common.slice.SliceCursorPageResponse;
import com.rc.readcompass.user.dto.PowerUserDto;
import com.rc.readcompass.user.dto.PowerUserSearchRequest;

public interface UserRankingQueryRepository {

    /**
     * 해당 기간의 최신 스냅샷(calculated_at 최대)을 기준으로
     * 파워 유저 목록을 커서 페이지네이션 조회한다.
     */
    SliceCursorPageResponse<PowerUserDto> searchCursor(PowerUserSearchRequest request);
}
