package io.nicheblog.dreamdiary.feature.user.account.entity;

import io.nicheblog.dreamdiary.auth.security.entity.RoleEntity;
import io.nicheblog.dreamdiary.global.intrfc.entity.BaseCrudEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * UserRoleEntity
 * <pre>
 *  사용자-역할(user_role) Entity
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(name = "user_role")
@DynamicInsert      // null인 값은 (null로 insert하는 대신) insert에서 제외
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Where(clause = "deleted_at IS NULL")
@SQLDelete(sql = "UPDATE user_role SET deleted_at = NOW() WHERE id = ?")
public class UserRoleEntity
        extends BaseCrudEntity {

    @PostLoad
    private void onLoad() {
        if (this.roleInfo != null) {
            this.roleId = this.roleInfo.getId();
            this.roleKey = this.roleInfo.getRoleKey();
            this.roleName = this.roleInfo.getRoleName();
        }
    }

    /** 사용자-권한 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("사용자 권한 ID")
    private Integer id;

    /** 역할 키 (요청/표시용 — DB에는 저장하지 않음) */
    @Transient
    private String roleKey;

    /** 역할 ID — DB: role_id */
    @Column(name = "role_id")
    @Comment("권한 ID")
    private Integer roleId;

    /** 역할 정보 매핑 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", referencedColumnName = "id", insertable = false, updatable = false)
    @Fetch(value = FetchMode.JOIN)
    @NotFound(action = NotFoundAction.IGNORE)
    @Comment("역할 정보")
    private RoleEntity roleInfo;

    /** 역할 표시명 (표시용) */
    @Transient
    private String roleName;

    /**
     * @param roleKey 역할 키 (예: MNGR)
     */
    public UserRoleEntity(final String roleKey) {
        this.roleKey = roleKey;
    }
}
