package io.nicheblog.dreamdiary.infrastructure.code.model;

import io.nicheblog.dreamdiary.feature.admin.code.model.CodeGroupDto;
import io.nicheblog.dreamdiary.global.TestConstant;
import lombok.experimental.UtilityClass;
import org.springframework.test.context.ActiveProfiles;

@UtilityClass
@ActiveProfiles("test")
public class CodeGroupDtoTestFactory {

    public static CodeGroupDto createCodeGroupDto() throws Exception {
        return CodeGroupDto.builder()
                .clCd(TestConstant.TEST_CL_CD)
                .clCdNm(TestConstant.TEST_CL_CD_NM)
                .description(TestConstant.TEST_DC)
                .build();
    }

    public static CodeGroupDto createCodeGroupDto_1() throws Exception {
        return CodeGroupDto.builder()
                .clCd(TestConstant.TEST_CL_CD_1)
                .clCdNm(TestConstant.TEST_CL_CD_NM_1)
                .description(TestConstant.TEST_DC_1)
                .build();
    }
}
