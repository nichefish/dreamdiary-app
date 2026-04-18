package io.nicheblog.dreamdiary.infrastructure.code.entity;

import io.nicheblog.dreamdiary.global.TestConstant;
import lombok.experimental.UtilityClass;
import org.springframework.test.context.ActiveProfiles;

@UtilityClass
@ActiveProfiles("test")
public class CodeGroupEntityTestFactory {

    public static CodeGroupEntity create() throws Exception {
        return CodeGroupEntity.builder()
                .groupCode(TestConstant.TEST_GROUP_CODE)
                .groupName(TestConstant.TEST_GROUP_NAME)
                .description(TestConstant.TEST_DC)
                .build();
    }
}
