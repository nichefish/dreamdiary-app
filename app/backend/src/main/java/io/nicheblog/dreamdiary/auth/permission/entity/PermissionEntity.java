package io.nicheblog.dreamdiary.auth.permission.entity;

import io.nicheblog.dreamdiary.auth.intrfc.entity.BaseAuditEntity;
import io.nicheblog.dreamdiary.global.intrfc.entity.Usable;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;

/**
 * PermissionEntity
 * <pre>
 *  원자 권한(permission) 카탈로그 Entity.
 *  시스템 롤·사용자 그룹에 부여되며, 메뉴 노출 등 소비처에서 판정한다.
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(name = "permission")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@Where(clause = "deleted_at IS NULL")
@SQLDelete(sql = "UPDATE permission SET deleted_at = NOW() WHERE id = ?")
public class PermissionEntity
        extends BaseAuditEntity
        implements Usable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("권한 ID")
    private Integer id;

    @Column(name = "perm_key", length = 100, unique = true, nullable = false)
    @Comment("권한 키")
    private String permKey;

    @Column(name = "perm_name", length = 100)
    @Comment("권한 표시명")
    private String permName;

    @Column(name = "description", length = 500)
    @Comment("권한 설명")
    private String description;

    @Column(name = "sort_order", columnDefinition = "INT DEFAULT 0")
    private Integer sortOrder;

    @Builder.Default
    @Column(name = "use_yn", length = 1, columnDefinition = "CHAR DEFAULT 'Y'")
    private String useYn = "Y";
}
