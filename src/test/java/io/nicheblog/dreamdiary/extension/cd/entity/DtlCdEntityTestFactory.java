package io.nicheblog.dreamdiary.extension.cd.entity;

import io.nicheblog.dreamdiary.extension.cd.entity.DtlCdEntity;
import io.nicheblog.dreamdiary.global.TestConstant;
import lombok.experimental.UtilityClass;
import org.springframework.test.context.ActiveProfiles;

/**
 * DtlCdEntityTestFactory
 * <pre>
 *  상세 코드 테스트 Entity 생성 팩토리 모듈
 * </pre>
 *
 * @author nichefish 
 */
@UtilityClass
@ActiveProfiles("test")
public class DtlCdEntityTestFactory {

    /**
     * 상세 코드 Entity 생성
     */
    public static DtlCdEntity create() throws Exception {
        return DtlCdEntity.builder()
                .clCd(TestConstant.TEST_CL_CD)
                .dtlCd(TestConstant.TEST_DTL_CD)
                .dtlCdNm(TestConstant.TEST_DTL_CD_NM)
                .dc(TestConstant.TEST_DC)
                .build();
    }
}
