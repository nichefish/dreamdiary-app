package io.nicheblog.dreamdiary.feature.journal.dream.entity;

import io.nicheblog.dreamdiary.feature.clsf._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.journal.day.entity.JournalDayEntity;
import io.nicheblog.dreamdiary.feature.journal.day.entity.JournalDaySmpEntity;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.experimental.UtilityClass;
import org.springframework.test.context.ActiveProfiles;

/**
 * JournalDreamEntityTestFactory
 * <pre>
 *  저널 꿈 테스트 Entity 생성 팩토리 모듈
 * </pre>
 *
 * @author nichefish 
 */
@UtilityClass
@ActiveProfiles("test")
public class JournalDreamEntityTestFactory {

    /**
     * 테스트용 저널 꿈 Entity 생성
     * @param journalDayEntity 저널 일자 객체
     */
    public static JournalDreamEntity createWithJournalDay(JournalDayEntity journalDayEntity) throws Exception {
        return JournalDreamEntity.builder()
                .contentType(ContentType.JOURNAL_DREAM.key)
                .title("test_title")
                .content("test_cn")
                .journalDayId(journalDayEntity != null ? journalDayEntity.getId() : null)
                .journalDay(toSmpEntity(journalDayEntity))
                .build();
    }

    /**
     * 테스트용 저널 꿈 Entity 생성
     * @param journalDtStr 저널 일자 날짜 문자열
     */
    public static JournalDreamEntity createWithJournalDt(String journalDtStr) throws Exception {
        return JournalDreamEntity.builder()
                .contentType(ContentType.JOURNAL_DREAM.key)
                .title("test_title")
                .content("test_cn")
                .journalDay(JournalDaySmpEntity.builder().journalDt(DateUtils.asDate(journalDtStr)).build())
                .build();
    }

    /**
     * 테스트용 저널 꿈 Entity 생성
     */
    public static JournalDreamEntity create() throws Exception {
        String tempJournalDtStr = "2000-01-01";
        return createWithJournalDt(tempJournalDtStr);
    }

    private static JournalDaySmpEntity toSmpEntity(final JournalDayEntity journalDayEntity) {
        if (journalDayEntity == null) return null;
        return JournalDaySmpEntity.builder()
                .id(journalDayEntity.getId())
                .contentType(journalDayEntity.getContentType())
                .journalDt(journalDayEntity.getJournalDt())
                .dtUnknownYn(journalDayEntity.getDtUnknownYn())
                .yy(journalDayEntity.getYy())
                .mnth(journalDayEntity.getMnth())
                .aprxmtDt(journalDayEntity.getAprxmtDt())
                .weather(journalDayEntity.getWeather())
                .build();
    }
}

