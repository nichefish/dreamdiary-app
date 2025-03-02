package io.nicheblog.dreamdiary.domain.user.emplym.mapstuct;

import io.nicheblog.dreamdiary.domain.user.emplym.entity.UserEmplymEntity;
import io.nicheblog.dreamdiary.domain.user.emplym.entity.UserEmplymEntityTestFactory;
import io.nicheblog.dreamdiary.domain.user.emplym.mapstruct.UserEmplymMapstruct;
import io.nicheblog.dreamdiary.domain.user.emplym.model.UserEmplymDtoTestFactory;
import io.nicheblog.dreamdiary.domain.user.info.model.emplym.UserEmplymDto;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * UserEmplymMapstructTest
 * <pre>
 *  사용자 인사정보 Mapstruct 매핑 테스트 모듈
 * </pre>
 *
 * @author nichefish
 */
@ActiveProfiles("test")
class UserEmplymMapstructTest {

    private final UserEmplymMapstruct userEmplymMapstruct = UserEmplymMapstruct.INSTANCE;

    /**
     * entity -> dto 검증
     */
    @Test
    void testToDto_checkEmplym() throws Exception {

        // Given::
        final UserEmplymEntity userEmplymEntity = UserEmplymEntityTestFactory.create();

        // When::
        final UserEmplymDto userEmplymDto = userEmplymMapstruct.toDto(userEmplymEntity);

        // Then::
        assertNotNull(userEmplymDto, "변환된 인사정보 Dto는 null일 수 없습니다.");
        // 날짜 변환 체크
        assertEquals("2000-01-01", userEmplymDto.getEcnyDt(), "직원정보 입사일이 제대로 매핑되지 않았습니다.");
        assertEquals("2000-01-01", userEmplymDto.getRetireDt(), "직원정보 퇴사일이 제대로 매핑되지 않았습니다.");
        // 이메일 변환 로직
        final String email = userEmplymEntity.getEmplymEmail();
        if (StringUtils.isNotEmpty(email)) {
            assertTrue(email.contains("@"), "이메일 형식이 올바르지 않습니다.");
            final String[] emailParts = email.split("@");
            final String expectedEmailId = emailParts[0];
            final String expectedEmailDomain = emailParts.length > 1 ? emailParts[1] : null;
            assertEquals(expectedEmailId, userEmplymDto.getEmplymEmailId(), "이메일 정보가 제대로 매핑되지 않았습니다.");
            assertEquals(expectedEmailDomain, userEmplymDto.getEmplymEmailDomain(), "이메일 정보가 제대로 매핑되지 않았습니다.");
        } else {
            assertNull(userEmplymDto.getEmplymEmailId(), "이메일 ID가 null이어야 합니다.");
            assertNull(userEmplymDto.getEmplymEmailDomain(), "이메일 도메인이 null이어야 합니다.");
        }

    }

    /* ----- */

    /**
     * dto -> entity 변환 검증
     */
    @Test
    void testToEntity_checkBasic() throws Exception {
        // Given::
        final UserEmplymDto userEmplymDto = UserEmplymDtoTestFactory.create();

        // When::
        final UserEmplymEntity userEmplymEntity = userEmplymMapstruct.toEntity(userEmplymDto);

        // Then::
        assertNotNull(userEmplymEntity, "변환된 인사정보 Entity는 null일 수 없습니다.");
        // 날짜 변환 체크
        assertEquals(DateUtils.asDate("2000-01-01"), userEmplymEntity.getEcnyDt(), "직원정보 입사일 정보가 제대로 매핑되지 않았습니다.");
        assertEquals(DateUtils.asDate("2000-01-01"), userEmplymEntity.getRetireDt(), "직원정보 퇴사일 정보가 제대로 매핑되지 않았습니다.");
        // 이메일 변환 로직
        assertEquals(userEmplymEntity.getEmplymEmail(), userEmplymDto.getEmplymEmailId() + "@" + userEmplymDto.getEmplymEmailDomain());
    }

}