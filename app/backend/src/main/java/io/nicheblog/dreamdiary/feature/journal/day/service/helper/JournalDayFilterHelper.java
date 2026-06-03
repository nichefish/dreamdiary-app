package io.nicheblog.dreamdiary.feature.journal.day.service.helper;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.journal.chapter.model.JournalChapterCtgrHintDto;
import io.nicheblog.dreamdiary.feature.journal.chapter.model.JournalChapterDto;
import io.nicheblog.dreamdiary.feature.journal.day.model.JournalDayDto;
import io.nicheblog.dreamdiary.feature.journal.day.model.JournalDaySearchParam;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryDto;
import io.nicheblog.dreamdiary.feature.journal.entry.service.helper.JournalEntryViewProjectionHelper;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * JournalDayFilterHelper
 * 저널 일자 목록을 검색 조건에 맞게 메모리 상에서 필터링한다.
 *
 * @author nichefish
 */
@UtilityClass
public final class JournalDayFilterHelper {

    private static final String CHAPTER_CTGR_NONE = "__NONE__";
    public static List<JournalDayDto> filterInMemory(final List<JournalDayDto> listDto, final JournalDaySearchParam searchParam) {
        if (CollectionUtils.isEmpty(listDto) || searchParam == null) return listDto;

        final boolean showDiaries = searchParam.isShowDiaries();
        final boolean showDreams = searchParam.isShowDreams();

        final String diaryKeyword = StringUtils.defaultString(searchParam.getDiaryKeyword()).trim().toLowerCase();
        final String dreamKeyword = StringUtils.defaultString(searchParam.getDreamKeyword()).trim().toLowerCase();
        final String diaryLifecycleKey = StringUtils.defaultString(searchParam.getDiaryLifecycleKey()).trim();
        final String dreamLifecycleKey = StringUtils.defaultString(searchParam.getDreamLifecycleKey()).trim();

        final boolean filterDiaries = showDiaries && StringUtils.isNotEmpty(diaryKeyword);
        final boolean filterDreams = showDreams && StringUtils.isNotEmpty(dreamKeyword);
        final boolean filterDiaryLifecycle = showDiaries && StringUtils.isNotEmpty(diaryLifecycleKey);
        final boolean filterDreamLifecycle = showDreams && StringUtils.isNotEmpty(dreamLifecycleKey);

        final List<String> chapterCtgrCds = normalizeChapterCtgrCds(searchParam.getChapterCtgrCds());
        final boolean filterChapterCtgr = showDiaries && CollectionUtils.isNotEmpty(chapterCtgrCds);

        if (!filterDiaries && !filterDreams && !filterDiaryLifecycle && !filterDreamLifecycle && !filterChapterCtgr) return listDto;

        final boolean hasNoneCategory = filterChapterCtgr && chapterCtgrCds.contains(CHAPTER_CTGR_NONE);
        final Set<String> ctgrSet = new HashSet<>();
        if (filterChapterCtgr) {
            for (final String ctgr : chapterCtgrCds) {
                if (StringUtils.isBlank(ctgr) || CHAPTER_CTGR_NONE.equals(ctgr)) continue;
                ctgrSet.add(ctgr.trim());
            }
        }

        final List<JournalDayDto> result = new ArrayList<>();
        for (final JournalDayDto day : listDto) {
            List<JournalChapterDto> filteredEntries = day.getJournalChapterList();
            final boolean hadChapters = CollectionUtils.isNotEmpty(day.getJournalChapterList());
            final List<JournalChapterCtgrHintDto> hiddenChapterCtgrList = filterChapterCtgr
                    ? collectHiddenChapterCtgrList(day.getJournalChapterList(), hasNoneCategory, ctgrSet)
                    : List.of();
            if (filterChapterCtgr || filterDiaries || filterDiaryLifecycle) {
                filteredEntries = new ArrayList<>();
                final List<JournalChapterDto> chapterList = day.getJournalChapterList();
                if (CollectionUtils.isNotEmpty(chapterList)) {
                    for (final JournalChapterDto chapter : chapterList) {
                        if (filterChapterCtgr && !matchesChapterCtgr(chapter, hasNoneCategory, ctgrSet)) continue;

                        if (filterDiaries || filterDiaryLifecycle) {
                            final List<JournalEntryDto> diaryList = JournalEntryViewProjectionHelper.getDiaryEntries(chapter);
                            if (CollectionUtils.isEmpty(diaryList)) continue;
                            final List<JournalEntryDto> filteredDiaries = new ArrayList<>();
                            for (final JournalEntryDto diary : diaryList) {
                                if (matchesEntryFilters(diary, diaryKeyword, diaryLifecycleKey)) filteredDiaries.add(diary);
                            }
                            if (filteredDiaries.isEmpty()) continue;

                            final JournalChapterDto filteredChapter = chapter.toBuilder().build();
                            JournalEntryViewProjectionHelper.applyChapterEntries(
                                    filteredChapter,
                                    JournalEntryViewProjectionHelper.replaceChapterEntries(
                                            chapter,
                                            ContentType.JOURNAL_DIARY,
                                            filteredDiaries
                                    )
                            );
                            filteredEntries.add(filteredChapter);
                        } else {
                            filteredEntries.add(chapter);
                        }
                    }
                }
            }

            List<JournalEntryDto> filteredDreams = day.getJournalDreamList();
            List<JournalEntryDto> filteredElseDreams = day.getJournalElseDreamList();
            if (filterDreams || filterDreamLifecycle) {
                filteredDreams = filterDreamList(day.getJournalDreamList(), dreamKeyword, dreamLifecycleKey);
                filteredElseDreams = filterDreamList(day.getJournalElseDreamList(), dreamKeyword, dreamLifecycleKey);
            }

            final boolean hasHiddenChapterCtgr = !filterDiaries && !filterDiaryLifecycle && CollectionUtils.isNotEmpty(hiddenChapterCtgrList);

            // 챕터가 원래부터 없던 날은 챕터 카테고리 필터로 제외하지 않는다.
            if ((filterDiaries || filterDiaryLifecycle || (filterChapterCtgr && hadChapters))
                    && CollectionUtils.isEmpty(filteredEntries)
                    && !hasHiddenChapterCtgr) continue;
            if ((filterDreams || filterDreamLifecycle) && CollectionUtils.isEmpty(filteredDreams) && CollectionUtils.isEmpty(filteredElseDreams)) continue;

            final JournalDayDto nextDay = day.toBuilder()
                    .journalChapterList(filteredEntries)
                    .hiddenChapterCtgrList(hiddenChapterCtgrList)
                    .journalDreamList(filteredDreams)
                    .journalElseDreamList(filteredElseDreams)
                    .build();
            result.add(nextDay);
        }

        return result;
    }

