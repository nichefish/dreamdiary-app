package io.nicheblog.dreamdiary.feature.journal.diary.model;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import lombok.experimental.UtilityClass;
import org.springframework.test.context.ActiveProfiles;

/**
 * JournalDiaryDtoTestFactory
 * <pre>
 *  저널 일기 테스트 Dto 생성 팩토리 모듈
 * </pre>
 *
 * @author nichefish 
 */
@UtilityClass
@ActiveProfiles("test")
public class JournalDiaryDtoTestFactory {

    /**
     * 테스트용 저널 일기 Dto 생성
     */
    public static JournalDiaryDto create() throws Exception {
        return JournalDiaryDto.builder()
                .contentType(ContentType.JOURNAL_DIARY.key)
                .build();
    }

    /**
     * 테스트용 저널 일기 Dto 생성
     * @param key 식별자
     */
    public static JournalDiaryDto createWithKey(final Integer key) throws Exception {
        return JournalDiaryDto.builder()
                .id(key)
                .contentType(ContentType.JOURNAL_DIARY.key)
                .build();
    }

    /**
     * 테스트용 일기 PostDto 생성
     */
    public static JournalDiaryPostDto createPost() throws Exception {
        return JournalDiaryPostDto.builder()
                .contentType(ContentType.JOURNAL_DIARY.key)
                .build();
    }

    /**
     * 테스트용 일기 PostDto 생성
     * @param key 식별키
     */
    public static JournalDiaryPostDto createPostWithKey(final Integer key) throws Exception {
        return JournalDiaryPostDto.builder()
                .id(key)
                .contentType(ContentType.JOURNAL_DIARY.key)
                .build();
    }
}
