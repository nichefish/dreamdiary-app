package io.nicheblog.dreamdiary.feature.user.account.entity;

import io.nicheblog.dreamdiary.feature.user.emplym.entity.UserEmplymEntity;
import io.nicheblog.dreamdiary.feature.user.profile.entity.UserProfileEntity;
import io.nicheblog.dreamdiary.global.TestConstant;
import lombok.experimental.UtilityClass;
import org.springframework.test.context.ActiveProfiles;

/**
 * UserEntityTestFactory
 * <pre>
 *  사용자 계정 테스트 Entity 생성 팩토리 모듈
 * </pre>
 * TODO: 케이스별로 생성 로직 세분화
 *
 * @author nichefish 
 */
@UtilityClass
@ActiveProfiles("test")
public class UserEntityTestFactory {

    /**
     * 테스트용 사용자 정보 Entity 객체 생성
     */
    public static UserEntity create() throws Exception {
        // 객체 생성
        return UserEntity.builder()
                .username(TestConstant.TEST_USER)
                .password(TestConstant.TEST_PASSWORD_ENCODED)
                .nickname(TestConstant.TEST_NICK_NM)
                .email("test_email_id@test_email_domain")
                .phoneNumber("010-0101-0101")
                .content("test_cn")
                .acntStus(UserStateEntity.builder().build())
                .build();
    }

    /**
     * 테스트용 사용자 정보 Entity 객체 생성
     */
    public static UserEntity create(UserProfileEntity profile) throws Exception {
        // 객체 생성
        UserEntity entity = create();
        entity.setProfile(profile);
        return entity;
    }

    /**
     * 테스트용 사용자 정보 Entity 객체 생성
     */
    public static UserEntity create(UserEmplymEntity emplym) throws Exception {
        // 객체 생성
        UserEntity entity = create();
        entity.setEmplym(emplym);
        return entity;
    }

    /**
     * 테스트용 사용자 정보 Entity 객체 생성
     */
    public static UserEntity create(UserProfileEntity profile, UserEmplymEntity emplym) throws Exception {
        // 객체 생성
        UserEntity entity = create();
        entity.setProfile(profile);
        entity.setEmplym(emplym);
        return entity;
    }
}
