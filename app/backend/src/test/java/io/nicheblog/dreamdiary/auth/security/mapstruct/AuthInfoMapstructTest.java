package io.nicheblog.dreamdiary.auth.security.mapstruct;

import io.nicheblog.dreamdiary.auth.security.model.AuthInfo;
import io.nicheblog.dreamdiary.auth.security.model.RoleDto;
import io.nicheblog.dreamdiary.auth.type.Auth;
import io.nicheblog.dreamdiary.feature.user.account.entity.*;
import io.nicheblog.dreamdiary.feature.user.account.model.profile.UserProfileDto;
import io.nicheblog.dreamdiary.feature.user.profile.entity.UserProfileEntity;
import io.nicheblog.dreamdiary.feature.user.profile.entity.UserProfileEntityTestFactory;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.TestConstant;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import io.nicheblog.dreamdiary.infrastructure.code.Code;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AuthInfoMapstructTest
 * <pre>
 *  사용자 인증 정보 MapStruct 매핑 테스트 모듈
 * </pre>
 *
 * @author nichefish
 */
@ActiveProfiles("test")
@Log4j2
class AuthInfoMapstructTest {

    private final AuthInfoMapstruct authInfoMapstruct = AuthInfoMapstruct.INSTANCE;

    /**
     * entity -> dto 검증 :: 기본 속성
     */
    @Test
    void testToDto_checkBasic() throws Exception {
        // Given::
        UserEntity userEntity = UserEntityTestFactory.create();
        userEntity.setId(42);
        userEntity.setProfileImageUrl("test_url");
        userEntity.setUseAllowedIpYn("Y");
        UserAllowedIpEntity aa = UserAllowedIpEntityTestFactory.create("1.1.1.1");
        UserAllowedIpEntity bb = UserAllowedIpEntityTestFactory.create("2.2.2.2");
        userEntity.setAllowedIpList(List.of(aa, bb));
        userEntity.setAllowedIpStrList(userEntity.getAllowedIpList().stream()
                .map(UserAllowedIpEntity::getAllowedIp)
                .collect(Collectors.toList()));

        // When::
        AuthInfo dto = authInfoMapstruct.toDto(userEntity);

        // Then::
        assertNotNull(dto);
        assertEquals(42, dto.getUserId());
        assertEquals(TestConstant.TEST_USER, dto.getUsername());
        assertEquals(TestConstant.TEST_PASSWORD_ENCODED, dto.getPassword());
        assertEquals(TestConstant.TEST_NICK_NM, dto.getNickname());
        assertEquals("test_url", dto.getProfileImageUrl());
        assertEquals("Y", dto.getUseAllowedIpYn());
        assertFalse(CollectionUtils.isEmpty(dto.getAllowedIpStrList()));
        assertEquals(2, dto.getAllowedIpStrList().size());
        assertEquals(dto.getAllowedIpStrList(), List.of("1.1.1.1", "2.2.2.2"));
    }

    /**
     * entity -> dto 검증 :: 어노테이션 매핑
     */
    @Test
    void testToDto_checkMapping() throws Exception {
        // Given::
        UserEntity userEntity = UserEntityTestFactory.create();
        UserRoleEntity aa = UserRoleEntityTestFactory.create(Auth.USER);
        UserRoleEntity bb = UserRoleEntityTestFactory.create(Auth.MNGR);
        userEntity.setUserRoles(List.of(aa, bb));

        UserStateEntity acntStus = UserStateEntity.builder()
                .lockedYn("N")
                .needsPasswordReset("Y")
                .build();
        userEntity.setAcntStus(acntStus);

        // When::
        AuthInfo dto = authInfoMapstruct.toDto(userEntity);

        // Then::
        assertNotNull(dto);
        assertFalse(CollectionUtils.isEmpty(dto.getRoles()));
        assertEquals(2, dto.getRoles().size());
        assertEquals(Code.AUTH_USER, dto.getRoles().get(0).getRoleKey());
        assertEquals(Code.AUTH_MNGR, dto.getRoles().get(1).getRoleKey());
        assertEquals("N", dto.getLockedYn());
        assertEquals("Y", dto.getNeedsPasswordReset());
    }

