package io.nicheblog.dreamdiary.domain.jrnl.dream.entity;

import io.nicheblog.dreamdiary.domain.jrnl.day.entity.JrnlDayEntity;
import io.nicheblog.dreamdiary.domain.jrnl.day.entity.JrnlDaySmpEntity;
import io.nicheblog.dreamdiary.extension.clsf.ContentType;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.experimental.UtilityClass;
import org.springframework.test.context.ActiveProfiles;

/**
 * JrnlDreamEntityTestFactory
 * <pre>
 *  저널 꿈 테스트 Entity 생성 팩토리 모듈
 * </pre>
 *
 * @author nichefish 
 */
@UtilityClass
@ActiveProfiles("test")
public class JrnlDreamEntityTestFactory {

    /**
     * 테스트용 저널 꿈 Entity 생성
     * @param jrnlDayEntity 저널 일자 객체
     */
    public static JrnlDreamEntity createWithJrnlDay(JrnlDayEntity jrnlDayEntity) throws Exception {
        return JrnlDreamEntity.builder()
                .contentType(ContentType.JRNL_DREAM.key)
                .title("test_title")
                .cn("test_cn")
                .jrnlDayNo(jrnlDayEntity != null ? jrnlDayEntity.getPostNo() : null)
                .jrnlDay(toSmpEntity(jrnlDayEntity))
                .build();
    }

    /**
     * 테스트용 저널 꿈 Entity 생성
     * @param jrnlDtStr 저널 일자 날짜 문자열
     */
    public static JrnlDreamEntity createWithJrnlDt(String jrnlDtStr) throws Exception {
        return JrnlDreamEntity.builder()
                .contentType(ContentType.JRNL_DREAM.key)
                .title("test_title")
                .cn("test_cn")
                .jrnlDay(JrnlDaySmpEntity.builder().jrnlDt(DateUtils.asDate(jrnlDtStr)).build())
                .build();
    }

    /**
     * 테스트용 저널 꿈 Entity 생성
     */
    public static JrnlDreamEntity create() throws Exception {
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
