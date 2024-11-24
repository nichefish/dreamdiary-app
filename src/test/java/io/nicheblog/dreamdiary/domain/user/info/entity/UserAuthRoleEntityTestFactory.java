package io.nicheblog.dreamdiary.domain.user.info.entity;

import io.nicheblog.dreamdiary.auth.Auth;
import lombok.experimental.UtilityClass;
import org.springframework.test.context.ActiveProfiles;

/**
 * UserAuthRoleEntityTestFactory
 * <pre>
 *  사용자 권한 테스트 Entity 생성 팩토리 모듈
 * </pre>
 *
 * @author nichefish 
 */
@UtilityClass
@ActiveProfiles("test")
public class UserAuthRoleEntityTestFactory {

    /**
     * 테스트용 사용자 권한 Entity 생성
     */
    public static UserAuthRoleEntity create(Auth auth) {
        return UserAuthRoleEntity.builder()
                .authCd(auth.name())
                .build();
    }
}
