package io.nicheblog.dreamdiary.feature.user.account.mapstuct;

import io.nicheblog.dreamdiary.auth.type.Auth;
import io.nicheblog.dreamdiary.feature.user.account.entity.UserAllowedIpEntity;
import io.nicheblog.dreamdiary.feature.user.account.entity.UserEntity;
import io.nicheblog.dreamdiary.feature.user.account.mapstruct.UserMapstruct;
import io.nicheblog.dreamdiary.feature.user.account.model.UserDto;
import io.nicheblog.dreamdiary.feature.user.account.model.UserDtoTestFactory;
import io.nicheblog.dreamdiary.feature.user.account.model.UserRoleDto;
import io.nicheblog.dreamdiary.feature.user.account.model.UserRoleDtoTestFactory;
import io.nicheblog.dreamdiary.feature.user.account.model.emplym.UserEmplymDto;
import io.nicheblog.dreamdiary.feature.user.account.model.profile.UserProfileDto;
import io.nicheblog.dreamdiary.feature.user.emplym.entity.UserEmplymEntity;
import io.nicheblog.dreamdiary.feature.user.emplym.model.UserEmplymDtoTestFactory;
import io.nicheblog.dreamdiary.feature.user.profile.entity.UserProfileEntity;
import io.nicheblog.dreamdiary.feature.user.profile.model.UserProfileDtoTestFactory;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import io.nicheblog.dreamdiary.infrastructure.code.Code;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * UserMapstructToEntityTest
 * <pre>
 *  사용자 계정 신청 Mapstruct 매핑 테스트 모듈 :: toEntity 분리
 *  TODO: updateFromDto
 * </pre>
 *
 * @author nichefish
 */
@ActiveProfiles("test")
@Log4j2
class UserMapstructToEntityTest {

    private final UserMapstruct userMapstruct = UserMapstruct.INSTANCE;

    private UserDto userDto;

    /**
     * 각 테스트 시작 전 세팅 초기화.
     */
    @BeforeEach
    void setUp() throws Exception {
        // 공통적으로 사용할 UserDto 초기화
        userDto = UserDtoTestFactory.create();
    }

    /**
     * dto -> entity 변환 검증 :: 기본 속성 검증
     */
    @Test
    void testToEntity_checkBasic() throws Exception {
        // Given::

        // When::
        final UserEntity entity = userMapstruct.toEntity(userDto);

        // Then::
        assertNotNull(entity, "변환된 사용자 Entity는 null일 수 없습니다.");
        // 이메일 변환 로직
        assertEquals(userDto.getEmailId() + "@" + userDto.getEmailDomain(), entity.getEmail(), "이메일이 올바르게 매핑되지 않았습니다.");
    }

    /**
     * dto -> entity 변환 검증:: 권한
     */
    @Test
    void testToEntity_checkAuth() throws Exception {
        // Given::
        // AUTH
        final UserRoleDto aa = UserRoleDtoTestFactory.create(Auth.USER);
        final UserRoleDto bb = UserRoleDtoTestFactory.create(Auth.MNGR);
        userDto.setUserRoles(List.of(aa, bb));

        // When::
        final UserEntity entity = userMapstruct.toEntity(userDto);

        // Then::
        assertNotNull(entity, "변환된 사용자 Entity는 null일 수 없습니다.");
        // 권한 관련 매핑 검증
        assertNotNull(entity.getUserRoles(), "변환된 사용자 역할 목록은 null일 수 없습니다.");
        assertEquals(2, entity.getUserRoles().size(), "사용자 역할 목록 크기가 일치하지 않습니다.");
        assertEquals(Code.AUTH_USER, entity.getUserRoles().get(0).getRoleKey(), "사용자 역할 목록에서 역할 정보가 제대로 매핑되지 않았습니다.");
        assertEquals(Code.AUTH_MNGR, entity.getUserRoles().get(1).getRoleKey(), "사용자 역할 목록에서 역할 정보가 제대로 매핑되지 않았습니다.");
    }

