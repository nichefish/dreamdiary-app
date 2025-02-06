package io.nicheblog.dreamdiary.domain.schdul.entity;

import io.nicheblog.dreamdiary.extension.ContentType;
import lombok.experimental.UtilityClass;
import org.springframework.test.context.ActiveProfiles;

/**
 * SchdulEntityTestFactory
 * <pre>
 *  일정 테스트 Entity 생성 팩토리 모듈
 * </pre>
 *
 * @author nichefish 
 */
@UtilityClass
@ActiveProfiles("test")
public class SchdulEntityTestFactory {

    /**
     * 테스트용 일정 Entity 생성
     */
    public static SchdulEntity create() throws Exception {
        return SchdulEntity.builder()
                .contentType(ContentType.SCHDUL.key)
                .title("test_title")
                .cn("test_cn")
                .ctgrCd("test_ctgr_cd")
                .build();
    }
}
