package io.nicheblog.dreamdiary.auth.security.service;

import io.nicheblog.dreamdiary.auth.policy.service.AuthPolicyQueryService;
import io.nicheblog.dreamdiary.auth.permission.service.PermissionResolveService;
import io.nicheblog.dreamdiary.auth.security.entity.RoleEntity;
import io.nicheblog.dreamdiary.auth.security.mapstruct.AuthInfoMapstruct;
import io.nicheblog.dreamdiary.auth.security.model.AuthInfo;
import io.nicheblog.dreamdiary.auth.security.repository.jpa.RoleRepository;
import io.nicheblog.dreamdiary.feature.user.account.entity.UserEntity;
import io.nicheblog.dreamdiary.feature.user.account.entity.UserEntityTestFactory;
import io.nicheblog.dreamdiary.feature.user.account.entity.UserRoleEntity;
import io.nicheblog.dreamdiary.feature.user.account.repository.jpa.UserRepository;
import io.nicheblog.dreamdiary.global.TestConstant;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * AuthService.loadUserByUsername — toDto 후 roles 비어 있을 때 role_id 로 RoleRepository 보강
 */
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class AuthServiceRoleEnrichmentTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private AuthPolicyQueryService authPolicyQueryService;
    @Mock
    private PermissionResolveService permissionResolveService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        final AuthInfoMapstruct authInfoMapstruct = Mappers.getMapper(AuthInfoMapstruct.class);
        authService = new AuthService(
                userRepository,
                roleRepository,
                authPolicyQueryService,
                authInfoMapstruct,
                permissionResolveService
        );
    }

    @Test
    @DisplayName("user_role.role_id 만 있고 roleInfo 가 비어 있어도 RoleRepository 로 roles 가 채워진다")
    void loadUserByUsername_fillsRolesFromRoleIdWhenRoleInfoMissing() throws Exception {
        final UserEntity user = UserEntityTestFactory.create();
        final UserRoleEntity ur = UserRoleEntity.builder()
                .roleId(10)
                .roleInfo(null)
                .build();
        user.setUserRoles(List.of(ur));

        when(userRepository.findByUsername(TestConstant.TEST_USER)).thenReturn(Optional.of(user));

        final RoleEntity roleEntity = RoleEntity.builder()
                .id(10)
                .roleKey("USER")
                .roleName("사용자")
                .build();
        when(roleRepository.findById(10)).thenReturn(Optional.of(roleEntity));
        when(permissionResolveService.resolvePermKeys(user)).thenReturn(List.of());

        final AuthInfo authInfo = authService.loadUserByUsername(TestConstant.TEST_USER);

        assertFalse(CollectionUtils.isEmpty(authInfo.getRoles()));
        assertEquals(1, authInfo.getRoles().size());
        assertEquals("USER", authInfo.getRoles().get(0).getRoleKey());
        assertTrue(authInfo.getAuthorities().stream().anyMatch(a -> "ROLE_USER".equals(a.getAuthority())));
    }
}
