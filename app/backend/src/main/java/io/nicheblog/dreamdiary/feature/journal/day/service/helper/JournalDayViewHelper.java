package io.nicheblog.dreamdiary.feature.journal.day.service.helper;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.tag.model.TagContentDto;
import io.nicheblog.dreamdiary.feature.attachable.tag.model.cmpstn.TagCmpstn;
import io.nicheblog.dreamdiary.feature.journal._shared.lifecycle.JournalLifecycleCacheRegistry;
import io.nicheblog.dreamdiary.feature.journal._shared.state.JournalState;
import io.nicheblog.dreamdiary.feature.journal._shared.state.JournalStateCacheRegistry;
import io.nicheblog.dreamdiary.feature.journal.chapter.model.JournalChapterDto;
import io.nicheblog.dreamdiary.feature.journal.chapter.service.helper.JournalChapterViewHelper;
import io.nicheblog.dreamdiary.feature.journal.day.model.JournalDayDto;
import io.nicheblog.dreamdiary.feature.journal.day.model.JournalDaySearchParam;
import io.nicheblog.dreamdiary.feature.journal.day.model.JournalDreamSectionDto;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryDto;
import io.nicheblog.dreamdiary.feature.journal.entry.service.helper.JournalEntryStateViewHelper;
import io.nicheblog.dreamdiary.feature.journal.entry.service.helper.JournalEntryViewProjectionHelper;
import io.nicheblog.dreamdiary.infrastructure.cache.util.EhCacheUtils;
import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.interceptor.SimpleKey;

import java.util.*;

/**
 * JournalDayViewHelper
 * 저널 일자 뷰 모델에 state·lifecycle 캐시 값을 병합한다.
 *
 * @author nichefish
 */
@UtilityClass
public final class JournalDayViewHelper {

    /**
     * 월간 일자 목록에 state와 lifecycle 캐시 값을 병합한다.
     *
     * @param listDto 일자 목록
     * @param searchParam 일자 검색 조건
     */
    @SuppressWarnings("unchecked")
    public static void mergeStates(final String username, final List<JournalDayDto> listDto, final JournalDaySearchParam searchParam) {
        if (CollectionUtils.isEmpty(listDto) || searchParam == null) return;

        final Object cacheKey = new SimpleKey(username, searchParam.getYy(), searchParam.getMnth());

        final Map<Integer, JournalState> chapterMap = getStateMap(cacheKey, ContentType.JOURNAL_CHAPTER, false);
        final Map<Integer, JournalState> diaryMap = getStateMap(cacheKey, ContentType.JOURNAL_DIARY, false);
        final Map<Integer, JournalState> dreamMap = getStateMap(cacheKey, ContentType.JOURNAL_DREAM, false);
        final Map<Integer, JournalState> reflectionStateMap = getStateMap(cacheKey, ContentType.JOURNAL_REFLECTION, false);
        final Map<Integer, String> diaryLifecycleMap = getLifecycleMap(cacheKey, ContentType.JOURNAL_DIARY, false);
        final Map<Integer, String> dreamLifecycleMap = getLifecycleMap(cacheKey, ContentType.JOURNAL_DREAM, false);
        final Map<Integer, String> reflectionLifecycleMap = getLifecycleMap(cacheKey, ContentType.JOURNAL_REFLECTION, false);

        JournalDayViewHelper.applyStates(listDto, chapterMap, diaryMap, dreamMap, reflectionStateMap, diaryLifecycleMap, dreamLifecycleMap, reflectionLifecycleMap, searchParam);
    }

    /**
     * 주간 일자 목록에 state와 lifecycle 캐시 값을 병합한다.
     *
     * @param listDto 일자 목록
     * @param searchParam 일자 검색 조건
     */
    @SuppressWarnings("unchecked")
    public static void mergeWeeklyStates(final String username, final List<JournalDayDto> listDto, final JournalDaySearchParam searchParam) {
        if (CollectionUtils.isEmpty(listDto) || searchParam == null) return;
        if (StringUtils.isBlank(searchParam.getWeekStartDt())) return;

        final Object cacheKey = new SimpleKey(
                username,
                searchParam.getWeekStartDt()
        );

        final Map<Integer, JournalState> chapterMap = getStateMap(cacheKey, ContentType.JOURNAL_CHAPTER, true);
        final Map<Integer, JournalState> diaryMap = getStateMap(cacheKey, ContentType.JOURNAL_DIARY, true);
        final Map<Integer, JournalState> dreamMap = getStateMap(cacheKey, ContentType.JOURNAL_DREAM, true);
        final Map<Integer, JournalState> reflectionStateMap = getStateMap(cacheKey, ContentType.JOURNAL_REFLECTION, true);
        final Map<Integer, String> diaryLifecycleMap = getLifecycleMap(cacheKey, ContentType.JOURNAL_DIARY, true);
        final Map<Integer, String> dreamLifecycleMap = getLifecycleMap(cacheKey, ContentType.JOURNAL_DREAM, true);
        final Map<Integer, String> reflectionLifecycleMap = getLifecycleMap(cacheKey, ContentType.JOURNAL_REFLECTION, true);

        JournalDayViewHelper.applyStates(listDto, chapterMap, diaryMap, dreamMap, reflectionStateMap, diaryLifecycleMap, dreamLifecycleMap, reflectionLifecycleMap, searchParam);
    }

