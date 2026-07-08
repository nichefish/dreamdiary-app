package io.nicheblog.dreamdiary.feature.user.profile.entity;

import java.time.LocalDate;
import lombok.experimental.UtilityClass;
import org.springframework.test.context.ActiveProfiles;

@UtilityClass
@ActiveProfiles("test")
public class UserProfileEntityTestFactory {

    public static UserProfileEntity create() throws Exception {
        return UserProfileEntity.builder()
                .brthdy(LocalDate.parse("2000-01-01"))
                .proflCn("test_profl_cn")
                .build();
    }
}
