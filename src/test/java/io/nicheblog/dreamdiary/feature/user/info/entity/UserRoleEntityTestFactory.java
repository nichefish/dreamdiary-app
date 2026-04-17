package io.nicheblog.dreamdiary.feature.user.info.entity;

import io.nicheblog.dreamdiary.auth.security.entity.RoleEntity;
import io.nicheblog.dreamdiary.auth.type.Auth;
import lombok.experimental.UtilityClass;
import org.springframework.test.context.ActiveProfiles;

/**
 * UserRoleEntityTestFactory
 * <pre>
 *  사용자 권한 테스트 Entity 생성 팩토리 모듈
 * </pre>
 *
 * @author nichefish 
 */
@UtilityClass
@ActiveProfiles("test")
public class UserRoleEntityTestFactory {

    /**
     * 테스트용 사용자 권한 Entity 생성
     */
    public static UserRoleEntity create(final Auth auth) {
        final RoleEntity roleInfo = RoleEntity.builder()
                .id(100 + auth.ordinal())
                .roleKey(auth.name())
                .roleName(auth.desc)
                .build();
        return UserRoleEntity.builder()
                .roleKey(auth.name())
                .roleName(auth.desc)
                .roleInfo(roleInfo)
                .roleId(roleInfo.getId())
                .build();
    }
}
