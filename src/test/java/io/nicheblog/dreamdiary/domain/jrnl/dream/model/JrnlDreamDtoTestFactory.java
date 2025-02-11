package io.nicheblog.dreamdiary.domain.jrnl.dream.model;

import io.nicheblog.dreamdiary.extension.clsf.ContentType;
import lombok.experimental.UtilityClass;
import org.springframework.test.context.ActiveProfiles;

/**
 * JrnlDreamDtoTestFactory
 * <pre>
 *  저널 꿈 테스트 Dto 생성 팩토리 모듈
 * </pre>
 *
 * @author nichefish 
 */
@UtilityClass
@ActiveProfiles("test")
public class JrnlDreamDtoTestFactory {

    /**
     * 테스트용 저널 꿈 Dto 생성
     */
    public static JrnlDreamDto create() throws Exception {
        return JrnlDreamDto.builder()
                .contentType(ContentType.JRNL_DREAM.key)
                .build();
    }

    /**
     * 테스트용 저널 꿈 Dto 생성
     * @param key 식별자
     */
    public static JrnlDreamDto createWithKey(final Integer key) throws Exception {
        return JrnlDreamDto.builder()
                .postNo(key)
                .contentType(ContentType.JRNL_DREAM.key)
                .build();
    }
}
