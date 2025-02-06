package io.nicheblog.dreamdiary.extension.tag.entity;

import io.nicheblog.dreamdiary.extension.tag.entity.ContentTagEntity;
import lombok.experimental.UtilityClass;
import org.springframework.test.context.ActiveProfiles;

/**
 * ContentTagEntityTestFactory
 * <pre>
 *  컨텐츠-태그 Entity 생성 팩토리 모듈
 * </pre>
 *
 * @author nichefish 
 */
@UtilityClass
@ActiveProfiles("test")
public class ContentTagEntityTestFactory {

    /**
     * 테스트용 컨텐츠 태그 Entity 생성
     */
    public static ContentTagEntity create() throws Exception {
        return ContentTagEntity.builder()
                .tagNm("태그")
                .build();
    }
}
