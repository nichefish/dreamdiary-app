package io.nicheblog.dreamdiary.feature.user.account.model;

import io.nicheblog.dreamdiary.auth.security.entity.RoleEntity;
import io.nicheblog.dreamdiary.global.intrfc.model.BaseCrudDto;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * UserRoleDto
 * <pre>
 *  사용자-권한 Dto.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class UserRoleDto
        extends BaseCrudDto {

    /** 사용자 권한 ID */
    private Integer id;

    /** 역할 키 (요청/표시용) */
    private String roleKey;

    /** 역할 PK (user_role.role_id) */
    private Integer roleId;

    /** 역할 표시명 (표시용) */
    private String roleName;

    /** 권한 정보 매핑 */
    private RoleEntity role;
}
