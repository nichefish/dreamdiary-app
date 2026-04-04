package io.nicheblog.dreamdiary.feature.jrnl.diary.service.my;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.jrnl.diary.model.JrnlDiaryDto;
import io.nicheblog.dreamdiary.feature.jrnl.diary.model.JrnlDiarySearchParam;
import io.nicheblog.dreamdiary.feature.jrnl.diary.service.JrnlDiaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * MyJrnlDiaryService
 * <pre>
 *  로그인 사용자 기준 저널 일기 서비스
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class MyJrnlDiaryService {

    private final JrnlDiaryService jrnlDiaryService;

    /**
     * 목록 조회 (dto level) :: 캐시 처리
     *
     * @param searchParam 검색조건을 담고 있는 파라미터 객체
     * @return {@link List} -- 조회된 목록
     */
    public List<JrnlDiaryDto> getMyListDto(final JrnlDiarySearchParam searchParam) throws Exception {
        final String userId = AuthUtils.requireLgnUserId();
        return jrnlDiaryService.getListDtoByUser(userId, searchParam);
    }

    /**
     * 특정 연도의중요 일기 목록 조회 :: 캐시 처리
     *
     * @param searchParam JrnlDiarySearchParam
     * @return {@link List} -- 해당 연도의중요 목록
     */
    public List<JrnlDiaryDto> getMySumryDiaryList(final JrnlDiarySearchParam searchParam) throws Exception {
        final String userId = AuthUtils.requireLgnUserId();
        return jrnlDiaryService.getSumryDiaryListByUser(userId, searchParam);
    }

    /**
     * 상세 조회 (dto level) :: 캐시 처리
     *
     * @param key 일련번호
     * @return {@link JrnlDiaryDto} -- 조회된 객체
     */
    public JrnlDiaryDto getMyDtlDtoWithCache(final Integer key) throws Exception {
        final String userId = AuthUtils.requireLgnUserId();
        return jrnlDiaryService.getDtlDtoWithCacheByUser(userId, key);
    }
}
