package io.nicheblog.dreamdiary.feature.journal.day.service.helper;

import io.nicheblog.dreamdiary.feature.journal.chapter.model.JournalChapterCtgrHintDto;
import io.nicheblog.dreamdiary.feature.journal.chapter.model.JournalChapterDto;
import io.nicheblog.dreamdiary.feature.journal.day.model.JournalDayDto;
import io.nicheblog.dreamdiary.feature.journal.day.model.JournalDaySearchParam;
import io.nicheblog.dreamdiary.feature.journal.diary.model.JournalDiaryDto;
import io.nicheblog.dreamdiary.feature.journal.dream.model.JournalDreamDto;
import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * JournalDayFilterHelper
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

        final boolean filterDiaries = showDiaries && StringUtils.isNotEmpty(diaryKeyword);
        final boolean filterDreams = showDreams && StringUtils.isNotEmpty(dreamKeyword);

        final List<String> chapterCtgrCds = normalizeChapterCtgrCds(searchParam.getChapterCtgrCds());
        final boolean filterChapterCtgr = showDiaries && CollectionUtils.isNotEmpty(chapterCtgrCds);

        if (!filterDiaries && !filterDreams && !filterChapterCtgr) return listDto;

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
            if (filterChapterCtgr || filterDiaries) {
                filteredEntries = new ArrayList<>();
                final List<JournalChapterDto> chapterList = day.getJournalChapterList();
                if (CollectionUtils.isNotEmpty(chapterList)) {
                    for (final JournalChapterDto chapter : chapterList) {
                        if (filterChapterCtgr && !matchesChapterCtgr(chapter, hasNoneCategory, ctgrSet)) continue;

                        if (filterDiaries) {
                            final List<JournalDiaryDto> diaryList = chapter.getJournalDiaryList();
                            if (CollectionUtils.isEmpty(diaryList)) continue;
                            final List<JournalDiaryDto> filteredDiaries = new ArrayList<>();
                            for (final JournalDiaryDto diary : diaryList) {
                                if (containsKeyword(diary.getCn(), diaryKeyword)) filteredDiaries.add(diary);
                            }
                            if (filteredDiaries.isEmpty()) continue;
                            filteredEntries.add(chapter.toBuilder().journalDiaryList(filteredDiaries).build());
                        } else {
                            filteredEntries.add(chapter);
                        }
                    }
                }
            }

            List<JournalDreamDto> filteredDreams = day.getJournalDreamList();
            List<JournalDreamDto> filteredElseDreams = day.getJournalElseDreamList();
            if (filterDreams) {
                filteredDreams = filterDreamList(day.getJournalDreamList(), dreamKeyword);
                filteredElseDreams = filterDreamList(day.getJournalElseDreamList(), dreamKeyword);
            }

            final boolean hasHiddenChapterCtgr = !filterDiaries && CollectionUtils.isNotEmpty(hiddenChapterCtgrList);

            // 챕터가 원래부터 없는 날은 챕터 카테고리 필터로 제외하지 않음
            if ((filterDiaries || (filterChapterCtgr && hadChapters))
                    && CollectionUtils.isEmpty(filteredEntries)
                    && !hasHiddenChapterCtgr) continue;
            if (filterDreams && CollectionUtils.isEmpty(filteredDreams) && CollectionUtils.isEmpty(filteredElseDreams)) continue;

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
        final String ctgrCd = StringUtils.trimToEmpty(chapter.getCtgrCd());
        if (ctgrCd.isEmpty()) return true;
        return ctgrSet.contains(ctgrCd);
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

            final String ctgrCd = StringUtils.trimToEmpty(chapter.getCtgrCd());
            final String ctgrKey = StringUtils.isNotEmpty(ctgrCd) ? ctgrCd : CHAPTER_CTGR_NONE;
            if (hiddenMap.containsKey(ctgrKey)) continue;

            hiddenMap.put(ctgrKey, JournalChapterCtgrHintDto.builder()
                    .ctgrCd(ctgrCd)
                    .ctgrNm(StringUtils.defaultIfBlank(chapter.getCtgrNm(), ctgrCd.isEmpty() ? "미분류" : ctgrCd))
                    .build());
        }

        return new ArrayList<>(hiddenMap.values());
    }

    private static boolean containsKeyword(final String value, final String keyword) {
        if (StringUtils.isEmpty(keyword)) return true;
        if (StringUtils.isEmpty(value)) return false;
        return value.toLowerCase().contains(keyword);
    }

    private static List<JournalDreamDto> filterDreamList(final List<JournalDreamDto> dreamList, final String keyword) {
        if (CollectionUtils.isEmpty(dreamList)) return new ArrayList<>();
        final List<JournalDreamDto> filtered = new ArrayList<>();
        for (final JournalDreamDto dream : dreamList) {
            if (containsKeyword(dream.getCn(), keyword)) filtered.add(dream);
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

