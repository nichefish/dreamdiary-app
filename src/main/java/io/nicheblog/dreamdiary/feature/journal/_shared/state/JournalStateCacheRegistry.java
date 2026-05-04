package io.nicheblog.dreamdiary.feature.journal._shared.state;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import lombok.experimental.UtilityClass;

import java.util.List;

@UtilityClass
public class JournalStateCacheRegistry {

    private static final List<ContentType> STATE_CONTENT_TYPES = List.of(
            ContentType.JOURNAL_CHAPTER,
            ContentType.JOURNAL_DIARY,
            ContentType.JOURNAL_DREAM,
            ContentType.JOURNAL_INTERPRETATION
    );

    public static boolean supports(final ContentType contentType) {
        return STATE_CONTENT_TYPES.contains(contentType);
    }

    public static List<ContentType> stateContentTypes() {
        return STATE_CONTENT_TYPES;
    }

    public static String monthlyMapCacheName(final ContentType contentType) {
        return switch (contentType) {
            case JOURNAL_CHAPTER -> "journalChapterStateMapByUser";
            case JOURNAL_DIARY -> "journalDiaryStateMapByUser";
            case JOURNAL_DREAM -> "journalDreamStateMapByUser";
            case JOURNAL_INTERPRETATION -> "journalInterpretationStateMapByUser";
            default -> throw new IllegalStateException("Unexpected value: " + contentType);
        };
    }

    public static String weeklyMapCacheName(final ContentType contentType) {
        return switch (contentType) {
            case JOURNAL_CHAPTER -> "journalChapterWeeklyStateMapByUser";
            case JOURNAL_DIARY -> "journalDiaryWeeklyStateMapByUser";
            case JOURNAL_DREAM -> "journalDreamWeeklyStateMapByUser";
            case JOURNAL_INTERPRETATION -> "journalInterpretationWeeklyStateMapByUser";
            default -> throw new IllegalStateException("Unexpected value: " + contentType);
        };
    }

    public static String annualStateListCacheName(final ContentType contentType) {
        return switch (contentType) {
            case JOURNAL_DIARY, JOURNAL_DREAM -> "journalEntryYyAnnualStatedListByUser";
            default -> null;
        };
    }
}
