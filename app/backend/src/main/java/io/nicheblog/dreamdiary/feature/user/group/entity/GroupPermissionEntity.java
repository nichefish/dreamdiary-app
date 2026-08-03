package io.nicheblog.dreamdiary.feature.user.group.entity;

import io.nicheblog.dreamdiary.auth.permission.entity.PermissionEntity;
import io.nicheblog.dreamdiary.global.intrfc.entity.BaseCrudEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * GroupPermissionEntity
 * <pre>
 *  사용자 그룹 ↔ 권한 부여 Entity.
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(name = "group_permission")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
public class GroupPermissionEntity
        extends BaseCrudEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("그룹-권한 ID")
    private Integer id;

    @Column(name = "group_id")
    @Comment("그룹 ID")
    private Integer groupId;

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
