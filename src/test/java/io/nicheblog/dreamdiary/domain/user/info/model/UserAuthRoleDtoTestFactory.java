package io.nicheblog.dreamdiary.domain.user.info.model;

import io.nicheblog.dreamdiary.auth.Auth;
import lombok.experimental.UtilityClass;
import org.springframework.test.context.ActiveProfiles;

/**
 * UserAuthRoleDtoTestFactory
 * <pre>
 *  사용자 권한 테스트 Dto 생성 팩토리 모듈
 * </pre>
 *
 * @author nichefish
 */
@UtilityClass
@ActiveProfiles("test")
public class UserAuthRoleDtoTestFactory {

    /**
     * 테스트용 사용자 권한 Dto 생성
     */
    public static UserAuthRoleDto create(Auth auth) {
        return UserAuthRoleDto.builder()
                .authCd(auth.name())
                .build();
    }
}
