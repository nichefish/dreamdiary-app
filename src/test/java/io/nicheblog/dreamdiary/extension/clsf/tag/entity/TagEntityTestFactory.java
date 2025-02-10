package io.nicheblog.dreamdiary.extension.clsf.tag.entity;

import io.nicheblog.dreamdiary.extension.clsf.tag.entity.TagEntity;
import lombok.experimental.UtilityClass;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;

/**
 * TagEntityTestFactory
 * <pre>
 *  컨텐츠 태그 Entity 생성 팩토리 모듈
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
                .tagNm("태그")
                .contentTagList(new ArrayList<>())
                .build();
    }
}
