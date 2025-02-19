package io.nicheblog.dreamdiary.domain.user.info.mapstuct;

import io.nicheblog.dreamdiary.auth.Auth;
import io.nicheblog.dreamdiary.domain.user.emplym.entity.UserEmplymEntity;
import io.nicheblog.dreamdiary.domain.user.emplym.entity.UserEmplymEntityTestFactory;
import io.nicheblog.dreamdiary.domain.user.info.entity.*;
import io.nicheblog.dreamdiary.domain.user.info.mapstruct.UserMapstruct;
import io.nicheblog.dreamdiary.domain.user.info.model.UserDto;
import io.nicheblog.dreamdiary.domain.user.profl.entity.UserProflEntity;
import io.nicheblog.dreamdiary.domain.user.profl.entity.UserProflEntityTestFactory;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.TestConstant;
import io.nicheblog.dreamdiary.global.intrfc.entity.BaseEntityTestFactoryHelper;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.CollectionUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * UserMapstructToDtoTest
 * <pre>
 *  사용자 계정 신청 Mapstruct 매핑 테스트 모듈 :: toDto 분리
 * </pre>
 *
 * @author nichefish
 */
@ActiveProfiles("test")
@Log4j2
class UserMapstructToDtoTest {

    private final UserMapstruct userMapstruct = UserMapstruct.INSTANCE;

    private UserEntity userEntity;

    /**
     * 각 테스트 시작 전 세팅 초기화.
     * @throws Exception 발생할 수 있는 예외.
     */
    @BeforeEach
    void setUp() throws Exception {
        // 공통적으로 사용할 UserEntity 초기화
        userEntity = UserEntityTestFactory.create();
    }

    /**
     * entity -> dto 검증 :: 기본 속성 매핑 검증
     */
    @Test
    void testToDto_checkBasic() throws Exception {
        // Given::

        // When::
        final UserDto.DTL userDto = userMapstruct.toDto(userEntity);

        // Then::
        assertNotNull(userDto, "변환된 사용자 상세 Dto는 null일 수 없습니다.");
        // 이메일 변환 로직 검증
        final String email = userEntity.getEmail();
        if (StringUtils.isNotEmpty(email)) {
            assertTrue(email.contains("@"), "이메일 형식이 올바르지 않습니다.");
            final String[] emailParts = email.split("@");
            final String expectedEmailId = emailParts[0];
            final String expectedEmailDomain = emailParts.length > 1 ? emailParts[1] : null;
            assertEquals(expectedEmailId, userDto.getEmailId(), "이메일 정보가 제대로 매핑되지 않았습니다.");
            assertEquals(expectedEmailDomain, userDto.getEmailDomain(), "이메일 정보가 제대로 매핑되지 않았습니다.");
        } else {
            assertNull(userDto.getEmailId(), "이메일 ID가 null이어야 합니다.");
            assertNull(userDto.getEmailDomain(), "이메일 도메인이 null이어야 합니다.");
        }
    }

    /**
     * entity -> dto 검증 :: 등록자/수정자 정보 매핑 검증
     */
    @Test
    void testToDto_checkAuditor() throws Exception {
        // Given::
        // 등록자 / 수정자
        BaseEntityTestFactoryHelper.setRegstrInfo(userEntity);
        BaseEntityTestFactoryHelper.setMdfusrInfo(userEntity);

        // When::
        final UserDto.DTL userDto = userMapstruct.toDto(userEntity);

        // Then::
        assertNotNull(userDto, "변환된 사용자 상세 Dto는 null일 수 없습니다.");
        assertEquals(TestConstant.TEST_REGSTR_ID, userDto.getRegstrId(), "등록자 ID가 제대로 매핑되지 않았습니다.");
        assertEquals(TestConstant.TEST_REGSTR_NM, userDto.getRegstrNm(), "등록자 이름이 제대로 매핑되지 않았습니다.");
        assertEquals("2000-01-01 00:00:00", userDto.getRegDt(), "등록일시가 제대로 매핑되지 않았습니다.");
        // 수정자
        assertEquals(TestConstant.TEST_MDFUSR_ID, userDto.getMdfusrId(), "수정자 ID가 제대로 매핑되지 않았습니다.");
        assertEquals(TestConstant.TEST_MDFUSR_NM, userDto.getMdfusrNm(), "수정자 이름이 제대로 매핑되지 않았습니다.");
        assertEquals("2000-01-01 00:00:00", userDto.getMdfDt(), "수정일시가 제대로 매핑되지 않았습니다.");
    }

