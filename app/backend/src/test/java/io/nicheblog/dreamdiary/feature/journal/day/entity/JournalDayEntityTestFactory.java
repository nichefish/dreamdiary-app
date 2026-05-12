package io.nicheblog.dreamdiary.feature.journal.day.entity;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.experimental.UtilityClass;
import org.springframework.test.context.ActiveProfiles;

/**
 * JournalDayEntityTestFactory
 * <pre>
 *  저널 일자 테스트 Entity 생성 팩토리 모듈
 * </pre>
 *
 * @author nichefish 
 */
@UtilityClass
@ActiveProfiles("test")
public class JournalDayEntityTestFactory {

    /**
     * 테스트용 저널 일자 Entity 생성
     * @param journalDtStr 저널 일자 문자열
     */
    public static JournalDayEntity createWithJournalDt(final String journalDtStr) throws Exception {
        return JournalDayEntity.builder()
                .contentType(ContentType.JOURNAL_DAY.key)
                .journalDate(DateUtils.asDate(journalDtStr))
                .build();
    }

    /**
     * 테스트용 저널 일자 Entity 생성
     */
    public static JournalDayEntity create() throws Exception {
        return JournalDayEntity.builder()
                .contentType(ContentType.JOURNAL_DAY.key)
                .build();
    }

    /**
     * 테스트용 저널 일자 Entity (simple) 생성
     */
    public static JournalDaySmpEntity createSmp() throws Exception {
        return JournalDaySmpEntity.builder()
                .contentType(ContentType.JOURNAL_DAY.key)
                .build();
    }
}

