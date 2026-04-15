package io.nicheblog.dreamdiary.feature.journal.sbjct.model;

import io.nicheblog.dreamdiary.feature.clsf._shared.type.ContentType;
import lombok.experimental.UtilityClass;
import org.springframework.test.context.ActiveProfiles;

/**
 * JournalSbjctDtoTestFactory
 * <pre>
 *  저널 주제 테스트 Dto 생성 팩토리 모듈
 * </pre>
 *
 * @author nichefish 
 */
@UtilityClass
@ActiveProfiles("test")
public class JournalSbjctDtoTestFactory {

    /**
     * 테스트용 저널 주제 Dto 생성
     */
    public static JournalSbjctDto create() throws Exception {
        return JournalSbjctDto.builder()
                .contentType(ContentType.JOURNAL_SBJCT.key)
                .build();
    }

    /**
     * 테스트용 저널 주제 Dto 생성
     * @param key 식별자
     */
    public static JournalSbjctDto createWithKey(final Integer key) throws Exception {
        return JournalSbjctDto.builder()
                .id(key)
                .contentType(ContentType.JOURNAL_SBJCT.key)
                .build();
    }
}
