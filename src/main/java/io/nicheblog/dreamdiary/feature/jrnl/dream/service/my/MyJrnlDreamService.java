package io.nicheblog.dreamdiary.feature.jrnl.dream.service.my;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.jrnl.dream.model.JrnlDreamDto;
import io.nicheblog.dreamdiary.feature.jrnl.dream.model.JrnlDreamSearchParam;
import io.nicheblog.dreamdiary.feature.jrnl.dream.service.JrnlDreamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * MyJrnlDreamService
 * <pre>
 *  로그인 사용자 기준 저널 꿈 서비스
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class MyJrnlDreamService {

    private final JrnlDreamService jrnlDreamService;

    /**
     * 목록 조회 (dto level) :: 캐시 처리
     *
     * @param searchParam 검색조건을 담고 있는 파라미터 객체
     * @return {@link List} -- 조회된 목록
     */
    public List<JrnlDreamDto> getMyListDto(final JrnlDreamSearchParam searchParam) throws Exception {
        final String userId = AuthUtils.requireLgnUserId();
        return jrnlDreamService.getListDtoByUser(userId, searchParam);
    }

    /**
     * 특정 연도의중요 꿈 목록 조회 :: 캐시 처리
     *
     * @param searchParam JrnlDreamSearchParam
     * @return {@link List} -- 해당 연도의중요 목록
     */
    public List<JrnlDreamDto> getMySumryDreamList(final JrnlDreamSearchParam searchParam) throws Exception {
        final String userId = AuthUtils.requireLgnUserId();
        return jrnlDreamService.getSumryDreamListByUser(userId, searchParam);
    }

    /**
     * 상세 조회 (dto level) :: 캐시 처리
     *
     * @param key 일련번호
     * @return {@link JrnlDreamDto} -- 조회된 객체
     */
    public JrnlDreamDto getMyDtlDtoWithCache(final Integer key) throws Exception {
        final String userId = AuthUtils.requireLgnUserId();
        return jrnlDreamService.getDtlDtoWithCacheByUser(userId, key);
    }
}
