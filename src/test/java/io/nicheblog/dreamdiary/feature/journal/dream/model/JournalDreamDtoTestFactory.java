package io.nicheblog.dreamdiary.feature.journal.dream.model;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import lombok.experimental.UtilityClass;
import org.springframework.test.context.ActiveProfiles;

/**
 * JournalDreamDtoTestFactory
 * <pre>
 *  저널 꿈 테스트 Dto 생성 팩토리 모듈
 * </pre>
 *
 * @author nichefish 
 */
@UtilityClass
@ActiveProfiles("test")
public class JournalDreamDtoTestFactory {

    /**
     * 테스트용 저널 꿈 Dto 생성
     */
    public static JournalDreamDto create() throws Exception {
        return JournalDreamDto.builder()
                .contentType(ContentType.JOURNAL_DREAM.key)
                .build();
    }

    /**
     * 테스트용 저널 꿈 Dto 생성
     * @param key 식별자
     */
    public static JournalDreamDto createWithKey(final Integer key) throws Exception {
        return JournalDreamDto.builder()
                .id(key)
                .contentType(ContentType.JOURNAL_DREAM.key)
                .build();
    }

    /**
     * 테스트용 꿈 PostDto 생성
     */
    public static JournalDreamPostDto createPost() throws Exception {
        return JournalDreamPostDto.builder()
                .contentType(ContentType.JOURNAL_DREAM.key)
                .build();
    }

    /**
     * 테스트용 꿈 PostDto 생성
     * @param key 식별키
     */
    public static JournalDreamPostDto createPostWithKey(final Integer key) throws Exception {
        return JournalDreamPostDto.builder()
                .id(key)
                .contentType(ContentType.JOURNAL_DREAM.key)
                .build();
    }
}
