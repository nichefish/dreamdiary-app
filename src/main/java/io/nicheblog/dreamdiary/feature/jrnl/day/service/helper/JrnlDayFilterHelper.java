package io.nicheblog.dreamdiary.feature.jrnl.day.service.helper;

import io.nicheblog.dreamdiary.feature.jrnl.day.model.JrnlDayDto;
import io.nicheblog.dreamdiary.feature.jrnl.day.model.JrnlDaySearchParam;
import io.nicheblog.dreamdiary.feature.jrnl.diary.model.JrnlDiaryDto;
import io.nicheblog.dreamdiary.feature.jrnl.dream.model.JrnlDreamDto;
import io.nicheblog.dreamdiary.feature.jrnl.entry.model.JrnlEntryDto;
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

    private static final String ENTRY_CTGR_NONE = "__NONE__";

    public static List<JrnlDayDto> filterInMemory(final List<JrnlDayDto> listDto, final JrnlDaySearchParam searchParam) {
        if (CollectionUtils.isEmpty(listDto) || searchParam == null) return listDto;

        final boolean showDiaries = searchParam.isShowDiaries();
        final boolean showDreams = searchParam.isShowDreams();

        final String diaryKeyword = StringUtils.defaultString(searchParam.getDiaryKeyword()).trim().toLowerCase();
        final String dreamKeyword = StringUtils.defaultString(searchParam.getDreamKeyword()).trim().toLowerCase();

        final boolean filterDiaries = showDiaries && StringUtils.isNotEmpty(diaryKeyword);
        final boolean filterDreams = showDreams && StringUtils.isNotEmpty(dreamKeyword);

        final List<String> entryCtgrCds = normalizeEntryCtgrCds(searchParam.getEntryCtgrCds());
        final boolean filterEntryCtgr = showDiaries && CollectionUtils.isNotEmpty(entryCtgrCds);

        if (!filterDiaries && !filterDreams && !filterEntryCtgr) return listDto;

        final boolean hasNoneCategory = filterEntryCtgr && entryCtgrCds.contains(ENTRY_CTGR_NONE);
        final Set<String> ctgrSet = new HashSet<>();
        if (filterEntryCtgr) {
            for (final String ctgr : entryCtgrCds) {
                if (StringUtils.isBlank(ctgr) || ENTRY_CTGR_NONE.equals(ctgr)) continue;
                ctgrSet.add(ctgr.trim());
            }
        }

        final List<JrnlDayDto> result = new ArrayList<>();
        for (final JrnlDayDto day : listDto) {
            List<JrnlEntryDto> filteredEntries = day.getJrnlEntryList();
            if (filterEntryCtgr || filterDiaries) {
                filteredEntries = new ArrayList<>();
                final List<JrnlEntryDto> entryList = day.getJrnlEntryList();
                if (CollectionUtils.isNotEmpty(entryList)) {
                    for (final JrnlEntryDto entry : entryList) {
                        if (filterEntryCtgr && !matchesEntryCtgr(entry, hasNoneCategory, ctgrSet)) continue;

                        if (filterDiaries) {
                            final List<JrnlDiaryDto> diaryList = entry.getJrnlDiaryList();
                            if (CollectionUtils.isEmpty(diaryList)) continue;
                            final List<JrnlDiaryDto> filteredDiaries = new ArrayList<>();
                            for (final JrnlDiaryDto diary : diaryList) {
                                if (containsKeyword(diary.getCn(), diaryKeyword)) filteredDiaries.add(diary);
                            }
                            if (filteredDiaries.isEmpty()) continue;
                            filteredEntries.add(entry.toBuilder().jrnlDiaryList(filteredDiaries).build());
                        } else {
                            filteredEntries.add(entry);
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

            if (filterDiaries && CollectionUtils.isEmpty(filteredEntries)) continue;
            if (filterDreams && CollectionUtils.isEmpty(filteredDreams) && CollectionUtils.isEmpty(filteredElseDreams)) continue;

            final JrnlDayDto nextDay = day.toBuilder()
                    .jrnlEntryList(filteredEntries)
                    .jrnlDreamList(filteredDreams)
                    .jrnlElseDreamList(filteredElseDreams)
                    .build();
            result.add(nextDay);
        }

        return result;
    }

    private static boolean matchesEntryCtgr(final JrnlEntryDto entry, final boolean hasNoneCategory, final Set<String> ctgrSet) {
        if (entry == null) return false;
        final String ctgrCd = StringUtils.trimToEmpty(entry.getCtgrCd());
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

    private static List<String> normalizeEntryCtgrCds(final List<String> entryCtgrCds) {
        if (CollectionUtils.isEmpty(entryCtgrCds)) return entryCtgrCds;
        if (entryCtgrCds.size() != 1) return entryCtgrCds;
        final String raw = entryCtgrCds.get(0);
        if (StringUtils.isBlank(raw) || !raw.contains(",")) return entryCtgrCds;

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
