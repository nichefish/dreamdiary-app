package io.nicheblog.dreamdiary.feature.journal.day.service.my;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.journal.day.model.JournalDayDto;
import io.nicheblog.dreamdiary.feature.journal.day.model.JournalDaySearchParam;
import io.nicheblog.dreamdiary.feature.journal.day.service.JournalDayQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * MyJournalDayQueryService
 * <pre>
 *   로그인 사용자 기준 JournalDay 조회 결과 enrich 서비스
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class MyJournalDayQueryService {

    private final JournalDayQueryService journalDayQueryService;

    /**
     * 연월기준 목록 조회 + enrich
     *
     * @param searchParam 조회 조건 (연도, 월, 필터 조건 포함)
     * @return {@link List} -- 가공 완료된 일자 DTO 목록
     */
    public List<JournalDayDto> getMyYyMnthListDtoEnriched(final JournalDaySearchParam searchParam) throws Exception {
        final String username = AuthUtils.requireLoginUsername();
        return journalDayQueryService.getYyMnthListDtoEnrichedByUser(username, searchParam);
    }

    /**
     * 기준일(standard day) 목록 조회 + enrich
     *
     * @param searchParam 조회 조건
     * @return {@link List} -- 가공 완료된 DTO 목록
     */
    public List<JournalDayDto> getMyStdrdDaysDtoEnriched(final JournalDaySearchParam searchParam) throws Exception {
        final String username = AuthUtils.requireLoginUsername();
        return journalDayQueryService.getStdrdDaysDtoEnrichedByUser(username, searchParam);
    }

    /**
     * 주간 목록 조회 + enrich
     *
     * @param searchParam 조회 조건
     * @return {@link List} -- 가공 완료된 DTO 목록
     */
    public List<JournalDayDto> getMyWeeklyListDtoEnriched(final JournalDaySearchParam searchParam) throws Exception {
        final String username = AuthUtils.requireLoginUsername();
        return journalDayQueryService.getWeeklyListDtoEnrichedByUser(username, searchParam);
    }

    /**
     * 메타 기준 조회 + enrich
     *
     * @param searchParam 조회 조건 (metaId 포함)
     * @return {@link List} -- 가공 완료된 DTO 목록
     */
    public List<JournalDayDto> getMyListDtoByMetaIdEnriched(final JournalDaySearchParam searchParam) throws Exception {
        final String username = AuthUtils.requireLoginUsername();
        return journalDayQueryService.getListDtoByMetaIdEnrichedByUser(username, searchParam);
    }

    /**
     * 태그 기준 조회 + enrich
     *
     * @param searchParam 조회 조건 (tagId 포함)
     * @return {@link List} -- 가공 완료된 DTO 목록
     */
    public List<JournalDayDto> getMyListDtoByTagIdEnriched(final JournalDaySearchParam searchParam) throws Exception {
        final String username = AuthUtils.requireLoginUsername();
        return journalDayQueryService.getListDtoByTagIdEnrichedByUser(username, searchParam);
    }

    /**
     * 상세 조회 + enrich
     *
     * @param key PK
     * @return {@link JournalDayDto} -- 가공 완료된 DTO
     */
    public JournalDayDto getMyDtlDtoEnriched(final Integer key) throws Exception {
        final String username = AuthUtils.requireLoginUsername();
        return journalDayQueryService.getDtlDtoEnrichedByUser(username, key);
    }
}

