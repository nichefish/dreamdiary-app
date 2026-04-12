package io.nicheblog.dreamdiary.feature.jrnl.diary.entity;

import io.nicheblog.dreamdiary.feature.clsf._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.jrnl.day.entity.JrnlDayEntity;
import io.nicheblog.dreamdiary.feature.jrnl.day.entity.JrnlDaySmpEntity;
import io.nicheblog.dreamdiary.feature.jrnl.chapter.entity.JrnlChapterEntity;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.experimental.UtilityClass;
import org.springframework.test.context.ActiveProfiles;

/**
 * JrnlDiaryEntityTestFactory
 * <pre>
 *  저널 일기 테스트 Entity 생성 팩토리 모듈
 * </pre>
 *
 * @author nichefish 
 */
@UtilityClass
@ActiveProfiles("test")
public class JrnlDiaryEntityTestFactory {

    /**
     * 테스트용 저널 일기 Entity 생성
     * @param jrnlDayEntity 저널 일자 Entity 객체
     */
    public static JrnlDiaryEntity createWithJrnlDay(JrnlDayEntity jrnlDayEntity) throws Exception {
        return JrnlDiaryEntity.builder()
                .contentType(ContentType.JRNL_DIARY.key)
                .title("test_title")
                .cn("test_cn")
                .jrnlChapter(JrnlChapterEntity.builder()
                        .jrnlDayNo(jrnlDayEntity != null ? jrnlDayEntity.getPostNo() : null)
                        .jrnlDay(toSmpEntity(jrnlDayEntity))
                        .build())
                .build();
    }

    /**
     * 테스트용 저널 일기 Entity 생성
     * @param jrnlDtStr 저널 일자 날짜 문자열
     */
    public static JrnlDiaryEntity createWithJrnlDt(String jrnlDtStr) throws Exception {
        final JrnlDaySmpEntity jrnlDay = JrnlDaySmpEntity.builder().jrnlDt(DateUtils.asDate(jrnlDtStr)).build();
        return JrnlDiaryEntity.builder()
                .contentType(ContentType.JRNL_DIARY.key)
                .title("test_title")
                .cn("test_cn")
                .jrnlChapter(JrnlChapterEntity.builder().jrnlDay(jrnlDay).build())
                .build();
    }

    /**
     * 테스트용 저널 일기 Entity 생성
     */
    public static JrnlDiaryEntity create() throws Exception {
        String tempJrnlDtStr = "2000-01-01";
        return createWithJrnlDt(tempJrnlDtStr);
    }

    private static JrnlDaySmpEntity toSmpEntity(final JrnlDayEntity jrnlDayEntity) {
        if (jrnlDayEntity == null) return null;
        return JrnlDaySmpEntity.builder()
                .postNo(jrnlDayEntity.getPostNo())
                .contentType(jrnlDayEntity.getContentType())
                .jrnlDt(jrnlDayEntity.getJrnlDt())
                .dtUnknownYn(jrnlDayEntity.getDtUnknownYn())
                .yy(jrnlDayEntity.getYy())
                .mnth(jrnlDayEntity.getMnth())
                .aprxmtDt(jrnlDayEntity.getAprxmtDt())
                .weather(jrnlDayEntity.getWeather())
                .build();
    }
}
