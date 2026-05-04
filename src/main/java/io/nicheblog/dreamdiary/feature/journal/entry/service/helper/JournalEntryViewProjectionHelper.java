package io.nicheblog.dreamdiary.feature.journal.entry.service.helper;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.journal.chapter.model.JournalChapterDto;
import io.nicheblog.dreamdiary.feature.journal.day.model.JournalDayDto;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryDto;
import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@UtilityClass
public class JournalEntryViewProjectionHelper {

    /**
     * 챕터 DTO에 엔트리 목록을 복사 적용한다.
     *
     * @param chapter 대상 챕터
     * @param entryList 엔트리 목록
     */
    public static void applyChapterEntries(final JournalChapterDto chapter, final List<JournalEntryDto> entryList) {
        if (chapter == null) return;
        chapter.setJournalEntryList(emptyToNull(copyEntries(entryList)));
    }

    /**
     * 챕터의 전체 엔트리 목록을 반환한다.
     *
     * @param chapter 대상 챕터
     * @return 엔트리 목록
     */
    public static List<JournalEntryDto> getChapterEntries(final JournalChapterDto chapter) {
        if (chapter == null) return List.of();
        return defaultList(chapter.getJournalEntryList());
    }

    /**
     * 챕터의 일기 엔트리 목록을 반환한다.
     *
     * @param chapter 대상 챕터
     * @return 일기 엔트리 목록
     */
    public static List<JournalEntryDto> getDiaryEntries(final JournalChapterDto chapter) {
        if (chapter == null) return List.of();
        return defaultList(filterByContentType(chapter.getJournalEntryList(), ContentType.JOURNAL_DIARY));
    }

    /**
     * 챕터의 꿈 엔트리 목록을 반환한다.
     *
     * @param chapter 대상 챕터
     * @return 꿈 엔트리 목록
     */
    public static List<JournalEntryDto> getDreamEntries(final JournalChapterDto chapter) {
        if (chapter == null) return List.of();
        return defaultList(filterByContentType(getChapterEntries(chapter), ContentType.JOURNAL_DREAM));
    }

    /**
     * 콘텐츠 타입에 맞는 엔트리 목록을 반환한다.
     *
     * @param chapter 대상 챕터
     * @param contentType 콘텐츠 타입
     * @return 타입별 엔트리 목록
     */
    public static List<JournalEntryDto> getEntriesByType(final JournalChapterDto chapter, final ContentType contentType) {
        if (chapter == null || contentType == null) return List.of();
        return switch (contentType) {
            case JOURNAL_DIARY -> getDiaryEntries(chapter);
            case JOURNAL_DREAM -> getDreamEntries(chapter);
            default -> List.of();
        };
    }

    /**
     * 특정 콘텐츠 타입 엔트리를 교체한 새 목록을 반환한다.
     *
     * @param chapter 대상 챕터
     * @param contentType 교체 대상 콘텐츠 타입
     * @param replacements 대체 엔트리 목록
     * @return 교체된 엔트리 목록
     */
    public static List<JournalEntryDto> replaceChapterEntries(
            final JournalChapterDto chapter,
            final ContentType contentType,
            final List<JournalEntryDto> replacements
    ) {
        final List<JournalEntryDto> updatedEntries = new ArrayList<>();
        if (chapter == null || contentType == null) return updatedEntries;

        for (final JournalEntryDto entry : getChapterEntries(chapter)) {
            if (entry == null) continue;
            if (isContentType(entry, contentType)) continue;
            updatedEntries.add(entry);
        }
        updatedEntries.addAll(defaultList(replacements));
        return updatedEntries;
    }

    /**
     * day DTO에 꿈/타인꿈 목록을 분리 적용한다.
     *
     * @param day 대상 day DTO
     */
    public static void applyDayDreamEntries(final JournalDayDto day) {
        if (day == null) return;

        final List<JournalEntryDto> dreamEntries = new ArrayList<>();
        final List<JournalEntryDto> elseDreamEntries = new ArrayList<>();

        final List<JournalChapterDto> chapterList = day.getJournalChapterList() != null
                ? day.getJournalChapterList()
                : List.of();
        for (final JournalChapterDto chapter : chapterList) {
            for (final JournalEntryDto entry : getChapterEntries(chapter)) {
                if (!isContentType(entry, ContentType.JOURNAL_DREAM)) continue;
                if (Objects.equals(entry.getElseDreamYn(), "Y")) {
                    elseDreamEntries.add(entry);
                    continue;
                }
                dreamEntries.add(entry);
            }
        }

        day.setJournalDreamList(emptyToNull(dreamEntries));
        day.setJournalElseDreamList(emptyToNull(elseDreamEntries));
    }

    /**
     * 엔트리 목록을 콘텐츠 타입 기준으로 필터링한다.
     *
     * @param entryList 엔트리 목록
     * @param contentType 콘텐츠 타입
     * @return 필터링된 엔트리 목록
     */
    private static List<JournalEntryDto> filterByContentType(
            final List<JournalEntryDto> entryList,
            final ContentType contentType
    ) {
        if (CollectionUtils.isEmpty(entryList) || contentType == null) return null;

        final List<JournalEntryDto> filteredEntries = new ArrayList<>();
        for (final JournalEntryDto entry : entryList) {
            if (!isContentType(entry, contentType)) continue;
            filteredEntries.add(entry);
        }
        return emptyToNull(filteredEntries);
    }

    /**
     * 엔트리의 콘텐츠 타입 일치 여부를 확인한다.
     *
     * @param entry 엔트리 DTO
     * @param contentType 콘텐츠 타입
     * @return 일치 여부
     */
    private static boolean isContentType(final JournalEntryDto entry, final ContentType contentType) {
        return entry != null
                && contentType != null
                && Objects.equals(contentType.key, entry.getContentType());
    }

    /**
     * 엔트리 목록을 가변 리스트로 복사한다.
     *
     * @param entryList 엔트리 목록
     * @return 복사된 리스트
     */
    private static List<JournalEntryDto> copyEntries(final List<JournalEntryDto> entryList) {
        if (CollectionUtils.isEmpty(entryList)) return new ArrayList<>();
        return new ArrayList<>(entryList);
    }

    /**
     * null 목록을 빈 목록으로 치환한다.
     *
     * @param entryList 엔트리 목록
     * @return null-safe 목록
     */
    private static List<JournalEntryDto> defaultList(final List<JournalEntryDto> entryList) {
        return entryList != null ? entryList : List.of();
    }

    /**
     * 빈 목록을 null로 변환한다.
     *
     * @param entryList 엔트리 목록
     * @return 비어있지 않으면 원본, 비어있으면 null
     */
    private static List<JournalEntryDto> emptyToNull(final List<JournalEntryDto> entryList) {
        return CollectionUtils.isEmpty(entryList) ? null : entryList;
    }
}
