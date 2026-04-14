package io.nicheblog.dreamdiary.feature.user.profile.model;

import io.nicheblog.dreamdiary.feature.user.info.model.profile.UserProfileDto;
import lombok.experimental.UtilityClass;
import org.springframework.test.context.ActiveProfiles;

@UtilityClass
@ActiveProfiles("test")
public class UserProfileDtoTestFactory {

    public static UserProfileDto create() {
        return UserProfileDto.builder()
                .brthdy("2000-01-01")
                .proflCn("test_profl_cn")
                .build();
    }
}
