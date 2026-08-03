package io.nicheblog.dreamdiary.feature.user.group.entity;

import io.nicheblog.dreamdiary.auth.intrfc.entity.BaseAuditEntity;
import io.nicheblog.dreamdiary.global.intrfc.entity.Sortable;
import io.nicheblog.dreamdiary.global.intrfc.entity.Usable;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;

/**
 * UserGroupEntity
 * <pre>
 *  사용자 그룹 Entity. 시스템 롤(USER/MNGR/DEV)과 직교하는 소속·권한 묶음.
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(name = "user_group")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@Where(clause = "deleted_at IS NULL")
@SQLDelete(sql = "UPDATE user_group SET deleted_at = NOW() WHERE id = ?")
public class UserGroupEntity
        extends BaseAuditEntity
        implements Usable, Sortable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("사용자 그룹 ID")
    private Integer id;

    @Column(name = "group_key", length = 50, unique = true, nullable = false)
    @Comment("그룹 키")
    private String groupKey;

    @Column(name = "group_name", length = 100)
    @Comment("그룹 표시명")
    private String groupName;

    @Column(name = "description", length = 500)
    @Comment("그룹 설명")
    private String description;

    @Column(name = "sort_order", columnDefinition = "INT DEFAULT 0")
    private Integer sortOrder;

    @Builder.Default
    @Column(name = "use_yn", length = 1, columnDefinition = "CHAR DEFAULT 'Y'")
    private String useYn = "Y";
}
