package io.nicheblog.dreamdiary.feature.journal.day.service.helper;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.tag.model.TagContentDto;
import io.nicheblog.dreamdiary.feature.journal._shared.state.JournalState;
import io.nicheblog.dreamdiary.feature.journal._shared.state.JournalStateCacheRegistry;
import io.nicheblog.dreamdiary.feature.journal.chapter.model.JournalChapterDto;
import io.nicheblog.dreamdiary.feature.journal.chapter.service.helper.JournaaChapterViewHelper;
import io.nicheblog.dreamdiary.feature.journal.day.model.JournalDayDto;
import io.nicheblog.dreamdiary.feature.journal.day.model.JournalDaySearchParam;
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
 *
 * @author nichefish
 */
@UtilityClass
public final class JournalDayViewHelper {

    /**
     * ?곹깭state merge
     *
     * @param listDto ????쇱옄 紐⑸줉
     * @param searchParam 寃???뚮씪誘명꽣
     */
    @SuppressWarnings("unchecked")
    public static void mergeStates(final String username, final List<JournalDayDto> listDto, final JournalDaySearchParam searchParam) {
        if (CollectionUtils.isEmpty(listDto) || searchParam == null) return;

        final Object cacheKey = new SimpleKey(username, searchParam.getYy(), searchParam.getMnth());

        final Map<Integer, JournalState> chapterMap = getStateMap(cacheKey, ContentType.JOURNAL_CHAPTER, false);
        final Map<Integer, JournalState> diaryMap = getStateMap(cacheKey, ContentType.JOURNAL_DIARY, false);
        final Map<Integer, JournalState> dreamMap = getStateMap(cacheKey, ContentType.JOURNAL_DREAM, false);
        final Map<Integer, JournalState> interpretationMap = getStateMap(cacheKey, ContentType.JOURNAL_INTERPRETATION, false);

        JournalDayViewHelper.applyStates(listDto, chapterMap, diaryMap, dreamMap, interpretationMap, searchParam);
    }

    /**
     * 二쇨컙 議고쉶??state merge
     *
     * @param listDto ?쇱옄 紐⑸줉
     * @param searchParam 寃???뚮씪誘명꽣
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
        final Map<Integer, JournalState> interpretationMap = getStateMap(cacheKey, ContentType.JOURNAL_INTERPRETATION, true);

        JournalDayViewHelper.applyStates(listDto, chapterMap, diaryMap, dreamMap, interpretationMap, searchParam);
    }

    /**
     * ?곹깭state merge
     *
     * @param journalDay ????쇱옄
     */
    @SuppressWarnings("unchecked")
    public static void mergeStates(final String username, final JournalDayDto journalDay) {
        if (journalDay == null) return;

        final Object cacheKey = new SimpleKey(username, journalDay.getYy(), journalDay.getMnth());

        final Map<Integer, JournalState> chapterMap = getStateMap(cacheKey, ContentType.JOURNAL_CHAPTER, false);
        final Map<Integer, JournalState> diaryMap = getStateMap(cacheKey, ContentType.JOURNAL_DIARY, false);
        final Map<Integer, JournalState> dreamMap = getStateMap(cacheKey, ContentType.JOURNAL_DREAM, false);
        final Map<Integer, JournalState> interpretationMap = getStateMap(cacheKey, ContentType.JOURNAL_INTERPRETATION, false);

        final List<JournalDayDto> listDto = List.of(journalDay);
        JournalDayViewHelper.applyStates(listDto, chapterMap, diaryMap, dreamMap, interpretationMap);
    }

