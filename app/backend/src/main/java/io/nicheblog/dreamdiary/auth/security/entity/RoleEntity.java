package io.nicheblog.dreamdiary.auth.security.entity;

import io.nicheblog.dreamdiary.global.intrfc.entity.BaseCrudEntity;
import io.nicheblog.dreamdiary.global.intrfc.entity.Usable;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import java.util.List;

/**
 * RoleEntity
 * <pre>
 *  (공통) 권한 정보 Entity.
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(name = "role")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@Where(clause = "deleted_at IS NULL")
public class RoleEntity
        extends BaseCrudEntity
        implements Usable {

    /** 내부 PK (id) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    /** 역할 키 (비즈니스 키, UNIQUE) — DB: role_key */
    @Column(name = "role_key", length = 50, unique = true)
    private String roleKey;

    /** 역할 표시명 — DB: role_name */
    @Column(name = "role_name", length = 50)
    private String roleName;

    /** 권한 레벨 */
    @Column(name = "auth_level")
    private Integer authLevel;

    /** 상위 권한 ID (null이면 최상위) */
    @Column(name = "parent_role_id")
    private Integer parentRoleId;

    /** 정렬 순서 */
    @Column(name = "sort_order", columnDefinition = "INT DEFAULT 0")
    private Integer sortOrder;

    /** 사용 여부 (Y/N) */
    @Builder.Default
    @Column(name = "use_yn", length = 1, columnDefinition = "CHAR DEFAULT 'Y'")
    private String useYn = "N";

    /** 하위 역할 목록 */
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_role_id", referencedColumnName = "id", insertable = false, updatable = false)
    @Fetch(value = FetchMode.JOIN)
    @BatchSize(size = 10)
    @OrderBy("sortOrder ASC")
    @NotFound(action = NotFoundAction.IGNORE)
    @Comment("하위 역할 목록")
    private List<RoleEntity> subRoleList;
}