    /**
     * dto -> entity 변환 검증:: 접속 IP
     */
    @Test
    void testToEntity_checkAllowedIp() throws Exception {
        // Given::
        // ACS_IP
        userDto.setUseAllowedIpYn("Y");
        userDto.setAllowedIpListStr("[{\"value\":\"1.1.1.1\"},{\"value\":\"2.2.2.2\"}]");

        // When::
        final UserEntity entity = userMapstruct.toEntity(userDto);

        // Then::
        assertNotNull(entity, "변환된 사용자 Entity는 null일 수 없습니다.");
        // 접속 IP 관련
        assertEquals(userDto.getUseAllowedIpYn(), entity.getUseAllowedIpYn(), "접속 IP 사용여부가 제대로 매핑되지 않았습니다.");
        final List<UserAllowedIpEntity> allowedIpEntityList = entity.getAllowedIpList();
        assertNotNull(allowedIpEntityList, "변환된 접속 가능 IP 목록 Dto는 null일 수 없습니다.");
        assertEquals(2, allowedIpEntityList.size(), "접속 가능 IP 목록 크기가 일치하지 않습니다.");
        assertEquals("1.1.1.1", allowedIpEntityList.get(0).getAllowedIp(), "접속 가능 IP 목록에서 IP 정보가 제대로 매핑되지 않았습니다.");
        assertEquals("2.2.2.2", allowedIpEntityList.get(1).getAllowedIp(), "접속 가능 IP 목록에서 IP 정보가 제대로 매핑되지 않았습니다.");
    }

    /**
     * dto -> entity 변환 검증:: 사용자 프로필 정보
     */
    @Test
    void testToEntity_checkProfl() throws Exception {
        // Given::
        final UserProfileDto userProfileDto = UserProfileDtoTestFactory.create();
        userDto.setProfile(userProfileDto);

        // When::
        final UserEntity entity = userMapstruct.toEntity(userDto);

        // Then::
        assertNotNull(entity, "변환된 사용자 Entity는 null일 수 없습니다.");
        final UserProfileEntity userProfileEntity = entity.getProfile();
        assertNotNull(userProfileEntity, "변환된 사용자 프로필 정보 Entity는 null일 수 없습니다.");
        // 날짜 변환 체크
        assertEquals(DateUtils.asLocalDate("2000-01-01"), userProfileEntity.getBrthdy(), "사용자 프로필 정보 생일 정보가 제대로 매핑되지 않았습니다.");
    }

    /**
     * dto -> entity 변환 검증:: 사용자 인사정보
     */
    @Test
    void testToEntity_checkEmplym() throws Exception {
        // Given::
        final UserEmplymDto userEmplymDto = UserEmplymDtoTestFactory.create();
        userDto.setEmplym(userEmplymDto);

        // When::
        final UserEntity entity = userMapstruct.toEntity(userDto);

        // Then::
        assertNotNull(entity, "변환된 사용자 Entity는 null일 수 없습니다.");
        UserEmplymEntity userEmplymEntity = entity.getEmplym();
        assertNotNull(userEmplymEntity, "변환된 사용자 직원정보 Entity는 null일 수 없습니다.");
        // 날짜 변환 체크
        assertEquals(DateUtils.asLocalDate("2000-01-01"), userEmplymEntity.getEcnyDt(), "사용자 직원정보 입사일 정보가 제대로 매핑되지 않았습니다.");
        assertEquals(DateUtils.asLocalDateTime("2000-01-01"), userEmplymEntity.getRetireDt(), "사용자 직원정보 퇴사일 정보가 제대로 매핑되지 않았습니다.");
        // 이메일 변환 로직
        assertEquals(userEmplymDto.getEmplymEmailId() + "@" + userEmplymDto.getEmplymEmailDomain(), userEmplymEntity.getEmplymEmail(), "이메일이 올바르게 매핑되지 않았습니다.");
    }

    /* ----- */

    /**
     * updateFromDto 검증 :: 기본 속성
     * TODO
     */
    @Test
    void testUpdateFromDto_checkBasic() throws Exception {
        //
    }
}
