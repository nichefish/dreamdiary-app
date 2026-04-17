package io.nicheblog.dreamdiary.feature.user.reqst.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.nicheblog.dreamdiary.feature.user.info.model.emplym.UserEmplymDto;
import io.nicheblog.dreamdiary.feature.user.info.model.profile.UserProfileDto;
import io.nicheblog.dreamdiary.global.TestConstant;
import io.nicheblog.dreamdiary.infrastructure.code.Code;
import lombok.experimental.UtilityClass;
import org.springframework.test.context.ActiveProfiles;

/**
 * UserReqstDtoTestFactory
 * <pre>
 *  사용자 계정 신청 테스트 Dto 생성 팩토리 모듈
 * </pre>
 * TODO: 케이스별로 생성 로직 세분화
 * 
 * @author nichefish 
 */
@UtilityClass
@ActiveProfiles("test")
public class UserReqstDtoTestFactory {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 테스트용 사용자 신청 정보 Dto 객체 생성
     */
    public static UserReqstDto create() {
        // 객체 생성
        return UserReqstDto.builder()
                .username(TestConstant.TEST_USER)
                .password("test_password")
                // 권한:: 운영 로직에선 변환 전 서비스단에서 할당하여 넣어줌.
                .roleKeysStr(Code.AUTH_USER)
                .nickname("test_nickname")
                .emailId("test_email_id")
                .emailDomain("test_email_domain")
                .phoneNumber("010-0101-0101")
                .content("test_cn")
                .build();
    }

    /**
     * 테스트용 사용자 신청 정보 Dto 객체 생성
     */
    public static UserReqstDto create(UserProfileDto profile) {
        // 객체 생성
        UserReqstDto dto = create();
        dto.setProfile(profile);
        return dto;
    }

    /**
     * 테스트용 사용자 신청 정보 Dto 객체 생성
     */
    public static UserReqstDto create(UserEmplymDto emplym) {
        // 객체 생성
        UserReqstDto dto = create();
        dto.setEmplym(emplym);
        return dto;
    }

    /**
     * 테스트용 사용자 신청 정보 Dto 객체 생성
     */
    public static UserReqstDto create(UserProfileDto profile, UserEmplymDto emplym) {
        // 객체 생성
        UserReqstDto dto = create();
        dto.setProfile(profile);
        dto.setEmplym(emplym);
        return dto;
    }

    /**
     * 사용자 계정 신청 JSON 문자열 생성
     */
    public static String createJson() throws JsonProcessingException {
        UserReqstDto userReqstDto = create();
        return objectMapper.writeValueAsString(userReqstDto);
    }
}
