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
                .groupCode(TestConstant.TEST_GROUP_CODE)
                .groupName(TestConstant.TEST_GROUP_NAME)
                .description(TestConstant.TEST_DC)
                .build();
    }

    public static CodeGroupDto createCodeGroupDto_1() throws Exception {
        return CodeGroupDto.builder()
                .groupCode(TestConstant.TEST_GROUP_CODE_1)
                .groupName(TestConstant.TEST_GROUP_NAME_1)
                .description(TestConstant.TEST_DC_1)
                .build();
    }
}
