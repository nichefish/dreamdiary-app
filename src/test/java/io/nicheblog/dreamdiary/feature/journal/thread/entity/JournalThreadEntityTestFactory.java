package io.nicheblog.dreamdiary.feature.journal.thread.entity;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import lombok.experimental.UtilityClass;
import org.springframework.test.context.ActiveProfiles;

/**
 * JournalThreadEntityTestFactory
 * <pre>
 *  저널 스레드 테스트 Entity 생성 팩토리 모듈
 * </pre>
 *
 * @author nichefish 
 */
@UtilityClass
@ActiveProfiles("test")
public class JournalThreadEntityTestFactory {

    /**
     * 테스트용 저널 스레드 Entity 생성
     */
    public static JournalThreadEntity create() throws Exception {
        return JournalThreadEntity.builder()
                .contentType(ContentType.JOURNAL_THREAD.key)
                .title("test_title")
                .content("test_cn")
                .categoryCode("test_category_code")
                .build();
    }
}
