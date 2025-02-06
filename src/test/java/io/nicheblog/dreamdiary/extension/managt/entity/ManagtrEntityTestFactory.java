package io.nicheblog.dreamdiary.extension.managt.entity;

import io.nicheblog.dreamdiary.extension.managt.entity.ManagtrEntity;
import lombok.experimental.UtilityClass;
import org.springframework.test.context.ActiveProfiles;

/**
 * ManagtEntityTestFactory
 * <pre>
 *  조치자 테스트 Entity 생성 팩토리 모듈
 * </pre>
 *
 * @author nichefish 
 */
@UtilityClass
@ActiveProfiles("test")
public class ManagtrEntityTestFactory {

    /**
     * 테스트용 조치자 Entity 생성
     */
    public static ManagtrEntity create() throws Exception {
        return ManagtrEntity.builder()
                .build();
    }
}
