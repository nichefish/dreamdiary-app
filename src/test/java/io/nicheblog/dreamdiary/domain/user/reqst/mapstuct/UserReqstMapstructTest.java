package io.nicheblog.dreamdiary.domain.user.reqst.mapstuct;

import io.nicheblog.dreamdiary.domain.user.emplym.entity.UserEmplymEntity;
import io.nicheblog.dreamdiary.domain.user.emplym.model.UserEmplymDtoTestFactory;
import io.nicheblog.dreamdiary.domain.user.info.entity.UserAcsIpEntity;
import io.nicheblog.dreamdiary.domain.user.info.entity.UserEntity;
import io.nicheblog.dreamdiary.domain.user.info.model.emplym.UserEmplymDto;
import io.nicheblog.dreamdiary.domain.user.info.model.profl.UserProflDto;
import io.nicheblog.dreamdiary.domain.user.profl.entity.UserProflEntity;
import io.nicheblog.dreamdiary.domain.user.profl.model.UserProflDtoTestFactory;
import io.nicheblog.dreamdiary.domain.user.reqst.mapstruct.UserReqstMapstruct;
import io.nicheblog.dreamdiary.domain.user.reqst.model.UserReqstDto;
import io.nicheblog.dreamdiary.domain.user.reqst.model.UserReqstDtoTestFactory;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * UserReqstMapstructTest
 * <pre>
 *  사용자 계정 신청 Mapstruct 매핑 테스트 모듈
 * </pre>
 * (toDto는 로직상 존재하지만 여기서는 세세한 매핑이 중요하지 않음. UserMapstructTest에서 테스트 예정)
 *
 * @author nichefish
 */
@ActiveProfiles("test")
class UserReqstMapstructTest {

    private final UserReqstMapstruct userReqstMapstruct = UserReqstMapstruct.INSTANCE;

    private UserReqstDto userReqstDto;
    /**
     * 각 테스트 시작 전 세팅 초기화.
     * @throws Exception 발생할 수 있는 예외.
     */
    @BeforeEach
    void setUp() throws Exception {
        // 공통적으로 사용할 UserReqstDto 초기화
        userReqstDto = UserReqstDtoTestFactory.create();
    }

    /**
     * dto -> entity 변환 검증 :: 기본 속성 검증
     */
    @Test
    void testToEntity_checkBasic() throws Exception {
        // Given::

        // When::
        final UserEntity entity = userReqstMapstruct.toEntity(userReqstDto);

        // Then::
        assertNotNull(entity, "변환된 사용자 계정 신청 Entity는 null일 수 없습니다.");
        // 이메일 변환 로직
        assertEquals(entity.getEmail(), userReqstDto.getEmailId() + "@" + userReqstDto.getEmailDomain(), "이메일이 올바르게 매핑되지 않았습니다.");
    }

    /**
     * dto -> entity 변환 검증 :: 접속 IP 관련
     */
    @Test
    void testToEntity_checkAcsIp() throws Exception {
        // Given::
        // 접속 IP 세팅
        userReqstDto.setUseAcsIpYn("Y");
        userReqstDto.setAcsIpListStr("[{\"value\":\"1.1.1.1\"},{\"value\":\"2.2.2.2\"}]");

        // When::
        final UserEntity entity = userReqstMapstruct.toEntity(userReqstDto);

        // Then::
        assertNotNull(entity, "변환된 사용자 계정 신청 Entity는 null일 수 없습니다.");
        // 접속 IP 관련
        assertEquals(userReqstDto.getUseAcsIpYn(),  entity.getUseAcsIpYn(), "접속 IP 사용여부가 제대로 매핑되지 않았습니다.");
        List<UserAcsIpEntity> acsIpEntityList = entity.getAcsIpList();
        assertNotNull(acsIpEntityList, "변환된 접속 가능 IP 목록 Dto는 null일 수 없습니다.");
        assertEquals(2, acsIpEntityList.size(), "접속 가능 IP 목록 크기가 일치하지 않습니다.");
        assertEquals("1.1.1.1", acsIpEntityList.get(0).getAcsIp(), "접속 가능 IP 목록에서 IP 정보가 제대로 매핑되지 않았습니다.");
        assertEquals("2.2.2.2", acsIpEntityList.get(1).getAcsIp(), "접속 가능 IP 목록에서 IP 정보가 제대로 매핑되지 않았습니다.");
    }

    /** 
     * dto -> entity 변환 검증 :: 사용자 프로필 정보 관련
     */
    @Test
    void testToEntity_checkProfl() throws Exception {
        // Given::
        final UserProflDto userProflDto = UserProflDtoTestFactory.create();
        userReqstDto.setProfl(userProflDto);

        // When::
        final UserEntity entity = userReqstMapstruct.toEntity(userReqstDto);

        // Then::
        assertNotNull(entity, "변환된 사용자 계정 신청 Entity는 null일 수 없습니다.");
        UserProflEntity userProflEntity = entity.getProfl();
        assertNotNull(userProflEntity, "변환된 사용자 프로필 정보 Entity는 null일 수 없습니다.");
        // 날짜 변환 체크
        assertEquals(DateUtils.asDate("2000-01-01"), userProflEntity.getBrthdy(), "사용자 프로필 정보 생일 정보가 제대로 매핑되지 않았습니다.");
    }

    /**
     * dto -> entity 변환 검증 :: 사용자 인사정보 관련
     */
    @Test
    void testToEntity_checkEmplym() throws Exception {
        // Given::
        final UserEmplymDto userEmplymDto = UserEmplymDtoTestFactory.create();
        userReqstDto.setEmplym(userEmplymDto);

        // When::
        final UserEntity entity = userReqstMapstruct.toEntity(userReqstDto);

        // Then::
        assertNotNull(entity, "변환된 사용자 계정 신청 Entity는 null일 수 없습니다.");
        UserEmplymEntity userEmplymEntity = entity.getEmplym();
        assertNotNull(userEmplymEntity, "변환된 사용자 직원정보 Entity는 null일 수 없습니다.");
        // 날짜 변환 체크
        assertEquals(DateUtils.asDate("2000-01-01"), userEmplymEntity.getEcnyDt(), "사용자 직원정보 입사일 정보가 제대로 매핑되지 않았습니다.");
        assertEquals(DateUtils.asDate("2000-01-01"), userEmplymEntity.getRetireDt(), "사용자 직원정보 퇴사일 정보가 제대로 매핑되지 않았습니다.");
        // 이메일 변환 로직
        assertEquals(userEmplymDto.getEmplymEmailId() + "@" + userEmplymDto.getEmplymEmailDomain(), userEmplymEntity.getEmplymEmail(), "이메일이 올바르게 매핑되지 않았습니다.");
    }
}