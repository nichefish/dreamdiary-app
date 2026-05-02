package io.nicheblog.dreamdiary.feature.journal.day.service;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.attachable._shared.entity.BaseAttachableKey;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.lifecycle.service.LifecycleService;
import io.nicheblog.dreamdiary.feature.attachable.related.model.RelatedContentDto;
import io.nicheblog.dreamdiary.feature.attachable.related.service.RelatedContentQueryService;
import io.nicheblog.dreamdiary.feature.journal._shared.lifecycle.JournalLifecycleViewHelper;
import io.nicheblog.dreamdiary.feature.journal.chapter.model.JournalChapterDto;
import io.nicheblog.dreamdiary.feature.journal.day.model.JournalDayDto;
import io.nicheblog.dreamdiary.feature.journal.day.model.JournalDaySearchParam;
import io.nicheblog.dreamdiary.feature.journal.day.service.helper.JournalDayFilterHelper;
import io.nicheblog.dreamdiary.feature.journal.day.service.helper.JournalDayHolydayHelper;
import io.nicheblog.dreamdiary.feature.journal.day.service.helper.JournalDayViewHelper;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryDto;
import io.nicheblog.dreamdiary.feature.journal.entry.service.helper.JournalEntryViewProjectionHelper;
import io.nicheblog.dreamdiary.feature.journal.entry.service.policy.JournalEntryTypePolicy;
import io.nicheblog.dreamdiary.feature.journal.interpretation.model.JournalInterpretationDto;
import io.nicheblog.dreamdiary.feature.journal.interpretation.service.JournalInterpretationQueryService;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import io.nicheblog.dreamdiary.infrastructure.cache.util.EhCacheUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * JournalDayQueryService
 * <pre>
 *  JournalDay 조회 결과를 화면에 필요한 상태/메타 정보로 보강(enrich)하는 Query 전용 서비스.
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class JournalDayQueryService {

    private final JournalDayService journalDayService;
    private final RelatedContentQueryService relatedContentQueryService;
    private final JournalInterpretationQueryService journalInterpretationQueryService;
    private final LifecycleService lifecycleService;

    /**
     * 연월 기준 목록 조회 후 화면 정보를 보강한다.
     *
     * @param username 사용자 계정명
     * @param searchParam 검색 조건 (연도, 월, 필터 조건 포함)
     * @return {@link List} -- 보강된 저널 일자 DTO 목록
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
     * 기준일 목록 조회 후 화면 정보를 보강한다.
     *
     * @param username 사용자 계정명
     * @param searchParam 검색 조건
     * @return {@link List} -- 보강된 DTO 목록
     */
    public List<JournalDayDto> getStdrdDaysDtoEnrichedByUser(final String username, final JournalDaySearchParam searchParam) throws Exception {
        final String resolvedUsername = AuthUtils.requireUsername(username);
        final List<JournalDayDto> listDto = journalDayService.getJournalStdrdDaysByUser(resolvedUsername, searchParam);
        return this.enrichList(resolvedUsername, listDto, searchParam);
    }

    /**
     * 주간 목록 조회 후 화면 정보를 보강한다.
     *
     * @param username 사용자 계정명
     * @param searchParam 검색 조건
     * @return {@link List} -- 보강된 DTO 목록
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
     * 메타 기준 목록 조회 후 화면 정보를 보강한다.
     *
     * @param username 사용자 계정명
     * @param searchParam 검색 조건 (metaId 포함)
     * @return {@link List} -- 보강된 DTO 목록
     */
    public List<JournalDayDto> getListDtoByMetaIdEnrichedByUser(final String username, final JournalDaySearchParam searchParam) throws Exception {
        final String resolvedUsername = AuthUtils.requireUsername(username);
        final List<JournalDayDto> listDto = journalDayService.getListDtoByMetaIdAndUser(resolvedUsername, searchParam);
        return this.enrichList(resolvedUsername, listDto, searchParam);
    }

    /**
     * 태그 기준 목록 조회 후 화면 정보를 보강한다.
     *
     * @param username 사용자 계정명
     * @param searchParam 검색 조건 (tagId 포함)
     * @return {@link List} -- 보강된 DTO 목록
     */
    public List<JournalDayDto> getListDtoByTagIdEnrichedByUser(final String username, final JournalDaySearchParam searchParam) throws Exception {
        final String resolvedUsername = AuthUtils.requireUsername(username);
        final List<JournalDayDto> listDto = journalDayService.getListDtoByTagIdAndUser(resolvedUsername, searchParam);
        return this.enrichList(resolvedUsername, listDto, searchParam);
    }

    /**
     * 상세 조회 후 화면 정보를 보강한다.
     *
     * @param username 사용자 계정명
     * @param key PK
     * @return {@link JournalDayDto} -- 보강된 DTO
     */
    public JournalDayDto getDtlDtoEnrichedByUser(final String username, final Integer key) throws Exception {
        final String resolvedUsername = AuthUtils.requireUsername(username);
        final JournalDayDto retrieved = journalDayService.getCachedDtlDtoByUser(resolvedUsername, key);
        return this.enrichDetail(resolvedUsername, retrieved);
    }

    /**
     * 목록 공통 enrich 처리.
     * 1) 휴일 정보 매핑 2) 해석 정보 병합 3) 상태 병합 4) 태그 요약 적용 5) 관련글 병합
     *
     * @param username 사용자 계정명
     * @param listDto 조회 결과 리스트
     * @param searchParam 검색 조건
     * @return enrich 완료 리스트
     */
    private List<JournalDayDto> enrichList(final String username, final List<JournalDayDto> listDto, final JournalDaySearchParam searchParam) throws Exception {
        if (listDto == null) return null;

        JournalDayHolydayHelper.setHolydayInfo(listDto, getHolydayMap());
        this.mergeInterpretations(username, listDto);
        if (searchParam != null) {
            JournalDayViewHelper.mergeStates(username, listDto, searchParam);
            JournalDayViewHelper.applyChapterTagSummary(listDto, searchParam);
        }
        this.mergeLifecycles(listDto);
        this.mergeRelatedContents(username, listDto);

        return listDto;
    }

    /**
     * 주간 목록 전용 enrich 처리.
     *
     * @param username 사용자 계정명
     * @param listDto 조회 결과 리스트
     * @param searchParam 검색 조건
     * @return enrich 완료 리스트
     */
    private List<JournalDayDto> enrichWeeklyList(final String username, final List<JournalDayDto> listDto, final JournalDaySearchParam searchParam) throws Exception {
        if (listDto == null) return null;

        JournalDayHolydayHelper.setHolydayInfo(listDto, getHolydayMap());
        this.mergeInterpretations(username, listDto);
        if (searchParam != null) {
            JournalDayViewHelper.mergeWeeklyStates(username, listDto, searchParam);
            JournalDayViewHelper.applyChapterTagSummary(listDto, searchParam);
        }
        this.mergeLifecycles(listDto);
        this.mergeRelatedContents(username, listDto);

        return listDto;
    }

    /**
     * 상세 공통 enrich 처리.
     *
     * @param username 사용자 계정명
     * @param retrieved 조회 결과
     * @return enrich 완료 DTO
     */
    private JournalDayDto enrichDetail(final String username, final JournalDayDto retrieved) throws Exception {
        if (retrieved == null) return null;

        JournalDayHolydayHelper.setHolydayInfo(retrieved, getHolydayMap());
        this.mergeInterpretations(username, List.of(retrieved));
        JournalDayViewHelper.mergeStates(username, retrieved);
        this.mergeLifecycles(List.of(retrieved));
        this.mergeRelatedContents(username, List.of(retrieved));

        return retrieved;
    }

    private void mergeInterpretations(final String username, final List<JournalDayDto> listDto) throws Exception {
        if (listDto == null || listDto.isEmpty()) return;

        final List<BaseAttachableKey> refKeyList = new ArrayList<>();
        for (final JournalEntryTypePolicy policy : JournalEntryTypePolicy.interpretableTypes()) {
            this.forEachEntryByType(listDto, policy.contentType, entry ->
                    refKeyList.add(new BaseAttachableKey(entry.getId(), policy.contentType))
            );
        }

        final Map<String, List<JournalInterpretationDto>> interpretationMap =
                journalInterpretationQueryService.getInterpretationMapByRefs(refKeyList, username);

        for (final JournalEntryTypePolicy policy : JournalEntryTypePolicy.interpretableTypes()) {
            this.forEachEntryByType(listDto, policy.contentType, entry ->
                    entry.setJournalInterpretationList(
                            interpretationMap.getOrDefault(buildRefMapKey(policy.contentType.key, entry.getId()), List.of())
                    )
            );
        }
    }

    private void mergeRelatedContents(final String username, final List<JournalDayDto> listDto) throws Exception {
        if (listDto == null || listDto.isEmpty()) return;

        final List<BaseAttachableKey> refKeyList = new ArrayList<>();
        for (final JournalEntryTypePolicy policy : JournalEntryTypePolicy.values()) {
            this.forEachEntryByType(listDto, policy.contentType, entry ->
                    refKeyList.add(new BaseAttachableKey(entry.getId(), policy.contentType))
            );
        }

        final Map<String, List<RelatedContentDto>> relatedMap =
                relatedContentQueryService.getRelatedContentMapByRefs(refKeyList, username);

        for (final JournalEntryTypePolicy policy : JournalEntryTypePolicy.values()) {
            this.forEachEntryByType(listDto, policy.contentType, entry ->
                    entry.setRelatedContentList(this.getRelatedList(relatedMap, policy.contentType.key, entry.getId()))
            );
        }
    }

    /**
     * 캐시 누락 여부와 상관없이 DB 기준 lifecycle 값을 일자 트리에 병합한다.
     *
     * @param listDto 일자 목록
     */
    private void mergeLifecycles(final List<JournalDayDto> listDto) {
        if (listDto == null || listDto.isEmpty()) return;

        for (final JournalEntryTypePolicy policy : JournalEntryTypePolicy.interpretableTypes()) {
            final List<JournalEntryDto> entryList = this.collectEntriesByType(listDto, policy.contentType);
            final List<Integer> entryIds = entryList.stream()
                    .map(JournalEntryDto::getId)
                    .filter(java.util.Objects::nonNull)
                    .distinct()
                    .toList();
            JournalLifecycleViewHelper.applyEntryLifecycle(
                    entryList,
                    lifecycleService.getLifecycleMap(policy.contentType, entryIds)
            );

            final List<JournalInterpretationDto> interpretationList = entryList.stream()
                    .flatMap(entry -> entry.getJournalInterpretationList() == null
                            ? java.util.stream.Stream.empty()
                            : entry.getJournalInterpretationList().stream())
                    .filter(java.util.Objects::nonNull)
                    .toList();
            final List<Integer> interpretationIds = interpretationList.stream()
                    .map(JournalInterpretationDto::getId)
                    .filter(java.util.Objects::nonNull)
                    .distinct()
                    .toList();
            JournalLifecycleViewHelper.applyInterpretationLifecycle(
                    interpretationList,
                    lifecycleService.getLifecycleMap(ContentType.JOURNAL_INTERPRETATION, interpretationIds)
            );
        }
    }

    private List<RelatedContentDto> getRelatedList(
            final Map<String, List<RelatedContentDto>> relatedMap,
            final String contentType,
            final Integer id
    ) {
        return relatedMap.getOrDefault(buildRefMapKey(contentType, id), List.of());
    }

    private String buildRefMapKey(final String contentType, final Integer id) {
        return String.format("%s:%d", contentType, id);
    }

    private void forEachEntryByType(
            final List<JournalDayDto> listDto,
            final ContentType contentType,
            final Consumer<JournalEntryDto> consumer
    ) {
        if (listDto == null || contentType == null || consumer == null) return;

        for (final JournalDayDto journalDay : listDto) {
            if (journalDay == null || journalDay.getJournalChapterList() == null) continue;

            for (final JournalChapterDto journalChapter : journalDay.getJournalChapterList()) {
                if (journalChapter == null) continue;
                final List<JournalEntryDto> entryList =
                        JournalEntryViewProjectionHelper.getEntriesByType(journalChapter, contentType);
                forEachEntry(entryList, consumer);
            }
        }
    }

    /**
     * 일자 트리에서 특정 컨텐츠 타입의 엔트리를 모은다.
     *
     * @param listDto 일자 목록
     * @param contentType 컨텐츠 타입
     * @return 엔트리 목록
     */
    private List<JournalEntryDto> collectEntriesByType(
            final List<JournalDayDto> listDto,
            final ContentType contentType
    ) {
        final List<JournalEntryDto> entryList = new ArrayList<>();
        this.forEachEntryByType(listDto, contentType, entryList::add);
        return entryList;
    }

    private void forEachEntry(final List<JournalEntryDto> entryList, final Consumer<JournalEntryDto> consumer) {
        if (entryList == null || consumer == null) return;

        for (final JournalEntryDto entry : entryList) {
            if (entry == null || entry.getId() == null) continue;
            consumer.accept(entry);
        }
    }

    /**
     * 휴일 정보 캐시를 조회한다.
     *
     * @return 휴일 맵
     */
    @SuppressWarnings("unchecked")
    private Map<String, List<String>> getHolydayMap() {
        return (Map<String, List<String>>) EhCacheUtils.getObjectFromCache("holydayMap");
    }
}
