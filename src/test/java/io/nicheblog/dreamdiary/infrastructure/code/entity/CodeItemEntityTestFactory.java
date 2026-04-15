package io.nicheblog.dreamdiary.infrastructure.code.entity;

import io.nicheblog.dreamdiary.global.TestConstant;
import lombok.experimental.UtilityClass;
import org.springframework.test.context.ActiveProfiles;

@UtilityClass
@ActiveProfiles("test")
public class CodeItemEntityTestFactory {

    public static CodeItemEntity create() throws Exception {
        return CodeItemEntity.builder()
                .clCd(TestConstant.TEST_CL_CD)
                .dtlCd(TestConstant.TEST_DTL_CD)
                .dtlCdNm(TestConstant.TEST_DTL_CD_NM)
                .description(TestConstant.TEST_DC)
                .build();
    }
}
