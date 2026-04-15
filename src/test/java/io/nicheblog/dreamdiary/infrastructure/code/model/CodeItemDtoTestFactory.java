package io.nicheblog.dreamdiary.infrastructure.code.model;

import io.nicheblog.dreamdiary.feature.admin.code.model.CodeItemDto;
import io.nicheblog.dreamdiary.global.TestConstant;
import lombok.experimental.UtilityClass;
import org.springframework.test.context.ActiveProfiles;

@UtilityClass
@ActiveProfiles("test")
public class CodeItemDtoTestFactory {

    public static CodeItemDto createCodeItemDto() throws Exception {
        return CodeItemDto.builder()
                .clCd(TestConstant.TEST_CL_CD)
                .dtlCd(TestConstant.TEST_DTL_CD)
                .dtlCdNm(TestConstant.TEST_DTL_CD_NM)
                .description(TestConstant.TEST_DC)
                .build();
    }
}
