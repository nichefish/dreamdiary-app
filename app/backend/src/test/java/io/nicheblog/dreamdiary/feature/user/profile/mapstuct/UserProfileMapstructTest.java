package io.nicheblog.dreamdiary.feature.user.profile.mapstuct;

import io.nicheblog.dreamdiary.feature.user.account.model.profile.UserProfileDto;
import io.nicheblog.dreamdiary.feature.user.profile.entity.UserProfileEntity;
import io.nicheblog.dreamdiary.feature.user.profile.entity.UserProfileEntityTestFactory;
import io.nicheblog.dreamdiary.feature.user.profile.mapstruct.UserProfileMapstruct;
import io.nicheblog.dreamdiary.feature.user.profile.model.UserProfileDtoTestFactory;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("test")
class UserProfileMapstructTest {

    private final UserProfileMapstruct userProfileMapstruct = UserProfileMapstruct.INSTANCE;

    @Test
    void testToDto_checkProfile() throws Exception {
        final UserProfileEntity userProfileEntity = UserProfileEntityTestFactory.create();
        final UserProfileDto userProfileDto = userProfileMapstruct.toDto(userProfileEntity);
        assertNotNull(userProfileDto, "변환된 프로필 정보 Dto는 null일 수 없습니다.");
        assertEquals("2000-01-01", userProfileDto.getBrthdy(), "프로필 생일 정보가 제대로 매핑되지 않았습니다.");
    }

    @Test
    void testToEntity_checkBasic() throws Exception {
        final UserProfileDto userProfileDto = UserProfileDtoTestFactory.create();
        final UserProfileEntity userProfileEntity = userProfileMapstruct.toEntity(userProfileDto);
        assertNotNull(userProfileEntity, "변환된 프로필 정보 Entity는 null일 수 없습니다.");
        assertEquals(DateUtils.asLocalDate("2000-01-01"), userProfileEntity.getBrthdy(), "프로필 생일 정보가 제대로 매핑되지 않았습니다.");
    }
}