    /**
     * entity -> dto 검증 :: 권한 매핑 검증
     */
    @Test
    void testToDto_checkAuth() throws Exception {
        // Given::
        // AUTH
        final UserAuthRoleEntity aa = UserAuthRoleEntityTestFactory.create(Auth.USER);
        final UserAuthRoleEntity bb = UserAuthRoleEntityTestFactory.create(Auth.MNGR);
        userEntity.setAuthList(List.of(aa, bb));

        // When::
        final UserDto.DTL userDto = userMapstruct.toDto(userEntity);

        // Then::
        assertNotNull(userDto, "변환된 사용자 상세 Dto는 null일 수 없습니다.");
        // 권한 관련 매핑 검증
        assertFalse(CollectionUtils.isEmpty(userDto.getAuthList()), "변환된 사용자 권한 목록 Dto는 null일 수 없습니다.");
        assertEquals(2, userDto.getAuthList().size(), "사용자 권한 목록 크기가 일치하지 않습니다.");
        assertEquals(Constant.AUTH_USER, userDto.getAuthList().get(0).getAuthCd(), "사용자 권한 목록에서 권한 정보가 제대로 매핑되지 않았습니다.");
        assertEquals(Constant.AUTH_MNGR, userDto.getAuthList().get(1).getAuthCd(), "사용자 권한 목록에서 권한 정보가 제대로 매핑되지 않았습니다.");
    }

    /**
     * entity -> dto 검증 :: 접속 IP 매핑 검증
     */
    @Test
    void testToDto_checkAcsIp() throws Exception {
        // Given::
        userEntity.setUseAcsIpYn("Y");
        final UserAcsIpEntity aa = UserAcsIpEntityTestFactory.create("1.1.1.1");
        final UserAcsIpEntity bb = UserAcsIpEntityTestFactory.create("2.2.2.2");
        userEntity.setAcsIpList(List.of(aa, bb));

        // When::
        final UserDto.DTL userDto = userMapstruct.toDto(userEntity);

        // Then::
        assertNotNull(userDto, "변환된 사용자 상세 Dto는 null일 수 없습니다.");
        // 접속 IP 관련 매핑 검증
        assertEquals(userEntity.getUseAcsIpYn(), userDto.getUseAcsIpYn(), "접속 IP 사용여부가 제대로 매핑되지 않았습니다.");
        assertNotNull(userDto.getAcsIpList(), "변환된 접속 가능 IP 목록 Dto는 null일 수 없습니다.");
        assertEquals(2, userDto.getAcsIpList().size(), "접속 가능 IP 목록 크기가 일치하지 않습니다.");
        assertEquals("1.1.1.1", userDto.getAcsIpList().get(0).getAcsIp(), "접속 가능 IP 목록에서 IP 정보가 제대로 매핑되지 않았습니다.");
        assertEquals("2.2.2.2", userDto.getAcsIpList().get(1).getAcsIp(), "접속 가능 IP 목록에서 IP 정보가 제대로 매핑되지 않았습니다.");
        assertEquals("1.1.1.1, 2.2.2.2", userDto.getAcsIpListStr(), "접속 가능 IP 목록 문자열이 제대로 매핑되지 않았습니다.");
    }

    /**
     * entity -> dto 검증 :: 사용자 프로필 정보 매핑 검증
     */
    @Test
    void testToDto_checkProfl() throws Exception {
        // Given::
        final UserProflEntity userProflEntity = UserProflEntityTestFactory.create();
        userEntity.setProfl(userProflEntity);

        // When::
        final UserDto userDto = userMapstruct.toDto(userEntity);

        // Then::
        assertNotNull(userDto, "변환된 사용자 상세 Dto는 null일 수 없습니다.");
        assertNotNull(userDto.getProfl(), "변환된 사용자 프로필 Dto는 null일 수 없습니다.");
        // 날짜 변환 검증
        assertEquals("2000-01-01", userDto.getProfl().getBrthdy(), "사용자 프로필 생일 정보가 제대로 매핑되지 않았습니다.");
    }

