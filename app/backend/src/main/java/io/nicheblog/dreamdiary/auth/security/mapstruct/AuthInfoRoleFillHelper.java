package io.nicheblog.dreamdiary.auth.security.mapstruct;

import io.nicheblog.dreamdiary.auth.security.model.RoleDto;
import io.nicheblog.dreamdiary.feature.user.account.entity.UserEntity;
import io.nicheblog.dreamdiary.feature.user.account.entity.UserRoleEntity;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * AuthInfoMapstruct의 역할 목록 변환 Helper.
 * UserEntity.userRoles를 AuthInfo.roles에 사용할 RoleDto 목록으로 변환한다.
 */
public class AuthInfoRoleFillHelper {

    /**
     * 사용자 역할 엔티티를 인증 역할 DTO 목록으로 변환한다.
     *
     * @param entity 사용자 엔티티
     * @return 인증 역할 DTO 목록
     */
    public static List<RoleDto> mapRolesFromUserRoles(final UserEntity entity) throws Exception {
        if (CollectionUtils.isEmpty(entity.getUserRoles())) {
            return List.of();
        }
        final List<RoleDto> roles = new ArrayList<>();
        for (final UserRoleEntity ur : entity.getUserRoles()) {
            if (ur.getRoleInfo() != null) {
                roles.add(RoleMapstruct.INSTANCE.toDto(ur.getRoleInfo()));
            }
        }
        return roles;
    }
}
