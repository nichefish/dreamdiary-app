package io.nicheblog.dreamdiary.auth.security.mapstruct;

import io.nicheblog.dreamdiary.auth.security.model.AuthInfo;
import io.nicheblog.dreamdiary.auth.security.model.RoleDto;
import io.nicheblog.dreamdiary.feature.user.account.entity.UserEntity;
import io.nicheblog.dreamdiary.feature.user.account.entity.UserRoleEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.mapstruct.AfterMapping;
import org.mapstruct.MappingTarget;

import java.util.ArrayList;
import java.util.List;

/**
 * AuthInfoMapstruct용 — UserEntity.userRoles → AuthInfo.roles.
 * 인터페이스 default @AfterMapping 은 생성된 AuthInfoMapstructImpl.toDto에 연결되지 않아 uses 클래스로 분리함.
 * (무인자 생성자 — Mappers.getMapper 경로 호환)
 */
public class AuthInfoRoleFillHelper {

    @AfterMapping
    public void mapRolesFromUserRoles(final UserEntity entity, final @MappingTarget AuthInfo dto) throws Exception {
        if (CollectionUtils.isEmpty(entity.getUserRoles())) {
            dto.setRoles(List.of());
            return;
        }
        final List<RoleDto> roles = new ArrayList<>();
        for (final UserRoleEntity ur : entity.getUserRoles()) {
            if (ur.getRoleInfo() != null) {
                roles.add(RoleMapstruct.INSTANCE.toDto(ur.getRoleInfo()));
            }
        }
        dto.setRoles(roles);
    }
}
