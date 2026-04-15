package io.nicheblog.dreamdiary.auth.security.mapstruct;

import io.nicheblog.dreamdiary.auth.security.model.AuthInfo;
import io.nicheblog.dreamdiary.auth.type.Auth;
import io.nicheblog.dreamdiary.feature.user.info.entity.*;
import io.nicheblog.dreamdiary.feature.user.info.model.profile.UserProfileDto;
import io.nicheblog.dreamdiary.feature.user.profile.entity.UserProfileEntity;
import io.nicheblog.dreamdiary.feature.user.profile.entity.UserProfileEntityTestFactory;
import io.nicheblog.dreamdiary.global.TestConstant;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import io.nicheblog.dreamdiary.infrastructure.code.Code;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
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
        userEntity.setProflImgUrl("test_url");
        // 접속IP : Entity 로딩시 AllowedIpList -> AllowedIpStrList 변환됨. (@Transient)
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
        assertEquals(TestConstant.TEST_USER, dto.getUsername());
        assertEquals(TestConstant.TEST_PASSWORD_ENCODED, dto.getPassword());
        assertEquals(TestConstant.TEST_NICK_NM, dto.getNickNm());
        assertEquals("test_url", dto.getProflImgUrl());
        // 접속 IP
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
        // authList
        UserAuthRoleEntity aa = UserAuthRoleEntityTestFactory.create(Auth.USER);
        UserAuthRoleEntity bb = UserAuthRoleEntityTestFactory.create(Auth.MNGR);
        userEntity.setAuthList(List.of(aa, bb));
        // acntStus
        UserStusEmbed acntStus = UserStusEmbed.builder()
                .lockedYn("N")
                .needsPwReset("Y")
                .cfYn("N")
                .build();
        userEntity.setAcntStus(acntStus);

        // When::
        AuthInfo dto = authInfoMapstruct.toDto(userEntity);

        // Then::
        assertNotNull(dto);
        // authList
        assertFalse(CollectionUtils.isEmpty(dto.getAuthList()));
        assertEquals(2, dto.getAuthList().size());
        assertEquals(Code.AUTH_USER, dto.getAuthList().get(0).getAuthCd());
        assertEquals(Code.AUTH_MNGR, dto.getAuthList().get(1).getAuthCd());
        // acntStus
        assertEquals("N", dto.getLockedYn());
        assertEquals("Y", dto.getNeedsPwReset());
        assertEquals("N", dto.getCfYn());
    }

    /**
     * entity -> dto 검증 :: 어노테이션 매핑 (lastLoginAt, passwordChangedAt)
     * 로직이 들어간 부분 테스트 분리
     */
    @Test
    void testToDto_checkMapping_stusDtNotNull() throws Exception {
        // Given::
        UserEntity userEntity = UserEntityTestFactory.create();
        // acntStus - lastLoginAt, passwordChangedAt = null
        UserStusEmbed acntStus = UserStusEmbed.builder()
                .lastLoginAt(DateUtils.asDate("2000-01-11"))
                .passwordChangedAt(DateUtils.asDate("2000-02-22"))
                .build();
        userEntity.setAcntStus(acntStus);

        // When::
        AuthInfo dto = authInfoMapstruct.toDto(userEntity);

        // Then::
        assertNotNull(dto);
        // acntStus - lastLoginAt, passwordChangedAt 대신 createdAt 사용
        assertEquals(dto.getLastLoginAt(), DateUtils.asDate("2000-01-11"));
        assertEquals(dto.getPasswordChangedAt(), DateUtils.asDate("2000-02-22"));
    }

    /**
     * entity -> dto 검증 :: 어노테이션 매핑 (lastLoginAt, passwordChangedAt)
     * 로직이 들어간 부분 테스트 분리
     */
    @Test
    void testToDto_checkMapping_stusDtNull() throws Exception {
        // Given::
        UserEntity userEntity = UserEntityTestFactory.create();
        // acntStus - lastLoginAt, passwordChangedAt = null
        UserStusEmbed acntStus = UserStusEmbed.builder()
                .lastLoginAt(null)
                .passwordChangedAt(null)
                .build();
        userEntity.setAcntStus(acntStus);
        userEntity.setCreatedAt(DateUtils.asDate("2000-01-31"));

        // When::
        AuthInfo dto = authInfoMapstruct.toDto(userEntity);

        // Then::
        assertNotNull(dto);
        // acntStus - lastLoginAt, passwordChangedAt 대신 createdAt 사용
        assertEquals(dto.getLastLoginAt(), DateUtils.asDate("2000-01-31"));
        assertEquals(dto.getPasswordChangedAt(), DateUtils.asDate("2000-01-31"));
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
