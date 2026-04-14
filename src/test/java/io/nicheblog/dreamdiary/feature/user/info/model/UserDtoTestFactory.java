package io.nicheblog.dreamdiary.feature.user.info.model;

import io.nicheblog.dreamdiary.feature.user.info.model.emplym.UserEmplymDto;
import io.nicheblog.dreamdiary.feature.user.info.model.profile.UserProfileDto;
import io.nicheblog.dreamdiary.global.TestConstant;
import lombok.experimental.UtilityClass;
import org.springframework.test.context.ActiveProfiles;

/**
 * UserDtoTestFactory
 * <pre>
 *  사용자 계정 테스트 Dto 생성 팩토리 모듈
 * </pre>
 * TODO: 케이스별로 생성 로직 세분화
 * 
 * @author nichefish 
 */
@UtilityClass
@ActiveProfiles("test")
public class UserDtoTestFactory {

    /**
     * 테스트용 사용자 Dto (상세) 생성
     */
    public static UserDto create() {
        // 객체 생성
        return UserDto.builder()
                .username(TestConstant.TEST_USER)
                .password("test_password")
                .nickNm("test_nick_nm")
                .emailId("test_email_id")
                .emailDomain("test_email_domain")
                .cttpc("010-0101-0101")
                .cn("test_cn")
                .build();
    }

    /**
     * 테스트용 사용자 Dto (상세) 생성
     */
    public static UserDto create(UserProfileDto profile) {
        // 객체 생성
        UserDto dto = create();
        dto.setProfile(profile);
        return dto;
    }

    /**
     * 테스트용 사용자 Dto (상세) 생성
     */
    public static UserDto create(UserEmplymDto emplym) {
        // 객체 생성
        UserDto dto = create();
        dto.setEmplym(emplym);
        return dto;
    }

    /**
     * 테스트용 사용자 Dto (상세) 생성
     */
    public static UserDto create(UserProfileDto profile, UserEmplymDto emplym) {
        // 객체 생성
        UserDto dto = create();
        dto.setProfile(profile);
        dto.setEmplym(emplym);
        return dto;
    }
}
