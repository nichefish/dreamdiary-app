package io.nicheblog.dreamdiary.feature.journal.day.service.helper;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.journal.chapter.model.JournalChapterDto;
import io.nicheblog.dreamdiary.feature.journal.chapter.model.JournalChapterPrefixHintDto;
import io.nicheblog.dreamdiary.feature.journal.day.model.JournalDayDto;
import io.nicheblog.dreamdiary.feature.journal.day.model.JournalDaySearchParam;
import io.nicheblog.dreamdiary.feature.journal.day.model.JournalDreamSectionDto;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryDto;
import io.nicheblog.dreamdiary.feature.journal.entry.service.helper.JournalEntryViewProjectionHelper;
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

        final List<Integer> chapterPrefixIds = searchParam.getChapterPrefixIds();
        final boolean filterChapterPrefix = showDiaries && CollectionUtils.isNotEmpty(chapterPrefixIds);

        if (!filterDiaries && !filterDreams && !filterDiaryLifecycle && !filterDreamLifecycle && !filterChapterPrefix) return listDto;

        final Set<Integer> prefixIdSet = filterChapterPrefix
                ? new HashSet<>(chapterPrefixIds)
                : Set.of();

        final List<JournalDayDto> result = new ArrayList<>();
        for (final JournalDayDto day : listDto) {
            List<JournalChapterDto> filteredEntries = day.getJournalChapterList();
            final boolean hadChapters = CollectionUtils.isNotEmpty(day.getJournalChapterList());
            final List<JournalChapterPrefixHintDto> hiddenChapterPrefixList = filterChapterPrefix
                    ? collectHiddenChapterPrefixList(day.getJournalChapterList(), prefixIdSet)
                    : List.of();
            if (filterChapterPrefix || filterDiaries || filterDiaryLifecycle) {
                filteredEntries = new ArrayList<>();
                final List<JournalChapterDto> chapterList = day.getJournalChapterList();
                if (CollectionUtils.isNotEmpty(chapterList)) {
                    for (final JournalChapterDto chapter : chapterList) {
                        if (filterChapterPrefix && !matchesChapterPrefix(chapter, prefixIdSet)) continue;

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

            List<JournalDreamSectionDto> filteredDreamSections = day.getJournalDreamSectionList();
            if (filterDreams || filterDreamLifecycle) {
                filteredDreamSections = filterDreamSections(day.getJournalDreamSectionList(), dreamKeyword, dreamLifecycleKey);
            }

            final boolean hasHiddenChapterPrefix = !filterDiaries && !filterDiaryLifecycle && CollectionUtils.isNotEmpty(hiddenChapterPrefixList);

            // 챕터가 원래부터 없던 날은 챕터 Prefix 필터로 제외하지 않는다.
            if ((filterDiaries || filterDiaryLifecycle || (filterChapterPrefix && hadChapters))
                    && CollectionUtils.isEmpty(filteredEntries)
                    && !hasHiddenChapterPrefix) continue;
            if ((filterDreams || filterDreamLifecycle) && CollectionUtils.isEmpty(filteredDreamSections)) continue;

            final JournalDayDto nextDay = day.toBuilder()
                    .journalChapterList(filteredEntries)
                    .hiddenChapterPrefixList(hiddenChapterPrefixList)
                    .journalDreamSectionList(filteredDreamSections)
                    .build();
            result.add(nextDay);
        }

        return result;
    }

    /**
     * 시스템 요약과 Prefix 미선택 챕터는 Prefix 필터 계약상 항상 유지한다.
     */
    private static boolean matchesChapterPrefix(final JournalChapterDto chapter, final Set<Integer> prefixIdSet) {
        if (chapter == null) return false;
        if ("Y".equals(chapter.getSummaryYn()) || chapter.getPrefixId() == null) return true;
        return prefixIdSet.contains(chapter.getPrefixId());
    }

    private static List<JournalChapterPrefixHintDto> collectHiddenChapterPrefixList(
            final List<JournalChapterDto> chapterList,
            final Set<Integer> prefixIdSet
    ) {
        if (CollectionUtils.isEmpty(chapterList)) return List.of();

        final Map<Integer, JournalChapterPrefixHintDto> hiddenMap = new LinkedHashMap<>();
        for (final JournalChapterDto chapter : chapterList) {
            if (chapter == null || chapter.getPrefix() == null
                    || matchesChapterPrefix(chapter, prefixIdSet)) continue;
            final Integer prefixId = chapter.getPrefix().getId();
            if (prefixId == null || hiddenMap.containsKey(prefixId)) continue;

            hiddenMap.put(prefixId, JournalChapterPrefixHintDto.builder()
                    .prefixId(prefixId)
                    .prefixName(chapter.getPrefix().getName())
                    .prefixColor(chapter.getPrefix().getColor())
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

    private static List<JournalDreamSectionDto> filterDreamSections(
            final List<JournalDreamSectionDto> sections,
            final String keyword,
            final String lifecycleKey
    ) {
        if (CollectionUtils.isEmpty(sections)) return null;
        final List<JournalDreamSectionDto> filtered = new ArrayList<>();
        for (final JournalDreamSectionDto section : sections) {
            if (section == null) continue;
            final List<JournalEntryDto> filteredEntries = filterDreamList(section.getEntries(), keyword, lifecycleKey);
            if (CollectionUtils.isEmpty(filteredEntries)) continue;
            filtered.add(section.toBuilder().entries(filteredEntries).build());
        }
        return CollectionUtils.isEmpty(filtered) ? null : filtered;
    }

}
