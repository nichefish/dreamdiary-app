package io.nicheblog.dreamdiary.auth.permission.entity;

import io.nicheblog.dreamdiary.global.intrfc.entity.BaseCrudEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * RolePermissionEntity
 * <pre>
 *  시스템 롤 ↔ 권한 부여 Entity.
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(name = "role_permission")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@Where(clause = "deleted_at IS NULL")
@SQLDelete(sql = "UPDATE role_permission SET deleted_at = NOW() WHERE id = ?")
public class RolePermissionEntity
        extends BaseCrudEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("롤-권한 ID")
    private Integer id;

    @Column(name = "role_id")
    @Comment("롤 ID")
    private Integer roleId;

    @Column(name = "permission_id")
    @Comment("권한 ID")
    private Integer permissionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_id", referencedColumnName = "id", insertable = false, updatable = false)
    @Fetch(FetchMode.JOIN)
    @NotFound(action = NotFoundAction.IGNORE)
    @Comment("권한 정보")
    private PermissionEntity permission;
}