    /**
     * 단일 일자에 state와 lifecycle 캐시 값을 병합한다.
     *
     * @param journalDay 일자 DTO
     */
    @SuppressWarnings("unchecked")
    public static void mergeStates(final String username, final JournalDayDto journalDay) {
        if (journalDay == null) return;

        final Object cacheKey = new SimpleKey(username, journalDay.getYy(), journalDay.getMnth());

        final Map<Integer, JournalState> chapterMap = getStateMap(cacheKey, ContentType.JOURNAL_CHAPTER, false);
        final Map<Integer, JournalState> diaryMap = getStateMap(cacheKey, ContentType.JOURNAL_DIARY, false);
        final Map<Integer, JournalState> dreamMap = getStateMap(cacheKey, ContentType.JOURNAL_DREAM, false);
        final Map<Integer, JournalState> reflectionStateMap = getStateMap(cacheKey, ContentType.JOURNAL_REFLECTION, false);
        final Map<Integer, String> diaryLifecycleMap = getLifecycleMap(cacheKey, ContentType.JOURNAL_DIARY, false);
        final Map<Integer, String> dreamLifecycleMap = getLifecycleMap(cacheKey, ContentType.JOURNAL_DREAM, false);
        final Map<Integer, String> reflectionLifecycleMap = getLifecycleMap(cacheKey, ContentType.JOURNAL_REFLECTION, false);

        final List<JournalDayDto> listDto = List.of(journalDay);
        JournalDayViewHelper.applyStates(listDto, chapterMap, diaryMap, dreamMap, reflectionStateMap, diaryLifecycleMap, dreamLifecycleMap, reflectionLifecycleMap);
    }

    /**
     * 캐시에 저장된 chapter/diary/dream/reflection state와 lifecycle 값을 일자 트리에 반영한다.
     *
     * @param listDto 일자 목록 DTO
     * @param chapterMap chapter id 기준 state 맵
     * @param diaryMap diary id 기준 state 맵
     */
    public static void applyStates(
        final List<JournalDayDto> listDto,
        final Map<Integer, JournalState> chapterMap,
        final Map<Integer, JournalState> diaryMap,
        final Map<Integer, JournalState> dreamMap,
        final Map<Integer, JournalState> reflectionStateMap,
        final Map<Integer, String> diaryLifecycleMap,
        final Map<Integer, String> dreamLifecycleMap,
        final Map<Integer, String> reflectionLifecycleMap
    ) {
        for (JournalDayDto day : listDto) {
            JournalChapterViewHelper.applyStates(day.getJournalChapterList(), chapterMap, diaryMap, diaryLifecycleMap, reflectionStateMap, reflectionLifecycleMap);
            applyDreamStatesForDay(day, dreamMap, dreamLifecycleMap, reflectionStateMap, reflectionLifecycleMap);
        }
    }

    /**
     * 검색 조건을 고려하여 캐시의 state와 lifecycle 값을 일자 트리에 반영한다.
     *
     * @param listDto 일자 목록 DTO
     * @param chapterMap chapter id 기준 state 맵
     * @param diaryMap diary id 기준 state 맵
     * @param searchParam 일자 검색 조건
     */
    public static void applyStates(
        final List<JournalDayDto> listDto,
        final Map<Integer, JournalState> chapterMap,
        final Map<Integer, JournalState> diaryMap,
        final Map<Integer, JournalState> dreamMap,
        final Map<Integer, JournalState> reflectionStateMap,
        final Map<Integer, String> diaryLifecycleMap,
        final Map<Integer, String> dreamLifecycleMap,
        final Map<Integer, String> reflectionLifecycleMap,
        final JournalDaySearchParam searchParam
    ) {
        for (JournalDayDto day : listDto) {

            if (searchParam.isShowDiaries()) {
                JournalChapterViewHelper.applyStates(day.getJournalChapterList(), chapterMap, diaryMap, diaryLifecycleMap, reflectionStateMap, reflectionLifecycleMap);
            }

            if (searchParam.isShowDreams()) {
                applyDreamStatesForDay(day, dreamMap, dreamLifecycleMap, reflectionStateMap, reflectionLifecycleMap);
            }
        }
    }

