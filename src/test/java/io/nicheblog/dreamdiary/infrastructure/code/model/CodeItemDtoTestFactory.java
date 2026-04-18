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
                .groupCode(TestConstant.TEST_GROUP_CODE)
                .code(TestConstant.TEST_CODE)
                .codeName(TestConstant.TEST_CODE_NAME)
                .description(TestConstant.TEST_DC)
                .build();
    }
}
