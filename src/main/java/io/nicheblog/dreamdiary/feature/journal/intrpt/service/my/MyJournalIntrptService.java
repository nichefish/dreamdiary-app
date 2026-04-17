package io.nicheblog.dreamdiary.feature.journal.intrpt.service.my;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.journal.intrpt.model.JournalIntrptDto;
import io.nicheblog.dreamdiary.feature.journal.intrpt.model.JournalIntrptSearchParam;
import io.nicheblog.dreamdiary.feature.journal.intrpt.service.JournalIntrptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * MyJournalIntrptService
 * <pre>
 *  로그인 사용자 기준 저널 해석 서비스
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class MyJournalIntrptService {

    private final JournalIntrptService journalIntrptService;

    /**
     * 목록 조회 (dto level)
     *
     * @param searchParam 검색조건을 담고 있는 파라미터 객체
     * @return {@link List} -- 조회된 목록
     */
    public List<JournalIntrptDto> getMyListDto(final JournalIntrptSearchParam searchParam) throws Exception {
        final String username = AuthUtils.requireLoginUsername();
        return journalIntrptService.getListDtoByUser(username, searchParam);
    }

    /**
     * 상세 조회 (dto level) :: 캐시 처리
     *
     * @param key 일련번호
     * @return {@link JournalIntrptDto} -- 조회된 객체
     */
    public JournalIntrptDto getMyDtlDtoWithCache(final Integer key) throws Exception {
        final String username = AuthUtils.requireLoginUsername();
        return journalIntrptService.getDtlDtoWithCacheByUser(username, key);
    }
}
