package io.nicheblog.dreamdiary.feature.jrnl.day.service;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.clsf._shared.entity.BaseClsfKey;
import io.nicheblog.dreamdiary.feature.clsf._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.clsf.related.model.RelatedContentDto;
import io.nicheblog.dreamdiary.feature.clsf.related.service.RelatedContentQueryService;
import io.nicheblog.dreamdiary.feature.jrnl.chapter.model.JrnlChapterDto;
import io.nicheblog.dreamdiary.feature.jrnl.day.model.JrnlDayDto;
import io.nicheblog.dreamdiary.feature.jrnl.day.model.JrnlDaySearchParam;
import io.nicheblog.dreamdiary.feature.jrnl.day.service.helper.JrnlDayFilterHelper;
import io.nicheblog.dreamdiary.feature.jrnl.day.service.helper.JrnlDayHldyHelper;
import io.nicheblog.dreamdiary.feature.jrnl.day.service.helper.JrnlDayViewHelper;
import io.nicheblog.dreamdiary.feature.jrnl.diary.model.JrnlDiaryDto;
import io.nicheblog.dreamdiary.feature.jrnl.dream.model.JrnlDreamDto;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import io.nicheblog.dreamdiary.infrastructure.cache.util.EhCacheUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
    private final RelatedContentQueryService relatedContentQueryService;

    /**
     * 연월기준 목록 조회 + enrich
     *
     * @param username 조회 사용자 계정명
     * @param searchParam 조회 조건 (연도, 월, 필터 조건 포함)
     * @return {@link List} -- 가공 완료된 일자 DTO 목록
     */
    public List<JrnlDayDto> getYyMnthListDtoEnrichedByUser(final String username, final JrnlDaySearchParam searchParam) throws Exception {
        if (searchParam == null) return List.of();
        final String resolvedUsername = AuthUtils.requireUsername(username);

        final List<JrnlDayDto> listDto = jrnlDayService.getCachedYyMnthListDtoByUser(
                resolvedUsername,
                searchParam.getYy(),
                searchParam.getMnth()
        );
        final List<JrnlDayDto> filteredList = JrnlDayFilterHelper.filterInMemory(listDto, searchParam);

        return this.enrichList(resolvedUsername, filteredList, searchParam);
    }

    /**
     * 기준일(standard day) 목록 조회 + enrich
     *
     * @param username 조회 사용자 계정명
     * @param searchParam 조회 조건
     * @return {@link List} -- 가공 완료된 DTO 목록
     */
    public List<JrnlDayDto> getStdrdDaysDtoEnrichedByUser(final String username, final JrnlDaySearchParam searchParam) throws Exception {
        final String resolvedUsername = AuthUtils.requireUsername(username);
        final List<JrnlDayDto> listDto = jrnlDayService.getJrnlStdrdDaysByUser(resolvedUsername, searchParam);
        return this.enrichList(resolvedUsername, listDto, searchParam);
    }

    /**
     * 주간 목록 조회 + enrich
     *
     * @param username 조회 사용자 계정명
     * @param searchParam 조회 조건
     * @return {@link List} -- 가공 완료된 DTO 목록
     */
    public List<JrnlDayDto> getWeeklyListDtoEnrichedByUser(final String username, final JrnlDaySearchParam searchParam) throws Exception {
        if (searchParam == null) return List.of();
        final String resolvedUsername = AuthUtils.requireUsername(username);

        final String weekStartDt = StringUtils.isNotBlank(searchParam.getWeekStartDt())
                ? searchParam.getWeekStartDt()
                : DateUtils.getWeekStartDateStr(searchParam.getStdrdDt());
        if (StringUtils.isBlank(weekStartDt)) return List.of();
        searchParam.setWeekStartDt(weekStartDt);

        final List<JrnlDayDto> listDto = jrnlDayService.getCachedWeeklyListDtoByUser(resolvedUsername, weekStartDt);
        final List<JrnlDayDto> filteredList = JrnlDayFilterHelper.filterInMemory(listDto, searchParam);
        return this.enrichWeeklyList(resolvedUsername, filteredList, searchParam);
    }

    /**
     * 메타 기준 조회 + enrich
     *
     * @param username 조회 사용자 계정명
     * @param searchParam 조회 조건 (metaId 포함)
     * @return {@link List} -- 가공 완료된 DTO 목록
     */
    public List<JrnlDayDto> getListDtoByMetaIdEnrichedByUser(final String username, final JrnlDaySearchParam searchParam) throws Exception {
        final String resolvedUsername = AuthUtils.requireUsername(username);
        final List<JrnlDayDto> listDto = jrnlDayService.getListDtoByMetaIdAndUser(resolvedUsername, searchParam);
        return this.enrichList(resolvedUsername, listDto, searchParam);
    }

    /**
     * 태그 기준 조회 + enrich
     *
     * @param username 조회 사용자 계정명
     * @param searchParam 조회 조건 (tagId 포함)
     * @return {@link List} -- 가공 완료된 DTO 목록
     */
    public List<JrnlDayDto> getListDtoByTagIdEnrichedByUser(final String username, final JrnlDaySearchParam searchParam) throws Exception {
        final String resolvedUsername = AuthUtils.requireUsername(username);
        final List<JrnlDayDto> listDto = jrnlDayService.getListDtoByTagIdAndUser(resolvedUsername, searchParam);
        return this.enrichList(resolvedUsername, listDto, searchParam);
    }

    /**
     * 상세 조회 + enrich
     *
     * @param username 조회 사용자 계정명
     * @param key PK
     * @return {@link JrnlDayDto} -- 가공 완료된 DTO
     */
    public JrnlDayDto getDtlDtoEnrichedByUser(final String username, final Integer key) throws Exception {
        final String resolvedUsername = AuthUtils.requireUsername(username);
        final JrnlDayDto retrieved = jrnlDayService.getCachedDtlDtoByUser(resolvedUsername, key);
        return this.enrichDetail(resolvedUsername, retrieved);
    }

    /**
     * 목록 공통 enrich 처리
     * 1) 휴일 정보 매핑 2) 상태 병합 (조회 조건 기반) 3) 태그 요약 적용
     *
     * @param listDto 조회 결과 리스트
     * @param searchParam 조회 조건
     * @return enrich 완료 리스트
     */
    private List<JrnlDayDto> enrichList(final String username, final List<JrnlDayDto> listDto, final JrnlDaySearchParam searchParam) throws Exception {
        if (listDto == null) return null;

        JrnlDayHldyHelper.setHldyInfo(listDto, getHldyMap());
        if (searchParam != null) {
            JrnlDayViewHelper.mergeStates(username, listDto, searchParam);
            JrnlDayViewHelper.applyChapterTagSummary(listDto, searchParam);
        }
        this.mergeRelatedContents(username, listDto);

        return listDto;
    }

    /**
     * 주간 목록 전용 enrich 처리
     *
     * @param listDto 조회 결과 리스트
     * @param searchParam 조회 조건
     * @return enrich 완료 리스트
     */
    private List<JrnlDayDto> enrichWeeklyList(final String username, final List<JrnlDayDto> listDto, final JrnlDaySearchParam searchParam) throws Exception {
        if (listDto == null) return null;

        JrnlDayHldyHelper.setHldyInfo(listDto, getHldyMap());
        if (searchParam != null) {
            JrnlDayViewHelper.mergeWeeklyStates(username, listDto, searchParam);
            JrnlDayViewHelper.applyChapterTagSummary(listDto, searchParam);
        }
        this.mergeRelatedContents(username, listDto);

        return listDto;
    }

    /**
     * 단건 공통 enrich 처리
     *
     * @param retrieved 조회 결과
     * @return enrich 완료 DTO
     */
    private JrnlDayDto enrichDetail(final String username, final JrnlDayDto retrieved) throws Exception {
        if (retrieved == null) return null;

        JrnlDayHldyHelper.setHldyInfo(retrieved, getHldyMap());
        JrnlDayViewHelper.mergeStates(username, retrieved);
        this.mergeRelatedContents(username, List.of(retrieved));

        return retrieved;
    }

    private void mergeRelatedContents(final String username, final List<JrnlDayDto> listDto) throws Exception {
        if (listDto == null || listDto.isEmpty()) return;

        final List<BaseClsfKey> refKeyList = new ArrayList<>();
        for (final JrnlDayDto jrnlDay : listDto) {
            if (jrnlDay == null) continue;

            final List<JrnlChapterDto> jrnlChapterList = jrnlDay.getJrnlChapterList();
            if (jrnlChapterList != null) {
                for (final JrnlChapterDto jrnlChapter : jrnlChapterList) {
                    if (jrnlChapter == null || jrnlChapter.getJrnlDiaryList() == null) continue;

                    for (final JrnlDiaryDto jrnlDiary : jrnlChapter.getJrnlDiaryList()) {
                        if (jrnlDiary == null || jrnlDiary.getId() == null) continue;
                        refKeyList.add(new BaseClsfKey(jrnlDiary.getId(), ContentType.JRNL_DIARY));
                    }
                }
            }

            this.collectDreamRefKeys(refKeyList, jrnlDay.getJrnlDreamList());
            this.collectDreamRefKeys(refKeyList, jrnlDay.getJrnlElseDreamList());
        }

        final Map<String, List<RelatedContentDto>> relatedMap = relatedContentQueryService.getRelatedContentMapByRefs(refKeyList, username);

        for (final JrnlDayDto jrnlDay : listDto) {
            if (jrnlDay == null) continue;

            final List<JrnlChapterDto> jrnlChapterList = jrnlDay.getJrnlChapterList();
            if (jrnlChapterList != null) {
                for (final JrnlChapterDto jrnlChapter : jrnlChapterList) {
                    if (jrnlChapter == null || jrnlChapter.getJrnlDiaryList() == null) continue;

                    for (final JrnlDiaryDto jrnlDiary : jrnlChapter.getJrnlDiaryList()) {
                        if (jrnlDiary == null || jrnlDiary.getId() == null) continue;
                        jrnlDiary.setRelatedContentList(this.getRelatedList(relatedMap, ContentType.JRNL_DIARY.key, jrnlDiary.getId()));
                    }
                }
            }

            this.applyDreamRelatedContents(relatedMap, jrnlDay.getJrnlDreamList());
            this.applyDreamRelatedContents(relatedMap, jrnlDay.getJrnlElseDreamList());
        }
    }

    private void collectDreamRefKeys(final List<BaseClsfKey> refKeyList, final List<JrnlDreamDto> jrnlDreamList) {
        if (jrnlDreamList == null) return;

        for (final JrnlDreamDto jrnlDream : jrnlDreamList) {
            if (jrnlDream == null || jrnlDream.getId() == null) continue;
            refKeyList.add(new BaseClsfKey(jrnlDream.getId(), ContentType.JRNL_DREAM));
        }
    }

    private void applyDreamRelatedContents(
            final Map<String, List<RelatedContentDto>> relatedMap,
            final List<JrnlDreamDto> jrnlDreamList
    ) {
        if (jrnlDreamList == null) return;

        for (final JrnlDreamDto jrnlDream : jrnlDreamList) {
            if (jrnlDream == null || jrnlDream.getId() == null) continue;
            jrnlDream.setRelatedContentList(this.getRelatedList(relatedMap, ContentType.JRNL_DREAM.key, jrnlDream.getId()));
        }
    }

    private List<RelatedContentDto> getRelatedList(
            final Map<String, List<RelatedContentDto>> relatedMap,
            final String contentType,
            final Integer id
    ) {
        return relatedMap.getOrDefault(String.format("%s:%d", contentType, id), List.of());
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
