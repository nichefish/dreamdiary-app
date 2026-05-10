package io.nicheblog.dreamdiary.feature.attachable.tag.entity;

import lombok.experimental.UtilityClass;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;

/**
 * TagEntityTestFactory
 * <pre>
 *  태그-컨텐츠 Entity 생성 팩토리 모듈
 * </pre>
 *
 * @author nichefish 
 */
@UtilityClass
@ActiveProfiles("test")
public class TagEntityTestFactory {

    /**
     * 테스트용 태그 Entity 생성
     */
    public static TagEntity create() throws Exception {
        return TagEntity.builder()
                .name("태그")
                .tagContentList(new ArrayList<>())
                .build();
    }
}
