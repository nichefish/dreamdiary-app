package io.nicheblog.dreamdiary.feature.user.account.model;

import io.nicheblog.dreamdiary.auth.type.Auth;
import lombok.experimental.UtilityClass;
import org.springframework.test.context.ActiveProfiles;

/**
 * UserRoleDtoTestFactory
 * <pre>
 *  사용자 권한 테스트 Dto 생성 팩토리 모듈
 * </pre>
 *
 * @author nichefish
 */
@UtilityClass
@ActiveProfiles("test")
public class UserRoleDtoTestFactory {

    /**
     * 테스트용 사용자 권한 Dto 생성
     */
    public static UserRoleDto create(Auth auth) {
        return UserRoleDto.builder()
                .roleKey(auth.name())
                .build();
    }
}
