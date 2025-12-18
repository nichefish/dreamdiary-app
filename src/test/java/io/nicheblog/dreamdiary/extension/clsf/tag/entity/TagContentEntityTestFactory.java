package io.nicheblog.dreamdiary.extension.clsf.tag.entity;

import lombok.experimental.UtilityClass;
import org.springframework.test.context.ActiveProfiles;

/**
 * TagContentEntityTestFactory
 * <pre>
 *  태그-컨텐츠 Entity 생성 팩토리 모듈
 * </pre>
 *
 * @author nichefish 
 */
@UtilityClass
@ActiveProfiles("test")
public class TagContentEntityTestFactory {

    /**
     * 테스트용 태그-컨텐츠 Entity 생성
     */
    public static TagContentEntity create() throws Exception {
        return TagContentEntity.builder()
                .tagNm("태그")
                .build();
    }
}
