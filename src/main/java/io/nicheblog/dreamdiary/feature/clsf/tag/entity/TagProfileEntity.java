package io.nicheblog.dreamdiary.feature.clsf.tag.entity;

import io.nicheblog.dreamdiary.auth.intrfc.entity.BaseAuditRegEntity;
import io.nicheblog.dreamdiary.global.type.TextClass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;

/**
 * TagProfileEntity
 * <pre>
 *  태그 프로필 Entity.
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(
    name = "tag_profile",
    uniqueConstraints = @UniqueConstraint(name = "uk_tag_profile", columnNames = { "tag_id", "content_type", "regstr_id" })
)
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@Where(clause = "del_yn='N'")
@SQLDelete(sql = "UPDATE tag_profile SET del_yn = 'Y', content_type = CONCAT(content_type, '_del_', id) WHERE id = ?")
public class TagProfileEntity
        extends BaseAuditRegEntity {

    /** 태그 프로필 ID (PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("태그 프로필 ID (PK)")
    private Integer id;

    /** 태그 ID */
    @Column(name = "tag_id", nullable = false)
    @Comment("태그 ID")
    private Integer tagId;

    @Column(name = "content_type", length = 50, nullable = false)
    @Comment("컨텐츠 타입")
    private String contentType;

    @Column(name = "cn", columnDefinition = "LONGTEXT")
    @Comment("프로필 본문")
    private String cn;

    @Enumerated(EnumType.STRING)
    @Column(name = "text_class", length = 30, nullable = false)
    @Comment("시각 의미")
    private TextClass textClass;
}