    /**
     * entity -> dto 검증 :: 사용자 인사정보 매핑 검증
     */
    @Test
    void testToDto_checkEmplym() throws Exception {
        // Given::
        final UserEmplymEntity userEmplymEntity = UserEmplymEntityTestFactory.create();
        userEntity.setEmplym(userEmplymEntity);

        // When::
        final UserDto userDto = userMapstruct.toDto(userEntity);

        // Then::
        assertNotNull(userDto, "변환된 사용자 상세 Dto는 null일 수 없습니다.");
        assertNotNull(userDto.getEmplym(), "변환된 사용자 직원정보 Dto는 null일 수 없습니다.");
        // 날짜 변환 검증
        assertEquals("2000-01-01", userDto.getEmplym().getEcnyDt(), "사용자 직원정보 입사일 정보가 제대로 매핑되지 않았습니다.");
        assertEquals("2000-01-01", userDto.getEmplym().getRetireDt(), "사용자 직원정보 퇴사일 정보가 제대로 매핑되지 않았습니다.");
        // 이메일 변환 로직
        final String email = userEmplymEntity.getEmplymEmail();
        if (StringUtils.isNotEmpty(email)) {
            assertTrue(email.contains("@"), "이메일 형식이 올바르지 않습니다.");
            final String[] emailParts = email.split("@");
            final String expectedEmailId = emailParts[0];
            final String expectedEmailDomain = emailParts.length > 1 ? emailParts[1] : null;
            assertEquals(expectedEmailId, userDto.getEmplym().getEmplymEmailId(), "이메일 정보가 제대로 매핑되지 않았습니다.");
            assertEquals(expectedEmailDomain, userDto.getEmplym().getEmplymEmailDomain(), "이메일 정보가 제대로 매핑되지 않았습니다.");
        } else {
            assertNull(userDto.getEmplym().getEmplymEmailId(), "이메일 ID가 null이어야 합니다.");
            assertNull(userDto.getEmplym().getEmplymEmailDomain(), "이메일 도메인이 null이어야 합니다.");
        }
    }

    /* ----- */

    /**
     * entity -> dto 검증 :: 기본 속성 매핑 검증
     */
    @Test
    void testToListDto_checkBasic() throws Exception {
        // Given::

        // When::
        final UserDto.LIST userDto = userMapstruct.toListDto(userEntity);

        // Then::
        assertNotNull(userDto, "변환된 사용자 목록 Dto는 null일 수 없습니다.");
        // 이메일 변환 로직 검증
        // 이메일 변환 로직 검증
        final String email = userEntity.getEmail();
        if (StringUtils.isNotEmpty(email)) {
            assertTrue(email.contains("@"), "이메일 형식이 올바르지 않습니다.");
            final String[] emailParts = email.split("@");
            final String expectedEmailId = emailParts[0];
            final String expectedEmailDomain = emailParts.length > 1 ? emailParts[1] : null;
            assertEquals(expectedEmailId, userDto.getEmailId(), "이메일 정보가 제대로 매핑되지 않았습니다.");
            assertEquals(expectedEmailDomain, userDto.getEmailDomain(), "이메일 정보가 제대로 매핑되지 않았습니다.");
        } else {
            assertNull(userDto.getEmailId(), "이메일 ID가 null이어야 합니다.");
            assertNull(userDto.getEmailDomain(), "이메일 도메인이 null이어야 합니다.");
        }
    }

    /* ----- */

    /**
     * entity -> listDto 검증 :: 등록자/수정자 정보 매핑 검증
     */
    @Test
    void testToListDto_checkAuditor() throws Exception {
        // Given::
        // 등록자 / 수정자
        BaseEntityTestFactoryHelper.setRegstrInfo(userEntity);
        BaseEntityTestFactoryHelper.setMdfusrInfo(userEntity);

        // When::
        final UserDto.LIST userListDto = userMapstruct.toListDto(userEntity);

        // Then::
        assertNotNull(userListDto, "변환된 사용자 목록 Dto는 null일 수 없습니다.");
        assertEquals(TestConstant.TEST_REGSTR_ID, userListDto.getRegstrId(), "등록자 ID가 제대로 매핑되지 않았습니다.");
        assertEquals(TestConstant.TEST_REGSTR_NM, userListDto.getRegstrNm(), "등록자 이름이 제대로 매핑되지 않았습니다.");
        assertEquals("2000-01-01 00:00:00", userListDto.getRegDt(), "등록일시가 제대로 매핑되지 않았습니다.");
        // 수정자
        assertEquals(TestConstant.TEST_MDFUSR_ID, userListDto.getMdfusrId(), "수정자 ID가 제대로 매핑되지 않았습니다.");
        assertEquals(TestConstant.TEST_MDFUSR_NM, userListDto.getMdfusrNm(), "수정자 이름이 제대로 매핑되지 않았습니다.");
        assertEquals("2000-01-01 00:00:00", userListDto.getMdfDt(), "수정일시가 제대로 매핑되지 않았습니다.");
    }

}