package io.nicheblog.dreamdiary.feature.jrnl.day.service.my;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.jrnl.day.service.JrnlDayMetaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * MyJrnlDayMetaService
 * <pre>
 *  로그인 사용자 기준 저널 일자 메타 서비스 모듈 (facade)
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class MyJrnlDayMetaService {
    
    private final JrnlDayMetaService jrnlDayMetaService;

    /**
     * 내 태그 카테고리 맵을 반환합니다.
     *
     * @return {@link Map} -- 태그 이름을 키로 하고, 카테고리 목록을 값으로 가지는 맵
     */
    public Map<String, List<String>> getMyMetaCtgrMap() throws Exception {
        final String userId = AuthUtils.requireLgnUserId();
        return jrnlDayMetaService.getMetaCtgrMapByUser(userId);
    }

    /**
     * 특정 메타가 존재하는 연도 목록을 반환합니다.
     *
     * @param metaNo 메타 번호
     * @return 연도 목록
     */
    public List<Integer> getMyYyListByMetaNo(final Integer metaNo) {
        final String userId = AuthUtils.requireLgnUserId();
        return jrnlDayMetaService.getYyListByMetaNoAndUser(metaNo, userId);
    }
}
