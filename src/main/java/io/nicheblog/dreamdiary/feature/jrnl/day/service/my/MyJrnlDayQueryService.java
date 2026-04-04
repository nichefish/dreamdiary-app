package io.nicheblog.dreamdiary.feature.jrnl.day.service.my;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.jrnl.day.model.JrnlDayDto;
import io.nicheblog.dreamdiary.feature.jrnl.day.model.JrnlDaySearchParam;
import io.nicheblog.dreamdiary.feature.jrnl.day.service.JrnlDayQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * MyJrnlDayQueryService
 * <pre>
 *   로그인 사용자 기준 JrnlDay 조회 결과 enrich 서비스
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class MyJrnlDayQueryService {

    private final JrnlDayQueryService jrnlDayQueryService;

    /**
     * 연월기준 목록 조회 + enrich
     *
     * @param searchParam 조회 조건 (연도, 월, 필터 조건 포함)
     * @return {@link List} -- 가공 완료된 일자 DTO 목록
     */
    public List<JrnlDayDto> getMyYyMnthListDtoEnriched(final JrnlDaySearchParam searchParam) throws Exception {
        final String userId = AuthUtils.requireUserId(AuthUtils.getLgnUserId());
        return jrnlDayQueryService.getYyMnthListDtoEnrichedByUser(userId, searchParam);
    }

    /**
     * 기준일(standard day) 목록 조회 + enrich
     *
     * @param searchParam 조회 조건
     * @return {@link List} -- 가공 완료된 DTO 목록
     */
    public List<JrnlDayDto> getMyStdrdDaysDtoEnriched(final JrnlDaySearchParam searchParam) throws Exception {
        final String userId = AuthUtils.requireUserId(AuthUtils.getLgnUserId());
        return jrnlDayQueryService.getStdrdDaysDtoEnrichedByUser(userId, searchParam);
    }

    /**
     * 주간 목록 조회 + enrich
     *
     * @param searchParam 조회 조건
     * @return {@link List} -- 가공 완료된 DTO 목록
     */
    public List<JrnlDayDto> getMyWeeklyListDtoEnriched(final JrnlDaySearchParam searchParam) throws Exception {
        final String userId = AuthUtils.requireUserId(AuthUtils.getLgnUserId());
        return jrnlDayQueryService.getWeeklyListDtoEnrichedByUser(userId, searchParam);
    }

    /**
     * 메타 기준 조회 + enrich
     *
     * @param searchParam 조회 조건 (metaNo 포함)
     * @return {@link List} -- 가공 완료된 DTO 목록
     */
    public List<JrnlDayDto> getMyListDtoByMetaNoEnriched(final JrnlDaySearchParam searchParam) throws Exception {
        final String userId = AuthUtils.requireUserId(AuthUtils.getLgnUserId());
        return jrnlDayQueryService.getListDtoByMetaNoEnrichedByUser(userId, searchParam);
    }

    /**
     * 태그 기준 조회 + enrich
     *
     * @param searchParam 조회 조건 (tagNo 포함)
     * @return {@link List} -- 가공 완료된 DTO 목록
     */
    public List<JrnlDayDto> getMyListDtoByTagNoEnriched(final JrnlDaySearchParam searchParam) throws Exception {
        final String userId = AuthUtils.requireUserId(AuthUtils.getLgnUserId());
        return jrnlDayQueryService.getListDtoByTagNoEnrichedByUser(userId, searchParam);
    }

    /**
     * 상세 조회 + enrich
     *
     * @param key PK
     * @return {@link JrnlDayDto} -- 가공 완료된 DTO
     */
    public JrnlDayDto getMyDtlDtoEnriched(final Integer key) throws Exception {
        final String userId = AuthUtils.requireUserId(AuthUtils.getLgnUserId());
        return jrnlDayQueryService.getDtlDtoEnrichedByUser(userId, key);
    }
}
