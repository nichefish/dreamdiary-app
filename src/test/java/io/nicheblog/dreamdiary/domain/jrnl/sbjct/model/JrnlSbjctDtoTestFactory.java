package io.nicheblog.dreamdiary.domain.jrnl.sbjct.model;

import io.nicheblog.dreamdiary.extension.clsf.ContentType;
import lombok.experimental.UtilityClass;
import org.springframework.test.context.ActiveProfiles;

/**
 * JrnlSbjctDtoTestFactory
 * <pre>
 *  저널 주제 테스트 Dto 생성 팩토리 모듈
 * </pre>
 *
 * @author nichefish 
 */
@UtilityClass
@ActiveProfiles("test")
public class JrnlSbjctDtoTestFactory {

    /**
     * 테스트용 저널 주제 Dto 생성
     */
    public static JrnlSbjctDto create() throws Exception {
        return JrnlSbjctDto.builder()
                .contentType(ContentType.JRNL_SBJCT.key)
                .build();
    }

    /**
     * 테스트용 저널 주제 Dto 생성
     * @param key 식별자
     */
    public static JrnlSbjctDto createWithKey(final Integer key) throws Exception {
        return JrnlSbjctDto.builder()
                .postNo(key)
                .contentType(ContentType.JRNL_SBJCT.key)
                .build();
    }
}
