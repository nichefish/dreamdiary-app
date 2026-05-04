package io.nicheblog.dreamdiary.feature.journal.interpretation.service.my;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.journal.interpretation.model.JournalInterpretationDto;
import io.nicheblog.dreamdiary.feature.journal.interpretation.model.JournalInterpretationSearchParam;
import io.nicheblog.dreamdiary.feature.journal.interpretation.service.JournalInterpretationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * MyJournalInterpretationService
 * <pre>
 *  로그인 사용자 기준 저널 해석 서비스
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class MyJournalInterpretationService {

    private final JournalInterpretationService journalInterpretationService;

    /**
     * 목록 조회 (dto level)
     *
     * @param searchParam 검색조건을 담고 있는 파라미터 객체
     * @return {@link List} -- 조회된 목록
     * @throws Exception 조회 중 예외
     */
    public List<JournalInterpretationDto> getMyListDto(final JournalInterpretationSearchParam searchParam) throws Exception {
        final String username = AuthUtils.requireLoginUsername();
        return journalInterpretationService.getListDtoByUser(username, searchParam);
    }

    /**
     * 상세 조회 (dto level) :: 캐시 처리
     *
     * @param key 일련번호
     * @return {@link JournalInterpretationDto} -- 조회된 객체
     * @throws Exception 조회 중 예외
     */
    public JournalInterpretationDto getMyDtlDtoWithCache(final Integer key) throws Exception {
        final String username = AuthUtils.requireLoginUsername();
        return journalInterpretationService.getDtlDtoWithCacheByUser(username, key);
    }
}