    /**
     * MapStruct uses + @AfterMapping 으로 roles 가 채워지면 Spring Security GrantedAuthority 도 유효해야 한다.
     */
    @Test
    @DisplayName("toDto 이후 roles·getAuthorities가 user_role(roleInfo) 기준으로 채워진다")
    void testToDto_rolesAndAuthoritiesPopulatedFromUserRoles() throws Exception {
        final UserEntity userEntity = UserEntityTestFactory.create();
        userEntity.setUserRoles(List.of(
                UserRoleEntityTestFactory.create(Auth.USER),
                UserRoleEntityTestFactory.create(Auth.DEV)
        ));

        final AuthInfo dto = authInfoMapstruct.toDto(userEntity);

        assertNotNull(dto.getRoles());
        assertEquals(2, dto.getRoles().size());
        final Set<String> keys = dto.getRoles().stream().map(RoleDto::getRoleKey).collect(Collectors.toSet());
        assertTrue(keys.contains(Code.AUTH_USER));
        assertTrue(keys.contains(Code.AUTH_DEV));

        final List<String> authorities = dto.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        assertTrue(authorities.contains(Constant.ROLE_USER));
        assertTrue(authorities.contains(Constant.ROLE_MNGR), "DEV 역할은 GrantedAuthority 로 ROLE_MNGR 로 매핑된다");
    }

    /**
     * entity -> dto 검증 :: 어노테이션 매핑 (lastLoginAt, passwordChangedAt)
     * 로직이 들어간 부분 테스트 분리
     */
    @Test
    void testToDto_checkMapping_stusDtNotNull() throws Exception {
        // Given::
        UserEntity userEntity = UserEntityTestFactory.create();
        UserStateEntity acntStus = UserStateEntity.builder()
                .lastLoginAt(DateUtils.asLocalDateTime("2000-01-11"))
                .passwordChangedAt(DateUtils.asLocalDateTime("2000-02-22"))
                .build();
        userEntity.setAcntStus(acntStus);

        // When::
        AuthInfo dto = authInfoMapstruct.toDto(userEntity);

        // Then::
        assertNotNull(dto);
        // acntStus - lastLoginAt, passwordChangedAt 대신 createdAt 사용
        assertEquals(dto.getLastLoginAt(), DateUtils.asLocalDateTime("2000-01-11"));
        assertEquals(dto.getPasswordChangedAt(), DateUtils.asLocalDateTime("2000-02-22"));
    }

    /**
     * entity -> dto 검증 :: 어노테이션 매핑 (lastLoginAt, passwordChangedAt)
     * 로직이 들어간 부분 테스트 분리
     */
    @Test
    void testToDto_checkMapping_stusDtNull() throws Exception {
        // Given::
        UserEntity userEntity = UserEntityTestFactory.create();
        UserStateEntity acntStus = UserStateEntity.builder()
                .lastLoginAt(null)
                .passwordChangedAt(null)
                .build();
        userEntity.setAcntStus(acntStus);
        userEntity.setCreatedAt(DateUtils.asLocalDateTime("2000-01-31"));

        // When::
        AuthInfo dto = authInfoMapstruct.toDto(userEntity);

        // Then::
        assertNotNull(dto);
        // acntStus - lastLoginAt, passwordChangedAt 대신 createdAt 사용
        assertEquals(dto.getLastLoginAt(), DateUtils.asLocalDateTime("2000-01-31"));
        assertEquals(dto.getPasswordChangedAt(), DateUtils.asLocalDateTime("2000-01-31"));
    }

    /**
     * entity -> dto 검증 :: 사용자 프로필(user_profl) 검증
     */
    @Test
    void testToDto_checkMapping_userProfile() throws Exception {
        // Given::
        UserProfileEntity userProfileEntity = UserProfileEntityTestFactory.create();
        userProfileEntity.setUserProfileId(1);
        UserEntity userEntity = UserEntityTestFactory.create(userProfileEntity);

        // When::
        AuthInfo dto = authInfoMapstruct.toDto(userEntity);

        // Then::
        assertNotNull(dto);
        // acntStus - lastLoginAt, passwordChangedAt 대신 createdAt 사용
        UserProfileDto userProfile = dto.getProfile();
        assertEquals("2000-01-01", userProfile.getBrthdy());
        assertEquals("test_profl_cn", userProfile.getProflCn());
        assertEquals(1, dto.getUserProfileId());
    }
}
