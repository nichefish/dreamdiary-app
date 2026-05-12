package io.nicheblog.dreamdiary.feature.journal.day.model;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import lombok.experimental.UtilityClass;
import org.springframework.test.context.ActiveProfiles;

/**
 * JournalDayDtoTestFactory
 * <pre>
 *  저널 일자 테스트 Dto 생성 팩토리 모듈
 * </pre>
 *
 * @author nichefish 
 */
@UtilityClass
@ActiveProfiles("test")
public class JournalDayDtoTestFactory {

    /**
     * 테스트용 저널 일자 Dto 생성
     */
    public static JournalDayDto create() throws Exception {
        return JournalDayDto.builder()
                .contentType(ContentType.JOURNAL_DAY.key)
                .build();
    }

    /**
     * 테스트용 저널 일자 Dto 생성
     * @param key 식별자
     */
    public static JournalDayDto createWithKey(final Integer key) throws Exception {
        return JournalDayDto.builder()
                .id(key)
                .contentType(ContentType.JOURNAL_DAY.key)
                .build();
    }

    /**
     * 테스트용 저널 일자 Dto 생성
     */
    public static JournalDayDto createWithJournalDt(final String journalDtStr) throws Exception {
        return JournalDayDto.builder()
                .id(0)
                .contentType(ContentType.JOURNAL_DAY.key)
                .journalDate(journalDtStr)
                .build();
    }

}

