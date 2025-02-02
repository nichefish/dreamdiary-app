package io.nicheblog.dreamdiary.global._common.auth.mapstruct;

import io.nicheblog.dreamdiary.auth.Auth;
import io.nicheblog.dreamdiary.auth.mapstruct.AuthInfoMapstruct;
import io.nicheblog.dreamdiary.auth.model.AuthInfo;
import io.nicheblog.dreamdiary.domain.user.info.entity.*;
import io.nicheblog.dreamdiary.domain.user.info.model.profl.UserProflDto;
import io.nicheblog.dreamdiary.domain.user.profl.entity.UserProflEntity;
import io.nicheblog.dreamdiary.domain.user.profl.entity.UserProflEntityTestFactory;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.TestConstant;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.CollectionUtils;

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
        // 접속IP : Entity 로딩시 AcsIpList -> AcsIpStrList 변환됨. (@Transient)
        userEntity.setUseAcsIpYn("Y");
        UserAcsIpEntity aa = UserAcsIpEntityTestFactory.create("1.1.1.1");
        UserAcsIpEntity bb = UserAcsIpEntityTestFactory.create("2.2.2.2");
        userEntity.setAcsIpList(List.of(aa, bb));
        userEntity.setAcsIpStrList(userEntity.getAcsIpList().stream()
                .map(UserAcsIpEntity::getAcsIp)
                .collect(Collectors.toList()));

        // When::
        AuthInfo dto = authInfoMapstruct.toDto(userEntity);

        // Then::
        assertNotNull(dto);
        assertEquals(TestConstant.TEST_USER, dto.getUserId());
        assertEquals(TestConstant.TEST_PASSWORD_ENCODED, dto.getPassword());
        assertEquals(TestConstant.TEST_NICK_NM, dto.getNickNm());
        assertEquals("test_url", dto.getProflImgUrl());
        // 접속 IP
        assertEquals("Y", dto.getUseAcsIpYn());
        assertFalse(CollectionUtils.isEmpty(dto.getAcsIpStrList()));
        assertEquals(2, dto.getAcsIpStrList().size());
        assertEquals(dto.getAcsIpStrList(), List.of("1.1.1.1", "2.2.2.2"));
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
        assertEquals(Constant.AUTH_USER, dto.getAuthList().get(0).getAuthCd());
        assertEquals(Constant.AUTH_MNGR, dto.getAuthList().get(1).getAuthCd());
        // acntStus
        assertEquals("N", dto.getLockedYn());
        assertEquals("Y", dto.getNeedsPwReset());
        assertEquals("N", dto.getCfYn());
    }

    /**
     * entity -> dto 검증 :: 어노테이션 매핑 (lstLgnDt, pwChgDt)
     * 로직이 들어간 부분 테스트 분리
     */
    @Test
    void testToDto_checkMapping_stusDtNotNull() throws Exception {
        // Given::
        UserEntity userEntity = UserEntityTestFactory.create();
        // acntStus - lstLgnDt, pwChgDt = null
        UserStusEmbed acntStus = UserStusEmbed.builder()
                .lstLgnDt(DateUtils.asDate("2000-01-11"))
                .pwChgDt(DateUtils.asDate("2000-02-22"))
                .build();
        userEntity.setAcntStus(acntStus);

        // When::
        AuthInfo dto = authInfoMapstruct.toDto(userEntity);

        // Then::
        assertNotNull(dto);
        // acntStus - lstLgnDt, pwChgDt 대신 regDt 사용
        assertEquals(dto.getLstLgnDt(), DateUtils.asDate("2000-01-11"));
        assertEquals(dto.getPwChgDt(), DateUtils.asDate("2000-02-22"));
    }

    /**
     * entity -> dto 검증 :: 어노테이션 매핑 (lstLgnDt, pwChgDt)
     * 로직이 들어간 부분 테스트 분리
     */
    @Test
    void testToDto_checkMapping_stusDtNull() throws Exception {
        // Given::
        UserEntity userEntity = UserEntityTestFactory.create();
        // acntStus - lstLgnDt, pwChgDt = null
        UserStusEmbed acntStus = UserStusEmbed.builder()
                .lstLgnDt(null)
                .pwChgDt(null)
                .build();
        userEntity.setAcntStus(acntStus);
        userEntity.setRegDt(DateUtils.asDate("2000-01-31"));

        // When::
        AuthInfo dto = authInfoMapstruct.toDto(userEntity);

        // Then::
        assertNotNull(dto);
        // acntStus - lstLgnDt, pwChgDt 대신 regDt 사용
        assertEquals(dto.getLstLgnDt(), DateUtils.asDate("2000-01-31"));
        assertEquals(dto.getPwChgDt(), DateUtils.asDate("2000-01-31"));
    }

    /**
     * entity -> dto 검증 :: 사용자 프로필(user_profl) 검증
     */
    @Test
    void testToDto_checkMapping_userProfl() throws Exception {
        // Given::
        UserProflEntity userProflEntity = UserProflEntityTestFactory.create();
        userProflEntity.setUserProflNo(1);
        UserEntity userEntity = UserEntityTestFactory.create(userProflEntity);

        // When::
        AuthInfo dto = authInfoMapstruct.toDto(userEntity);

        // Then::
        assertNotNull(dto);
        // acntStus - lstLgnDt, pwChgDt 대신 regDt 사용
        UserProflDto userProfl = dto.getProfl();
        assertEquals("2000-01-01", userProfl.getBrthdy());
        assertEquals("test_profl_cn", userProfl.getProflCn());
        assertEquals(1, dto.getUserProflNo());
    }

}