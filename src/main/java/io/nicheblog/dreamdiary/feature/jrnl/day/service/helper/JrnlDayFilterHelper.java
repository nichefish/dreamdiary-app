package io.nicheblog.dreamdiary.feature.jrnl.day.service.helper;

import io.nicheblog.dreamdiary.feature.jrnl.day.model.JrnlDayDto;
import io.nicheblog.dreamdiary.feature.jrnl.day.model.JrnlDaySearchParam;
import io.nicheblog.dreamdiary.feature.jrnl.diary.model.JrnlDiaryDto;
import io.nicheblog.dreamdiary.feature.jrnl.dream.model.JrnlDreamDto;
import io.nicheblog.dreamdiary.feature.jrnl.chapter.model.JrnlChapterDto;
import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * JrnlDayFilterHelper
 *
 * @author nichefish
 */
@UtilityClass
public final class JrnlDayFilterHelper {

    private static final String CHAPTER_CTGR_NONE = "__NONE__";

    public static List<JrnlDayDto> filterInMemory(final List<JrnlDayDto> listDto, final JrnlDaySearchParam searchParam) {
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

        final List<JrnlDayDto> result = new ArrayList<>();
        for (final JrnlDayDto day : listDto) {
            List<JrnlChapterDto> filteredEntries = day.getJrnlChapterList();
            final boolean hadChapters = CollectionUtils.isNotEmpty(day.getJrnlChapterList());
            if (filterChapterCtgr || filterDiaries) {
                filteredEntries = new ArrayList<>();
                final List<JrnlChapterDto> chapterList = day.getJrnlChapterList();
                if (CollectionUtils.isNotEmpty(chapterList)) {
                    for (final JrnlChapterDto chapter : chapterList) {
                        if (filterChapterCtgr && !matchesChapterCtgr(chapter, hasNoneCategory, ctgrSet)) continue;

                        if (filterDiaries) {
                            final List<JrnlDiaryDto> diaryList = chapter.getJrnlDiaryList();
                            if (CollectionUtils.isEmpty(diaryList)) continue;
                            final List<JrnlDiaryDto> filteredDiaries = new ArrayList<>();
                            for (final JrnlDiaryDto diary : diaryList) {
                                if (containsKeyword(diary.getCn(), diaryKeyword)) filteredDiaries.add(diary);
                            }
                            if (filteredDiaries.isEmpty()) continue;
                            filteredEntries.add(chapter.toBuilder().jrnlDiaryList(filteredDiaries).build());
                        } else {
                            filteredEntries.add(chapter);
                        }
                    }
                }
            }

            List<JrnlDreamDto> filteredDreams = day.getJrnlDreamList();
            List<JrnlDreamDto> filteredElseDreams = day.getJrnlElseDreamList();
            if (filterDreams) {
                filteredDreams = filterDreamList(day.getJrnlDreamList(), dreamKeyword);
                filteredElseDreams = filterDreamList(day.getJrnlElseDreamList(), dreamKeyword);
            }

            // 챕터가 원래부터 없는 날은 챕터 카테고리 필터로 제외하지 않음
            if ((filterDiaries || (filterChapterCtgr && hadChapters)) && CollectionUtils.isEmpty(filteredEntries)) continue;
            if (filterDreams && CollectionUtils.isEmpty(filteredDreams) && CollectionUtils.isEmpty(filteredElseDreams)) continue;

            final JrnlDayDto nextDay = day.toBuilder()
                    .jrnlChapterList(filteredEntries)
                    .jrnlDreamList(filteredDreams)
                    .jrnlElseDreamList(filteredElseDreams)
                    .build();
            result.add(nextDay);
        }

        return result;
    }

    private static boolean matchesChapterCtgr(final JrnlChapterDto chapter, final boolean hasNoneCategory, final Set<String> ctgrSet) {
        if (chapter == null) return false;
        final String ctgrCd = StringUtils.trimToEmpty(chapter.getCtgrCd());
        if (ctgrCd.isEmpty()) return hasNoneCategory;
        return ctgrSet.contains(ctgrCd);
    }

    private static boolean containsKeyword(final String value, final String keyword) {
        if (StringUtils.isEmpty(keyword)) return true;
        if (StringUtils.isEmpty(value)) return false;
        return value.toLowerCase().contains(keyword);
    }

    private static List<JrnlDreamDto> filterDreamList(final List<JrnlDreamDto> dreamList, final String keyword) {
        if (CollectionUtils.isEmpty(dreamList)) return new ArrayList<>();
        final List<JrnlDreamDto> filtered = new ArrayList<>();
        for (final JrnlDreamDto dream : dreamList) {
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
