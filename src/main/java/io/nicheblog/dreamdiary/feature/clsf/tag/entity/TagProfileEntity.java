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

    /** 태그 프로필 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("태그 프로필 ID")
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

    /**
     * 개별 태그 시각 의미. {@code null}이면 카테고리 프로필(또는 전역 기본) 색을 상속한다.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "text_class", length = 30, nullable = true)
    @Comment("시각 의미 (NULL=카테고리/기본 상속)")
    private TextClass textClass;
}