    private static void applyDreamStatesForDay(
            final JournalDayDto day,
            final Map<Integer, JournalState> dreamMap,
            final Map<Integer, String> dreamLifecycleMap,
            final Map<Integer, JournalState> reflectionStateMap,
            final Map<Integer, String> reflectionLifecycleMap
    ) {
        if (day == null || CollectionUtils.isEmpty(day.getJournalDreamSectionList())) return;
        for (final JournalDreamSectionDto section : day.getJournalDreamSectionList()) {
            if (section == null) continue;
            JournalEntryStateViewHelper.applyDreamStates(
                    section.getEntries(),
                    dreamMap,
                    dreamLifecycleMap,
                    reflectionStateMap,
                    reflectionLifecycleMap
            );
        }
    }

    /**
     * chapter가 접힌 상태일 때 하위 일기 축(DIARY + Reflection) 태그를 모아 chapter 요약 태그 목록에 주입한다.
     *
     * @param listDto 일자 목록 DTO
     */
    public static void applyChapterTagSummary(final List<JournalDayDto> listDto, final JournalDaySearchParam searchParam) {
        if (CollectionUtils.isEmpty(listDto)) return;
        if (!searchParam.isShowDiaries()) return;

        for (final JournalDayDto day : listDto) {
            if (CollectionUtils.isEmpty(day.getJournalChapterList())) continue;

            for (final JournalChapterDto chapter : day.getJournalChapterList()) {
                /* 일기 축: DIARY 엔트리 + 같은 챕터의 Reflection(독립 포함) 태그를 합친다. */
                final List<JournalEntryDto> diaryAxisEntries = new ArrayList<>();
                diaryAxisEntries.addAll(JournalEntryViewProjectionHelper.getDiaryEntries(chapter));
                diaryAxisEntries.addAll(JournalEntryViewProjectionHelper.getEntriesByType(chapter, ContentType.JOURNAL_REFLECTION));
                if (CollectionUtils.isEmpty(diaryAxisEntries)) continue;

                final Map<Integer, TagContentDto> tagMap = new LinkedHashMap<>();
                for (final JournalEntryDto entry : diaryAxisEntries) {
                    if (entry.getTag() == null) continue;
                    final List<TagContentDto> tagList = entry.getTag().getList();
                    if (CollectionUtils.isEmpty(tagList)) continue;

                    for (final TagContentDto tag : tagList) {
                        tagMap.putIfAbsent(tag.getTagId(), tag);
                    }
                }

                // 자체 태그 저장을 없앤 뒤(엔티티 TagEmbed 제거) DTO.tag 가 매핑되지 않아 null 일 수 있다.
                // 집계 컨테이너를 여기서 보장한다.
                if (chapter.getTag() == null) chapter.setTag(new TagCmpstn());
                chapter.getTag().setList(new ArrayList<>(tagMap.values()));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<Integer, JournalState> getStateMap(
            final Object cacheKey,
            final ContentType contentType,
            final boolean weekly
    ) {
        final String cacheName = weekly
                ? JournalStateCacheRegistry.weeklyMapCacheName(contentType)
                : JournalStateCacheRegistry.monthlyMapCacheName(contentType);
        return Optional.ofNullable((Map<Integer, JournalState>) EhCacheUtils.getObjectFromCache(cacheName, cacheKey))
                .orElse(Collections.emptyMap());
    }

    /**
     * 월간 또는 주간 캐시에서 lifecycle 보조 맵을 읽는다.
     *
     * @param cacheKey 사용할 날짜 기준 캐시 키
     * @param contentType lifecycle을 지원하는 컨텐츠 타입
     * @param weekly 주간 캐시 namespace 사용 여부
     * @return 컨텐츠 ID 기준 lifecycle 키 맵
     */
    @SuppressWarnings("unchecked")
    private static Map<Integer, String> getLifecycleMap(
            final Object cacheKey,
            final ContentType contentType,
            final boolean weekly
    ) {
        final String cacheName = weekly
                ? JournalLifecycleCacheRegistry.weeklyMapCacheName(contentType)
                : JournalLifecycleCacheRegistry.monthlyMapCacheName(contentType);
        return Optional.ofNullable((Map<Integer, String>) EhCacheUtils.getObjectFromCache(cacheName, cacheKey))
                .orElse(Collections.emptyMap());
    }
}
