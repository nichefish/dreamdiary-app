package io.nicheblog.dreamdiary.domain.jrnl.sbjct.entity;

import io.nicheblog.dreamdiary.extension.ContentType;
import lombok.experimental.UtilityClass;
import org.springframework.test.context.ActiveProfiles;

/**
 * JrnlSbjctEntityTestFactory
 * <pre>
 *  저널 주제 테스트 Entity 생성 팩토리 모듈
 * </pre>
 *
 * @author nichefish 
 */
@UtilityClass
@ActiveProfiles("test")
public class JrnlSbjctEntityTestFactory {

    /**
     * 테스트용 저널 주제 Entity 생성
     */
    public static JrnlSbjctEntity create() throws Exception {
        return JrnlSbjctEntity.builder()
                .contentType(ContentType.JRNL_SBJCT.key)
                .title("test_title")
                .cn("test_cn")
                .ctgrCd("test_ctgr_cd")
                .build();
    }
}
