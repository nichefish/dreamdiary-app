package io.nicheblog.dreamdiary.infrastructure.code.entity;

import io.nicheblog.dreamdiary.global.TestConstant;
import lombok.experimental.UtilityClass;
import org.springframework.test.context.ActiveProfiles;

@UtilityClass
@ActiveProfiles("test")
public class CodeGroupEntityTestFactory {

    public static CodeGroupEntity create() throws Exception {
        return CodeGroupEntity.builder()
                .clCd(TestConstant.TEST_CL_CD)
                .clCdNm(TestConstant.TEST_CL_CD_NM)
                .description(TestConstant.TEST_DC)
                .build();
    }
}
