package io.nicheblog.dreamdiary.feature.journal.day.service;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.clsf._shared.entity.BaseClsfKey;
import io.nicheblog.dreamdiary.feature.clsf._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.clsf.related.model.RelatedContentDto;
import io.nicheblog.dreamdiary.feature.clsf.related.service.RelatedContentQueryService;
import io.nicheblog.dreamdiary.feature.journal.chapter.model.JournalChapterDto;
import io.nicheblog.dreamdiary.feature.journal.day.model.JournalDayDto;
import io.nicheblog.dreamdiary.feature.journal.day.model.JournalDaySearchParam;
import io.nicheblog.dreamdiary.feature.journal.day.service.helper.JournalDayFilterHelper;
import io.nicheblog.dreamdiary.feature.journal.day.service.helper.JournalDayHldyHelper;
import io.nicheblog.dreamdiary.feature.journal.day.service.helper.JournalDayViewHelper;
import io.nicheblog.dreamdiary.feature.journal.diary.model.JournalDiaryDto;
import io.nicheblog.dreamdiary.feature.journal.dream.model.JournalDreamDto;
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
 * JournalDayQueryService
 * <pre>
 *   JournalDay 조회 결과를 화면에 필요한 상태/메타 정보로 조립(enrich)하는 Query 전용 서비스
 * </pre>
 *
 * @author nichefish
 */
@Service("journalDayQueryService")
@RequiredArgsConstructor
@Log4j2
public class JournalDayQueryService {

    private final JournalDayService journalDayService;
    private final RelatedContentQueryService relatedContentQueryService;

    /**
     * 연월기준 목록 조회 + enrich
     *
     * @param username 조회 사용자 계정명
     * @param searchParam 조회 조건 (연도, 월, 필터 조건 포함)
     * @return {@link List} -- 가공 완료된 일자 DTO 목록
     */
    public List<JournalDayDto> getYyMnthListDtoEnrichedByUser(final String username, final JournalDaySearchParam searchParam) throws Exception {
        if (searchParam == null) return List.of();
        final String resolvedUsername = AuthUtils.requireUsername(username);

        final List<JournalDayDto> listDto = journalDayService.getCachedYyMnthListDtoByUser(
                resolvedUsername,
                searchParam.getYy(),
                searchParam.getMnth()
        );
        final List<JournalDayDto> filteredList = JournalDayFilterHelper.filterInMemory(listDto, searchParam);

        return this.enrichList(resolvedUsername, filteredList, searchParam);
    }

    /**
     * 기준일(standard day) 목록 조회 + enrich
     *
     * @param username 조회 사용자 계정명
     * @param searchParam 조회 조건
     * @return {@link List} -- 가공 완료된 DTO 목록
     */
    public List<JournalDayDto> getStdrdDaysDtoEnrichedByUser(final String username, final JournalDaySearchParam searchParam) throws Exception {
        final String resolvedUsername = AuthUtils.requireUsername(username);
        final List<JournalDayDto> listDto = journalDayService.getJournalStdrdDaysByUser(resolvedUsername, searchParam);
        return this.enrichList(resolvedUsername, listDto, searchParam);
    }

    /**
     * 주간 목록 조회 + enrich
     *
     * @param username 조회 사용자 계정명
     * @param searchParam 조회 조건
     * @return {@link List} -- 가공 완료된 DTO 목록
     */
    public List<JournalDayDto> getWeeklyListDtoEnrichedByUser(final String username, final JournalDaySearchParam searchParam) throws Exception {
        if (searchParam == null) return List.of();
        final String resolvedUsername = AuthUtils.requireUsername(username);

        final String weekStartDt = StringUtils.isNotBlank(searchParam.getWeekStartDt())
                ? searchParam.getWeekStartDt()
                : DateUtils.getWeekStartDateStr(searchParam.getStdrdDt());
        if (StringUtils.isBlank(weekStartDt)) return List.of();
        searchParam.setWeekStartDt(weekStartDt);

        final List<JournalDayDto> listDto = journalDayService.getCachedWeeklyListDtoByUser(resolvedUsername, weekStartDt);
        final List<JournalDayDto> filteredList = JournalDayFilterHelper.filterInMemory(listDto, searchParam);
        return this.enrichWeeklyList(resolvedUsername, filteredList, searchParam);
    }

    /**
     * 메타 기준 조회 + enrich
     *
     * @param username 조회 사용자 계정명
     * @param searchParam 조회 조건 (metaId 포함)
     * @return {@link List} -- 가공 완료된 DTO 목록
     */
    public List<JournalDayDto> getListDtoByMetaIdEnrichedByUser(final String username, final JournalDaySearchParam searchParam) throws Exception {
        final String resolvedUsername = AuthUtils.requireUsername(username);
        final List<JournalDayDto> listDto = journalDayService.getListDtoByMetaIdAndUser(resolvedUsername, searchParam);
        return this.enrichList(resolvedUsername, listDto, searchParam);
    }

