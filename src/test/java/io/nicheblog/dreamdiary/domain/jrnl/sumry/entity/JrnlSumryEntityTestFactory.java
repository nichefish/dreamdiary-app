package io.nicheblog.dreamdiary.domain.jrnl.sumry.entity;

import io.nicheblog.dreamdiary.extension.clsf.ContentType;
import lombok.experimental.UtilityClass;
import org.springframework.test.context.ActiveProfiles;

/**
 * JrnlSumryEntityTestFactory
 * <pre>
 *  저널 결산 테스트 Entity 생성 팩토리 모듈
 * </pre>
 *
 * @author nichefish 
 */
@UtilityClass
@ActiveProfiles("test")
public class JrnlSumryEntityTestFactory {

    /**
<<<<<<< HEAD
<<<<<<< HEAD
     * 테스트용 저널 결산 Entity 생성
=======
     * 테스트용 저널 일기 Entity 생성
>>>>>>> b9c6a276 (기초적인 CRUD 관련 테스트 코드 작성 중)
=======
     * 테스트용 저널 결산 Entity 생성
>>>>>>> 46bfb69e (기초적인 CRUD 관련 테스트 코드 작성 중)
     */
    public static JrnlSumryEntity create() throws Exception {
        return JrnlSumryEntity.builder()
                .contentType(ContentType.JRNL_SUMRY.key)
                .title("test_title")
                .cn("test_cn")
                .ctgrCd("test_ctgr_cd")
                .build();
    }
}
