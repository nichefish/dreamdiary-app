package io.nicheblog.dreamdiary.feature.clsf.tag.entity;

import io.nicheblog.dreamdiary.auth.intrfc.entity.BaseAuditRegEntity;
import io.nicheblog.dreamdiary.feature.clsf._shared.entity.BaseClsfKey;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * TagContentEntity
 * <pre>
 *  태그-컨텐츠 Entity.
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(name = "tag_content")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@Where(clause = "del_yn='N'")
@SQLDelete(sql = "UPDATE tag_content SET del_yn = 'Y' WHERE id = ?")
public class TagContentEntity
        extends BaseAuditRegEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("태그-컨텐츠 ID")
    private Integer id;

    /** 태그 ID */
    @Column(name = "tag_id")
    @Comment("태그 ID")
    private Integer tagId;

    /** 참조 글 ID. */
    @Column(name = "ref_post_no")
    @Comment("참조 글 ID")
    private Integer refPostNo;

    /** 참조 컨텐츠 타입 */
    @Column(name = "ref_content_type")
    @Comment("참조 컨텐츠 타입")
    private String refContentType;

    /** 태그 정보 */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tag_id", referencedColumnName = "id", updatable = false, insertable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    private TagSmpEntity tag;

    /** 태그 */
    @Transient
    private String tagNm;

    /** 태그 카테고리 */
    @Transient
    private String ctgr;

    /* ----- */

    /**
     * 생성자.
     *
     * @param tagId - 태그 ID
     * @param clsfKey - 게시글 번호와 컨텐츠 타입 정보를 포함하는 분류 키 객체
     */
    public TagContentEntity(final Integer tagId, final BaseClsfKey clsfKey) {
        this.tagId = tagId;
        this.refPostNo = clsfKey.getPostNo();
        this.refContentType = clsfKey.getContentType();
    }
}
