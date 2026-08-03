package io.nicheblog.dreamdiary.feature.user.group.entity;

import io.nicheblog.dreamdiary.global.intrfc.entity.BaseCrudEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;

import javax.persistence.*;

/**
 * UserGroupMemberEntity
 * <pre>
 *  사용자 ↔ 그룹 멤버십 Entity (M:N).
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(name = "user_group_member")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
public class UserGroupMemberEntity
        extends BaseCrudEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("그룹 멤버 ID")
    private Integer id;

    @Column(name = "user_id")
    @Comment("사용자 ID")
    private Integer userId;

    @Column(name = "group_id")
    @Comment("그룹 ID")
    private Integer groupId;

    @Builder.Default
    @Column(name = "is_primary_yn", length = 1, columnDefinition = "CHAR DEFAULT 'N'")
    @Comment("주 그룹 여부")
    private String isPrimaryYn = "N";
}
