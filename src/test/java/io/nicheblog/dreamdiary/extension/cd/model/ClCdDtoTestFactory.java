package io.nicheblog.dreamdiary.extension.cd.model;

import io.nicheblog.dreamdiary.extension.cd.model.ClCdDto;
import io.nicheblog.dreamdiary.global.TestConstant;
import lombok.experimental.UtilityClass;
import org.springframework.test.context.ActiveProfiles;

/**
 * ClCdDtoTestFactory
 * <pre>
 *  분류 코드 테스트 Dto 생성 팩토리 모듈
 * </pre>
 *
 * @author nichefish 
 */
@UtilityClass
@ActiveProfiles("test")
public class ClCdDtoTestFactory {

    /**
     * 테스트용 분류 코드 상세 Dto 생성
     */
    public static ClCdDto createClCdDtlDto() throws Exception {
        return ClCdDto.builder()
                .clCd(TestConstant.TEST_CL_CD)
                .clCdNm(TestConstant.TEST_CL_CD_NM)
                .dc(TestConstant.TEST_DC)
                .build();
    }

    /**
     * 테스트용 분류 코드 상세 Dto 생성
     * (variation 1)
     */
    public static ClCdDto createClCdDtlDto_1() throws Exception {
        return ClCdDto.builder()
                .clCd(TestConstant.TEST_CL_CD_1)
                .clCdNm(TestConstant.TEST_CL_CD_NM_1)
                .dc(TestConstant.TEST_DC_1)
                .build();
    }
}
