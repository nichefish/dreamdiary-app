package io.nicheblog.dreamdiary.feature.journal.entry.model;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import lombok.experimental.UtilityClass;
import org.springframework.test.context.ActiveProfiles;

@UtilityClass
@ActiveProfiles("test")
public class JournalEntryDtoTestFactory {

    public static JournalEntryDto create(final ContentType contentType) {
        return JournalEntryDto.builder()
                .contentType(contentType.key)
                .build();
    }

    public static JournalEntryDto createWithKey(final Integer key, final ContentType contentType) {
        return JournalEntryDto.builder()
                .id(key)
                .contentType(contentType.key)
                .build();
    }

    public static JournalEntryPostDto createPost(final ContentType contentType) {
        return JournalEntryPostDto.builder()
                .contentType(contentType.key)
                .build();
    }

    public static JournalEntryPostDto createPostWithKey(final Integer key, final ContentType contentType) {
        return JournalEntryPostDto.builder()
                .id(key)
                .contentType(contentType.key)
                .build();
    }

    public static JournalEntryPostDto createDiaryPost() {
        return createPost(ContentType.JOURNAL_DIARY);
    }

    public static JournalEntryPostDto createDiaryPostWithKey(final Integer key) {
        return createPostWithKey(key, ContentType.JOURNAL_DIARY);
    }

    public static JournalEntryPostDto createDreamPost() {
        return createPost(ContentType.JOURNAL_DREAM);
    }

    public static JournalEntryPostDto createDreamPostWithKey(final Integer key) {
        return createPostWithKey(key, ContentType.JOURNAL_DREAM);
    }

}
