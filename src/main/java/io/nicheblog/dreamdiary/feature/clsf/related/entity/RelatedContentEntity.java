package io.nicheblog.dreamdiary.feature.clsf.related.entity;

import io.nicheblog.dreamdiary.auth.intrfc.entity.BaseAuditRegEntity;
import io.nicheblog.dreamdiary.feature.clsf._shared.entity.BaseClsfKey;
import io.nicheblog.dreamdiary.feature.clsf.related.type.RelationOriginType;
import io.nicheblog.dreamdiary.feature.clsf.related.type.RelationType;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;

/**
 * RelatedContentEntity
 * <pre>
 *  관련글 Entity.
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(name = "related_content")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Where(clause = "del_yn='N'")
@SQLDelete(sql = "UPDATE related_content SET del_yn = 'Y' WHERE id = ?")
public class RelatedContentEntity
        extends BaseAuditRegEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("관련글 ID")
    private Integer id;

    @Column(name = "left_post_no", nullable = false)
    @Comment("좌측 글 번호")
    private Integer leftPostNo;

    @Column(name = "left_content_type", length = 30, nullable = false)
    @Comment("좌측 컨텐츠 타입")
    private String leftContentType;

    @Column(name = "right_post_no", nullable = false)
    @Comment("우측 글 번호")
    private Integer rightPostNo;

    @Column(name = "right_content_type", length = 30, nullable = false)
    @Comment("우측 컨텐츠 타입")
    private String rightContentType;

    @Builder.Default
    @Column(name = "relation_type", length = 30, nullable = false)
    @Comment("관계 타입")
    private String relationType = RelationType.REFERENCE.key;

    @Column(name = "reason", length = 255)
    @Comment("관계 사유")
    private String reason;

    @Builder.Default
    @Column(name = "origin_type", length = 20, nullable = false)
    @Comment("관계 생성 출처")
    private String originType = RelationOriginType.MANUAL.key;

    public RelatedContentEntity(
            final BaseClsfKey leftKey,
            final BaseClsfKey rightKey,
            final RelationType relationType,
            final String reason,
            final RelationOriginType originType
    ) {
        this.leftPostNo = leftKey.getPostNo();
        this.leftContentType = leftKey.getContentType();
        this.rightPostNo = rightKey.getPostNo();
        this.rightContentType = rightKey.getContentType();
        this.relationType = relationType != null ? relationType.key : RelationType.REFERENCE.key;
        this.reason = reason;
        this.originType = originType != null ? originType.key : RelationOriginType.MANUAL.key;
    }
}
