package io.nicheblog.dreamdiary.auth.security.mapstruct;

import io.nicheblog.dreamdiary.auth.security.model.AuthInfo;
import io.nicheblog.dreamdiary.auth.type.Auth;
import io.nicheblog.dreamdiary.feature.user.account.entity.UserEntity;
import io.nicheblog.dreamdiary.feature.user.account.entity.UserRoleEntity;
import io.nicheblog.dreamdiary.feature.user.account.entity.UserRoleEntityTestFactory;
import io.nicheblog.dreamdiary.feature.user.account.entity.UserEntityTestFactory;
import io.nicheblog.dreamdiary.global.TestConstant;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AuthInfoRoleFillHelper — user_role → AuthInfo.roles (MapStruct uses @AfterMapping)
 */
@ActiveProfiles("test")
class AuthInfoRoleFillHelperTest {

    private final AuthInfoRoleFillHelper helper = new AuthInfoRoleFillHelper();

    @Test
    @DisplayName("roleInfo가 있으면 AuthInfo.roles에 RoleDto가 채워진다")
    void mapRolesFromUserRoles_whenRoleInfoPresent_fillsRoles() throws Exception {
        final UserEntity user = UserEntityTestFactory.create();
        final UserRoleEntity ur = UserRoleEntityTestFactory.create(Auth.USER);
        user.setUserRoles(List.of(ur));

        final AuthInfo dto = AuthInfo.builder().username(TestConstant.TEST_USER).build();
        helper.mapRolesFromUserRoles(user, dto);

        assertFalse(CollectionUtils.isEmpty(dto.getRoles()));
        assertEquals(1, dto.getRoles().size());
        assertEquals(Auth.USER.name(), dto.getRoles().get(0).getRoleKey());
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

        final AuthInfo dto = AuthInfo.builder().username(TestConstant.TEST_USER).build();
        helper.mapRolesFromUserRoles(user, dto);

        assertTrue(CollectionUtils.isEmpty(dto.getRoles()));
    }

    @Test
    @DisplayName("userRoles가 비어 있으면 roles는 빈 리스트")
    void mapRolesFromUserRoles_whenUserRolesEmpty_emptyList() throws Exception {
        final UserEntity user = UserEntityTestFactory.create();
        user.setUserRoles(List.of());

        final AuthInfo dto = AuthInfo.builder().username(TestConstant.TEST_USER).build();
        helper.mapRolesFromUserRoles(user, dto);

        assertNotNull(dto.getRoles());
        assertTrue(dto.getRoles().isEmpty());
    }
}
