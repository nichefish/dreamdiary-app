package io.nicheblog.dreamdiary.feature.journal.chapter.service.my;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.journal.chapter.model.JournalChapterDto;
import io.nicheblog.dreamdiary.feature.journal.chapter.model.JournalChapterSearchParam;
import io.nicheblog.dreamdiary.feature.journal.chapter.service.JournalChapterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * MyJournalChapterService
 * <pre>
 *  로그인 사용자 기준 저널 챕터 서비스.
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class MyJournalChapterService {

    private final JournalChapterService journalChapterService;

    /**
     * 목록 조회 (dto level)
     *
     * @param searchParam 검색조건을 담고 있는 파라미터 객체
     * @return {@link List} -- 조회된 목록
     */
    public List<JournalChapterDto> getMyListDto(final JournalChapterSearchParam searchParam) throws Exception {
        final String username = AuthUtils.requireLoginUsername();
        return journalChapterService.getListDtoByUser(username, searchParam);
    }

    /**
     * 상세 조회 (dto level) :: 캐시 처리
     *
     * @param key 일련번호
     * @return {@link JournalChapterDto} -- 조회된 객체
     */
    public JournalChapterDto getMyDtlDtoWithCache(final Integer key) throws Exception {
        final String username = AuthUtils.requireLoginUsername();
        return journalChapterService.getDtlDtoWithCacheByUser(username, key);
    }
}
