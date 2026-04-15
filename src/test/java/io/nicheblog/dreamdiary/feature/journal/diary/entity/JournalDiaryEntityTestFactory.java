package io.nicheblog.dreamdiary.feature.journal.diary.entity;

import io.nicheblog.dreamdiary.feature.clsf._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.journal.chapter.entity.JournalChapterEntity;
import io.nicheblog.dreamdiary.feature.journal.day.entity.JournalDayEntity;
import io.nicheblog.dreamdiary.feature.journal.day.entity.JournalDaySmpEntity;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.experimental.UtilityClass;
import org.springframework.test.context.ActiveProfiles;

/**
 * JournalDiaryEntityTestFactory
 * <pre>
 *  저널 일기 테스트 Entity 생성 팩토리 모듈
 * </pre>
 *
 * @author nichefish 
 */
@UtilityClass
@ActiveProfiles("test")
public class JournalDiaryEntityTestFactory {

    /**
     * 테스트용 저널 일기 Entity 생성
     * @param journalDayEntity 저널 일자 Entity 객체
     */
    public static JournalDiaryEntity createWithJournalDay(JournalDayEntity journalDayEntity) throws Exception {
        return JournalDiaryEntity.builder()
                .contentType(ContentType.JOURNAL_DIARY.key)
                .title("test_title")
                .content("test_cn")
                .journalChapter(JournalChapterEntity.builder()
                        .journalDayId(journalDayEntity != null ? journalDayEntity.getId() : null)
                        .journalDay(toSmpEntity(journalDayEntity))
                        .build())
                .build();
    }

    /**
     * 테스트용 저널 일기 Entity 생성
     * @param journalDtStr 저널 일자 날짜 문자열
     */
    public static JournalDiaryEntity createWithJournalDt(String journalDtStr) throws Exception {
        final JournalDaySmpEntity journalDay = JournalDaySmpEntity.builder().journalDt(DateUtils.asDate(journalDtStr)).build();
        return JournalDiaryEntity.builder()
                .contentType(ContentType.JOURNAL_DIARY.key)
                .title("test_title")
                .content("test_cn")
                .journalChapter(JournalChapterEntity.builder().journalDay(journalDay).build())
                .build();
    }

    /**
     * 테스트용 저널 일기 Entity 생성
     */
    public static JournalDiaryEntity create() throws Exception {
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

