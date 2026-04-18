package io.nicheblog.dreamdiary.infrastructure.code.entity;

import io.nicheblog.dreamdiary.global.TestConstant;
import lombok.experimental.UtilityClass;
import org.springframework.test.context.ActiveProfiles;

@UtilityClass
@ActiveProfiles("test")
public class CodeItemEntityTestFactory {

    public static CodeItemEntity create() throws Exception {
        return CodeItemEntity.builder()
                .groupCode(TestConstant.TEST_GROUP_CODE)
                .code(TestConstant.TEST_CODE)
                .codeName(TestConstant.TEST_CODE_NAME)
                .description(TestConstant.TEST_DC)
                .build();
    }
}
