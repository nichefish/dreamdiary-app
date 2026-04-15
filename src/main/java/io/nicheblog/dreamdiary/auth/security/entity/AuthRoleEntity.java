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
 * AuthRoleEntity
 * <pre>
 *  (공통) 권한 정보 Entity.
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(name = "auth_role")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@Where(clause = "deleted_at IS NULL")
public class AuthRoleEntity
        extends BaseCrudEntity
        implements Usable {

    /** 권한 코드 (PK) */
    @Id
    @Column(name = "auth_cd", length = 50)
    private String authCd;

    /** 권한 이름 */
    @Column(name = "auth_nm", length = 50)
    private String authNm;

    /** 권한 레벨 */
    @Column(name = "auth_level")
    private Integer authLevel;

    /** 상위 권한 코드 (null일시 최상위 권한) */
    @Column(name = "top_auth_cd", length = 50)
    private String topAuthCd;

    /** 정렬 순서 */
    @Column(name = "sort_order", columnDefinition = "INT DEFAULT 0")
    private Integer sortOrder;

    /** 사용 여부 (Y/N) */
    @Builder.Default
    @Column(name = "use_yn", length = 1, columnDefinition = "CHAR DEFAULT 'Y'")
    private String useYn = "N";

    /** 하위 권한 정보 */
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "auth_cd", referencedColumnName = "top_auth_cd", insertable = false, updatable = false)
    @Fetch(value = FetchMode.JOIN)
    @BatchSize(size = 10)
    @OrderBy("sortOrder ASC")
    @NotFound(action = NotFoundAction.IGNORE)
    @Comment("하위 권한 정보")
    private List<AuthRoleEntity> subAuthList;
}