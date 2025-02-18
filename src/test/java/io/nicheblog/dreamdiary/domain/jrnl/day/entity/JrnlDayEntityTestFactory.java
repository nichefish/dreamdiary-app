package io.nicheblog.dreamdiary.domain.jrnl.day.entity;

import io.nicheblog.dreamdiary.extension.clsf.ContentType;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.experimental.UtilityClass;
import org.springframework.test.context.ActiveProfiles;

/**
 * JrnlDayEntityTestFactory
 * <pre>
 *  저널 일자 테스트 Entity 생성 팩토리 모듈
 * </pre>
 *
 * @author nichefish 
 */
@UtilityClass
@ActiveProfiles("test")
public class JrnlDayEntityTestFactory {

    /**
     * 테스트용 저널 일자 Entity 생성
     * @param jrnlDtStr 저널 일자 문자열
     */
    public static JrnlDayEntity createWithJrnlDt(final String jrnlDtStr) throws Exception {
        return JrnlDayEntity.builder()
                .contentType(ContentType.JRNL_DAY.key)
                .jrnlDt(DateUtils.asDate(jrnlDtStr))
                .build();
    }

    /**
     * 테스트용 저널 일자 Entity 생성
     */
    public static JrnlDayEntity create() throws Exception {
        return JrnlDayEntity.builder()
                .contentType(ContentType.JRNL_DAY.key)
                .build();
    }

    /**
     * 테스트용 저널 일자 Entity (simple) 생성
     */
    public static JrnlDaySmpEntity createSmp() throws Exception {
        return JrnlDaySmpEntity.builder()
                .contentType(ContentType.JRNL_DAY.key)
                .build();
    }
}
