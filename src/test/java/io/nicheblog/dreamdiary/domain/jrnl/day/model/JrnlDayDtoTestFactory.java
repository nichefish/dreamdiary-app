package io.nicheblog.dreamdiary.domain.jrnl.day.model;

import io.nicheblog.dreamdiary.extension.clsf.ContentType;
import lombok.experimental.UtilityClass;
import org.springframework.test.context.ActiveProfiles;

/**
 * JrnlDayDtoTestFactory
 * <pre>
 *  저널 일자 테스트 Dto 생성 팩토리 모듈
 * </pre>
 *
 * @author nichefish 
 */
@UtilityClass
@ActiveProfiles("test")
public class JrnlDayDtoTestFactory {

    /**
     * 테스트용 저널 일자 Dto 생성
     */
    public static JrnlDayDto create() throws Exception {
        return JrnlDayDto.builder()
                .contentType(ContentType.JRNL_DAY.key)
                .build();
    }

    /**
     * 테스트용 저널 일자 Dto 생성
     * @param key 식별자
     */
    public static JrnlDayDto createWithKey(final Integer key) throws Exception {
        return JrnlDayDto.builder()
                .postNo(key)
                .contentType(ContentType.JRNL_DAY.key)
                .build();
    }

    /**
     * 테스트용 저널 일자 Dto 생성
     */
    public static JrnlDayDto createWithJrnlDt(final String jrnlDtStr) throws Exception {
        return JrnlDayDto.builder()
                .postNo(0)
                .contentType(ContentType.JRNL_DAY.key)
                .jrnlDt(jrnlDtStr)
                .build();
    }

}