    /**
     * 태그 기준 조회 + enrich
     *
     * @param username 조회 사용자 계정명
     * @param searchParam 조회 조건 (tagId 포함)
     * @return {@link List} -- 가공 완료된 DTO 목록
     */
    public List<JournalDayDto> getListDtoByTagIdEnrichedByUser(final String username, final JournalDaySearchParam searchParam) throws Exception {
        final String resolvedUsername = AuthUtils.requireUsername(username);
        final List<JournalDayDto> listDto = journalDayService.getListDtoByTagIdAndUser(resolvedUsername, searchParam);
        return this.enrichList(resolvedUsername, listDto, searchParam);
    }

    /**
     * 상세 조회 + enrich
     *
     * @param username 조회 사용자 계정명
     * @param key PK
     * @return {@link JournalDayDto} -- 가공 완료된 DTO
     */
    public JournalDayDto getDtlDtoEnrichedByUser(final String username, final Integer key) throws Exception {
        final String resolvedUsername = AuthUtils.requireUsername(username);
        final JournalDayDto retrieved = journalDayService.getCachedDtlDtoByUser(resolvedUsername, key);
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
    private List<JournalDayDto> enrichList(final String username, final List<JournalDayDto> listDto, final JournalDaySearchParam searchParam) throws Exception {
        if (listDto == null) return null;

        JournalDayHldyHelper.setHldyInfo(listDto, getHldyMap());
        if (searchParam != null) {
            JournalDayViewHelper.mergeStates(username, listDto, searchParam);
            JournalDayViewHelper.applyChapterTagSummary(listDto, searchParam);
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
    private List<JournalDayDto> enrichWeeklyList(final String username, final List<JournalDayDto> listDto, final JournalDaySearchParam searchParam) throws Exception {
        if (listDto == null) return null;

        JournalDayHldyHelper.setHldyInfo(listDto, getHldyMap());
        if (searchParam != null) {
            JournalDayViewHelper.mergeWeeklyStates(username, listDto, searchParam);
            JournalDayViewHelper.applyChapterTagSummary(listDto, searchParam);
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
    private JournalDayDto enrichDetail(final String username, final JournalDayDto retrieved) throws Exception {
        if (retrieved == null) return null;

        JournalDayHldyHelper.setHldyInfo(retrieved, getHldyMap());
        JournalDayViewHelper.mergeStates(username, retrieved);
        this.mergeRelatedContents(username, List.of(retrieved));

        return retrieved;
    }

    private void mergeRelatedContents(final String username, final List<JournalDayDto> listDto) throws Exception {
        if (listDto == null || listDto.isEmpty()) return;

        final List<BaseClsfKey> refKeyList = new ArrayList<>();
        for (final JournalDayDto journalDay : listDto) {
            if (journalDay == null) continue;

            final List<JournalChapterDto> journalChapterList = journalDay.getJournalChapterList();
            if (journalChapterList != null) {
                for (final JournalChapterDto journalChapter : journalChapterList) {
                    if (journalChapter == null || journalChapter.getJournalDiaryList() == null) continue;

                    for (final JournalDiaryDto journalDiary : journalChapter.getJournalDiaryList()) {
                        if (journalDiary == null || journalDiary.getId() == null) continue;
                        refKeyList.add(new BaseClsfKey(journalDiary.getId(), ContentType.JOURNAL_DIARY));
                    }
                }
            }

            this.collectDreamRefKeys(refKeyList, journalDay.getJournalDreamList());
            this.collectDreamRefKeys(refKeyList, journalDay.getJournalElseDreamList());
        }

        final Map<String, List<RelatedContentDto>> relatedMap = relatedContentQueryService.getRelatedContentMapByRefs(refKeyList, username);

        for (final JournalDayDto journalDay : listDto) {
            if (journalDay == null) continue;

            final List<JournalChapterDto> journalChapterList = journalDay.getJournalChapterList();
            if (journalChapterList != null) {
                for (final JournalChapterDto journalChapter : journalChapterList) {
                    if (journalChapter == null || journalChapter.getJournalDiaryList() == null) continue;

                    for (final JournalDiaryDto journalDiary : journalChapter.getJournalDiaryList()) {
                        if (journalDiary == null || journalDiary.getId() == null) continue;
                        journalDiary.setRelatedContentList(this.getRelatedList(relatedMap, ContentType.JOURNAL_DIARY.key, journalDiary.getId()));
                    }
                }
            }

            this.applyDreamRelatedContents(relatedMap, journalDay.getJournalDreamList());
            this.applyDreamRelatedContents(relatedMap, journalDay.getJournalElseDreamList());
        }
    }

    private void collectDreamRefKeys(final List<BaseClsfKey> refKeyList, final List<JournalDreamDto> journalDreamList) {
        if (journalDreamList == null) return;

        for (final JournalDreamDto journalDream : journalDreamList) {
            if (journalDream == null || journalDream.getId() == null) continue;
            refKeyList.add(new BaseClsfKey(journalDream.getId(), ContentType.JOURNAL_DREAM));
        }
    }

    private void applyDreamRelatedContents(
            final Map<String, List<RelatedContentDto>> relatedMap,
            final List<JournalDreamDto> journalDreamList
    ) {
        if (journalDreamList == null) return;

        for (final JournalDreamDto journalDream : journalDreamList) {
            if (journalDream == null || journalDream.getId() == null) continue;
            journalDream.setRelatedContentList(this.getRelatedList(relatedMap, ContentType.JOURNAL_DREAM.key, journalDream.getId()));
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

