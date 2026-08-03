package io.nicheblog.dreamdiary.auth.security.mapstruct;

import io.nicheblog.dreamdiary.auth.security.model.RoleDto;
import io.nicheblog.dreamdiary.auth.type.Auth;
import io.nicheblog.dreamdiary.feature.user.account.entity.UserEntity;
import io.nicheblog.dreamdiary.feature.user.account.entity.UserEntityTestFactory;
import io.nicheblog.dreamdiary.feature.user.account.entity.UserRoleEntity;
import io.nicheblog.dreamdiary.feature.user.account.entity.UserRoleEntityTestFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AuthInfoRoleFillHelper — user_role → AuthInfo.roles 변환 테스트.
 */
@ActiveProfiles("test")
class AuthInfoRoleFillHelperTest {

    @Test
    @DisplayName("roleInfo가 있으면 AuthInfo.roles에 RoleDto가 채워진다")
    void mapRolesFromUserRoles_whenRoleInfoPresent_fillsRoles() throws Exception {
        final UserEntity user = UserEntityTestFactory.create();
        final UserRoleEntity ur = UserRoleEntityTestFactory.create(Auth.USER);
        user.setUserRoles(List.of(ur));

        final List<RoleDto> roles = AuthInfoRoleFillHelper.mapRolesFromUserRoles(user);

        assertFalse(CollectionUtils.isEmpty(roles));
        assertEquals(1, roles.size());
        assertEquals(Auth.USER.name(), roles.get(0).getRoleKey());
    }

    @Test
    @DisplayName("roleInfo가 null이면 해당 user_role 행은 스킵되어 roles가 비어 있을 수 있다")
    void mapRolesFromUserRoles_whenRoleInfoNull_skipsRow() throws Exception {
        final UserEntity user = UserEntityTestFactory.create();
        final UserRoleEntity ur = UserRoleEntity.builder()
                .roleId(999)
                .roleInfo(null)
                .build();
        user.setUserRoles(List.of(ur));

        final List<RoleDto> roles = AuthInfoRoleFillHelper.mapRolesFromUserRoles(user);

        assertTrue(CollectionUtils.isEmpty(roles));
    }

    @Test
    @DisplayName("userRoles가 비어 있으면 roles는 빈 리스트")
    void mapRolesFromUserRoles_whenUserRolesEmpty_emptyList() throws Exception {
        final UserEntity user = UserEntityTestFactory.create();
        user.setUserRoles(List.of());

        final List<RoleDto> roles = AuthInfoRoleFillHelper.mapRolesFromUserRoles(user);

        assertNotNull(roles);
        assertTrue(roles.isEmpty());
    }
}
