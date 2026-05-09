package io.nicheblog.dreamdiary.feature.journal.thread.model;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import lombok.experimental.UtilityClass;
import org.springframework.test.context.ActiveProfiles;

/**
 * JournalThreadDtoTestFactory
 * <pre>
 *  저널 스레드 테스트 Dto 생성 팩토리 모듈
 * </pre>
 *
 * @author nichefish 
 */
@UtilityClass
@ActiveProfiles("test")
public class JournalThreadDtoTestFactory {

    /**
     * 테스트용 저널 스레드 Dto 생성
     */
    public static JournalThreadDto create() throws Exception {
        return JournalThreadDto.builder()
                .contentType(ContentType.JOURNAL_THREAD.key)
                .build();
    }

    /**
     * 테스트용 저널 스레드 Dto 생성
     * @param key 식별자
     */
    public static JournalThreadDto createWithKey(final Integer key) throws Exception {
        return JournalThreadDto.builder()
                .id(key)
                .contentType(ContentType.JOURNAL_THREAD.key)
                .build();
    }
}