    /**
     * 罹먯떆????λ맂 ?곹깭 留?chapter/diary/dream/interpretation)??湲곗??쇰줈 議고쉶??{@link JournalDayDto} ?몃━ 援ъ“???곹깭瑜?諛섏쁺?쒕떎.
     *
     * @param listDto 議고쉶??????쇱옄 紐⑸줉 DTO
     * @param chapterMap chapter id ??{@link JournalState} 留?     * @param diaryMap diary id ??{@link JournalState} 留?
     */
    public static void applyStates(
        final List<JournalDayDto> listDto,
        final Map<Integer, JournalState> chapterMap,
        final Map<Integer, JournalState> diaryMap,
        final Map<Integer, JournalState> dreamMap,
        final Map<Integer, JournalState> interpretationMap
    ) {
        for (JournalDayDto day : listDto) {
            JournaaChapterViewHelper.applyStates(day.getJournalChapterList(), chapterMap, diaryMap, interpretationMap);
            JournalEntryStateViewHelper.applyDreamStates(day.getJournalDreamList(), dreamMap, interpretationMap);
            JournalEntryStateViewHelper.applyDreamStates(day.getJournalElseDreamList(), dreamMap, interpretationMap);
        }
    }

    /**
     * 罹먯떆????λ맂 ?곹깭 留?chapter/diary/dream/interpretation)??湲곗??쇰줈 議고쉶??{@link JournalDayDto} ?몃━ 援ъ“???곹깭瑜?諛섏쁺?쒕떎.
     *
     * @param listDto 議고쉶??????쇱옄 紐⑸줉 DTO
     * @param chapterMap chapter id ??{@link JournalState} 留?     * @param diaryMap diary id ??{@link JournalState} 留?
     * @param searchParam JournalDaySearchParam
     */
    public static void applyStates(
        final List<JournalDayDto> listDto,
        final Map<Integer, JournalState> chapterMap,
        final Map<Integer, JournalState> diaryMap,
        final Map<Integer, JournalState> dreamMap,
        final Map<Integer, JournalState> interpretationMap,
        final JournalDaySearchParam searchParam
    ) {
        for (JournalDayDto day : listDto) {

            if (searchParam.isShowDiaries()) {
                JournaaChapterViewHelper.applyStates(day.getJournalChapterList(), chapterMap, diaryMap, interpretationMap);
            }

            if (searchParam.isShowDreams()) {
                JournalEntryStateViewHelper.applyDreamStates(day.getJournalDreamList(), dreamMap, interpretationMap);
                JournalEntryStateViewHelper.applyDreamStates(day.getJournalElseDreamList(), dreamMap, interpretationMap);
            }
        }
    }

    /**
     * Entry媛 collapsed ?곹깭??寃쎌슦, ?섏쐞 {@link JournalEntryDto} ?ㅼ뿉 ?ы븿???쒓렇瑜??섏쭛?섏뿬 以묐났 ?쒓굅??"?붿빟 ?쒓렇 紐⑸줉"??Entry??二쇱엯?쒕떎.
     *
     * @param listDto 議고쉶??????쇱옄 紐⑸줉 DTO
     */
    public static void applyChapterTagSummary(final List<JournalDayDto> listDto, final JournalDaySearchParam searchParam) {
        if (CollectionUtils.isEmpty(listDto)) return;
        if (!searchParam.isShowDiaries()) return;

        for (final JournalDayDto day : listDto) {
            if (CollectionUtils.isEmpty(day.getJournalChapterList())) continue;

            for (final JournalChapterDto chapter : day.getJournalChapterList()) {
                final List<JournalEntryDto> diaryEntries = JournalEntryViewProjectionHelper.getDiaryEntries(chapter);
                if (CollectionUtils.isEmpty(diaryEntries)) continue;

                final Map<Integer, TagContentDto> tagMap = new LinkedHashMap<>();
                for (final JournalEntryDto diary : diaryEntries) {
                    final List<TagContentDto> tagList = diary.getTag().getList();
                    if (CollectionUtils.isEmpty(tagList)) continue;

                    for (final TagContentDto tag : tagList) {
                        tagMap.putIfAbsent(tag.getTagId(), tag);
                    }
                }

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
}
