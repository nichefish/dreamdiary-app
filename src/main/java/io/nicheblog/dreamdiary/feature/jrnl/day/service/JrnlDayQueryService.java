package io.nicheblog.dreamdiary.feature.jrnl.day.service;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.jrnl.day.model.JrnlDayDto;
import io.nicheblog.dreamdiary.feature.jrnl.day.model.JrnlDaySearchParam;
import io.nicheblog.dreamdiary.feature.jrnl.day.service.helper.JrnlDayFilterHelper;
import io.nicheblog.dreamdiary.feature.jrnl.day.service.helper.JrnlDayHldyHelper;
import io.nicheblog.dreamdiary.feature.jrnl.day.service.helper.JrnlDayViewHelper;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import io.nicheblog.dreamdiary.infrastructure.cache.util.EhCacheUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * JrnlDayQueryService
 * <pre>
 *   JrnlDay 조회 결과를 화면에 필요한 상태/메타 정보로 조립(enrich)하는 Query 전용 서비스
 * </pre>
 *
 * @author nichefish
 */
@Service("jrnlDayQueryService")
@RequiredArgsConstructor
@Log4j2
public class JrnlDayQueryService {

    private final JrnlDayService jrnlDayService;

    /**
     * 연월기준 목록 조회 + enrich
     *
     * @param userId 조회 사용자 ID
     * @param searchParam 조회 조건 (연도, 월, 필터 조건 포함)
     * @return {@link List} -- 가공 완료된 일자 DTO 목록
     */
    public List<JrnlDayDto> getYyMnthListDtoEnrichedByUser(final String userId, final JrnlDaySearchParam searchParam) throws Exception {
        if (searchParam == null) return List.of();

        final List<JrnlDayDto> listDto = jrnlDayService.getCachedYyMnthListDtoByUser(
                AuthUtils.requireUserId(userId),
                searchParam.getYy(),
                searchParam.getMnth()
        );
        final List<JrnlDayDto> filteredList = JrnlDayFilterHelper.filterInMemory(listDto, searchParam);

        return this.enrichList(filteredList, searchParam);
    }

    /**
     * 기준일(standard day) 목록 조회 + enrich
     *
     * @param userId 조회 사용자 ID
     * @param searchParam 조회 조건
     * @return {@link List} -- 가공 완료된 DTO 목록
     */
    public List<JrnlDayDto> getStdrdDaysDtoEnrichedByUser(final String userId, final JrnlDaySearchParam searchParam) throws Exception {
        final List<JrnlDayDto> listDto = jrnlDayService.getJrnlStdrdDaysByUser(AuthUtils.requireUserId(userId), searchParam);
        return this.enrichList(listDto, searchParam);
    }

    /**
     * 주간 목록 조회 + enrich
     *
     * @param userId 조회 사용자 ID
     * @param searchParam 조회 조건
     * @return {@link List} -- 가공 완료된 DTO 목록
     */
    public List<JrnlDayDto> getWeeklyListDtoEnrichedByUser(final String userId, final JrnlDaySearchParam searchParam) throws Exception {
        if (searchParam == null) return List.of();

        final String weekStartDt = StringUtils.isNotBlank(searchParam.getWeekStartDt())
                ? searchParam.getWeekStartDt()
                : DateUtils.getWeekStartDateStr(searchParam.getStdrdDt());
        if (StringUtils.isBlank(weekStartDt)) return List.of();
        searchParam.setWeekStartDt(weekStartDt);

        final List<JrnlDayDto> listDto = jrnlDayService.getCachedWeeklyListDtoByUser(AuthUtils.requireUserId(userId), weekStartDt);
        final List<JrnlDayDto> filteredList = JrnlDayFilterHelper.filterInMemory(listDto, searchParam);
        return this.enrichWeeklyList(filteredList, searchParam);
    }

