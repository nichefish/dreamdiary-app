package io.nicheblog.dreamdiary.extension.cd.model;

import io.nicheblog.dreamdiary.extension.cd.model.DtlCdDto;
import io.nicheblog.dreamdiary.global.TestConstant;
import lombok.experimental.UtilityClass;
import org.springframework.test.context.ActiveProfiles;

/**
 * DtlCdDtoTestFactory
 * <pre>
 *  상세 코드 테스트 Dto 생성 팩토리 모듈
 * </pre>
 *
 * @author nichefish 
 */
@UtilityClass
@ActiveProfiles("test")
public class DtlCdDtoTestFactory {

    /**
     * 상세 코드 상세 Dto 생성
     */
    public static DtlCdDto createDtlDto() throws Exception {
        return DtlCdDto.builder()
                .clCd(TestConstant.TEST_CL_CD)
                .dtlCd(TestConstant.TEST_DTL_CD)
                .dtlCdNm(TestConstant.TEST_DTL_CD_NM)
                .dc(TestConstant.TEST_DC)
                .build();
    }
}
