package io.nicheblog.dreamdiary.feature.jrnl.chapter.service.my;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.jrnl.chapter.model.JrnlChapterDto;
import io.nicheblog.dreamdiary.feature.jrnl.chapter.model.JrnlChapterSearchParam;
import io.nicheblog.dreamdiary.feature.jrnl.chapter.service.JrnlChapterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * MyJrnlChapterService
 * <pre>
 *  로그인 사용자 기준 저널 챕터 서비스.
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class MyJrnlChapterService {

    private final JrnlChapterService jrnlChapterService;

    /**
     * 목록 조회 (dto level)
     *
     * @param searchParam 검색조건을 담고 있는 파라미터 객체
     * @return {@link List} -- 조회된 목록
     */
    public List<JrnlChapterDto> getMyListDto(final JrnlChapterSearchParam searchParam) throws Exception {
        final String userId = AuthUtils.requireLgnUserId();
        return jrnlChapterService.getListDtoByUser(userId, searchParam);
    }

    /**
     * 상세 조회 (dto level) :: 캐시 처리
     *
     * @param key 일련번호
     * @return {@link JrnlChapterDto} -- 조회된 객체
     */
    public JrnlChapterDto getMyDtlDtoWithCache(final Integer key) throws Exception {
        final String userId = AuthUtils.requireLgnUserId();
        return jrnlChapterService.getDtlDtoWithCacheByUser(userId, key);
    }
}