    /**
     * 메타 기준 조회 + enrich
     *
     * @param userId 조회 사용자 ID
     * @param searchParam 조회 조건 (metaNo 포함)
     * @return {@link List} -- 가공 완료된 DTO 목록
     */
    public List<JrnlDayDto> getListDtoByMetaNoEnrichedByUser(final String userId, final JrnlDaySearchParam searchParam) throws Exception {
        final List<JrnlDayDto> listDto = jrnlDayService.getListDtoByMetaNoAndUser(AuthUtils.requireUserId(userId), searchParam);
        return this.enrichList(listDto, searchParam);
    }

    /**
     * 태그 기준 조회 + enrich
     *
     * @param userId 조회 사용자 ID
     * @param searchParam 조회 조건 (tagNo 포함)
     * @return {@link List} -- 가공 완료된 DTO 목록
     */
    public List<JrnlDayDto> getListDtoByTagNoEnrichedByUser(final String userId, final JrnlDaySearchParam searchParam) throws Exception {
        final List<JrnlDayDto> listDto = jrnlDayService.getListDtoByTagNoAndUser(AuthUtils.requireUserId(userId), searchParam);
        return this.enrichList(listDto, searchParam);
    }

    /**
     * 상세 조회 + enrich
     *
     * @param userId 조회 사용자 ID
     * @param key PK
     * @return {@link JrnlDayDto} -- 가공 완료된 DTO
     */
    public JrnlDayDto getDtlDtoEnrichedByUser(final String userId, final Integer key) throws Exception {
        final JrnlDayDto retrieved = jrnlDayService.getCachedDtlDtoByUser(AuthUtils.requireUserId(userId), key);
        return this.enrichDetail(retrieved);
    }

    /**
     * 목록 공통 enrich 처리
     * 1) 휴일 정보 매핑 2) 상태 병합 (조회 조건 기반) 3) 태그 요약 적용
     *
     * @param listDto 조회 결과 리스트
     * @param searchParam 조회 조건
     * @return enrich 완료 리스트
     */
    private List<JrnlDayDto> enrichList(final List<JrnlDayDto> listDto, final JrnlDaySearchParam searchParam) throws Exception {
        if (listDto == null) return null;

        JrnlDayHldyHelper.setHldyInfo(listDto, getHldyMap());
        if (searchParam != null) {
            JrnlDayViewHelper.mergeStates(listDto, searchParam);
            JrnlDayViewHelper.applyEntryTagSummary(listDto, searchParam);
        }

        return listDto;
    }

    /**
     * 주간 목록 전용 enrich 처리
     *
     * @param listDto 조회 결과 리스트
     * @param searchParam 조회 조건
     * @return enrich 완료 리스트
     */
    private List<JrnlDayDto> enrichWeeklyList(final List<JrnlDayDto> listDto, final JrnlDaySearchParam searchParam) throws Exception {
        if (listDto == null) return null;

        JrnlDayHldyHelper.setHldyInfo(listDto, getHldyMap());
        if (searchParam != null) {
            JrnlDayViewHelper.mergeWeeklyStates(listDto, searchParam);
            JrnlDayViewHelper.applyEntryTagSummary(listDto, searchParam);
        }

        return listDto;
    }

    /**
     * 단건 공통 enrich 처리
     *
     * @param retrieved 조회 결과
     * @return enrich 완료 DTO
     */
    private JrnlDayDto enrichDetail(final JrnlDayDto retrieved) throws Exception {
        if (retrieved == null) return null;

        JrnlDayHldyHelper.setHldyInfo(retrieved, getHldyMap());
        JrnlDayViewHelper.mergeStates(retrieved);

        return retrieved;
    }

    /**
     * 휴일 정보 캐시 조회
     *
     * @return 휴일 맵
     */
    @SuppressWarnings("unchecked")
    private Map<String, List<String>> getHldyMap() {
        return (Map<String, List<String>>) EhCacheUtils.getObjectFromCache("hldyMap");
    }
}