    private static boolean matchesChapterCtgr(final JournalChapterDto chapter, final boolean hasNoneCategory, final Set<String> ctgrSet) {
        if (chapter == null) return false;
        final String categoryCode = StringUtils.trimToEmpty(chapter.getCategoryCode());
        if (categoryCode.isEmpty()) return true;
        return ctgrSet.contains(categoryCode);
    }

    private static List<JournalChapterCtgrHintDto> collectHiddenChapterCtgrList(
            final List<JournalChapterDto> chapterList,
            final boolean hasNoneCategory,
            final Set<String> ctgrSet
    ) {
        if (CollectionUtils.isEmpty(chapterList)) return List.of();

        final Map<String, JournalChapterCtgrHintDto> hiddenMap = new LinkedHashMap<>();
        for (final JournalChapterDto chapter : chapterList) {
            if (chapter == null || matchesChapterCtgr(chapter, hasNoneCategory, ctgrSet)) continue;

            final String categoryCode = StringUtils.trimToEmpty(chapter.getCategoryCode());
            final String ctgrKey = StringUtils.isNotEmpty(categoryCode) ? categoryCode : CHAPTER_CTGR_NONE;
            if (hiddenMap.containsKey(ctgrKey)) continue;

            hiddenMap.put(ctgrKey, JournalChapterCtgrHintDto.builder()
                    .categoryCode(categoryCode)
                    .categoryName(StringUtils.defaultIfBlank(chapter.getCategoryName(), categoryCode.isEmpty() ? MessageUtils.getMessage("txt.ctgr.none", null) : categoryCode))
                    .build());
        }

        return new ArrayList<>(hiddenMap.values());
    }

    private static boolean containsKeyword(final String value, final String keyword) {
        if (StringUtils.isEmpty(keyword)) return true;
        if (StringUtils.isEmpty(value)) return false;
        return value.toLowerCase().contains(keyword);
    }

    private static boolean matchesEntryFilters(final JournalEntryDto entry, final String keyword, final String lifecycleKey) {
        if (entry == null) return false;
        return containsKeyword(entry.getContent(), keyword) && matchesLifecycle(entry, lifecycleKey);
    }

    private static boolean matchesLifecycle(final JournalEntryDto entry, final String lifecycleKey) {
        if (StringUtils.isEmpty(lifecycleKey)) return true;
        if (entry == null || entry.getLifecycle() == null) return "OPEN".equals(lifecycleKey);
        return lifecycleKey.equals(entry.getLifecycle().getLifecycleKey());
    }

    private static List<JournalEntryDto> filterDreamList(final List<JournalEntryDto> dreamList, final String keyword, final String lifecycleKey) {
        if (CollectionUtils.isEmpty(dreamList)) return new ArrayList<>();
        final List<JournalEntryDto> filtered = new ArrayList<>();
        for (final JournalEntryDto dream : dreamList) {
            if (matchesEntryFilters(dream, keyword, lifecycleKey)) filtered.add(dream);
        }
        return filtered;
    }

    private static List<String> normalizeChapterCtgrCds(final List<String> chapterCtgrCds) {
        if (CollectionUtils.isEmpty(chapterCtgrCds)) return chapterCtgrCds;
        if (chapterCtgrCds.size() != 1) return chapterCtgrCds;
        final String raw = chapterCtgrCds.get(0);
        if (StringUtils.isBlank(raw) || !raw.contains(",")) return chapterCtgrCds;

        final String[] parts = StringUtils.split(raw, ",");
        final List<String> normalized = new ArrayList<>();
        if (parts != null) {
            for (final String part : parts) {
                final String trimmed = StringUtils.trimToNull(part);
                if (trimmed != null) normalized.add(trimmed);
            }
        }
        return normalized;
    }
}
