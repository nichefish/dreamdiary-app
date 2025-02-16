package io.nicheblog.dreamdiary.extension.clsf.tag.entity;

import io.nicheblog.dreamdiary.global.intrfc.entity.BaseAuditRegEntity;
import io.nicheblog.dreamdiary.global.intrfc.entity.BaseClsfKey;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.*;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.*;

/**
 * ContentTagEntity
 * <pre>
 *  컨텐츠-태그 Entity.
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(name = "content_tag")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@Where(clause = "del_yn='N'")
@SQLDelete(sql = "UPDATE content_tag SET del_yn = 'Y' WHERE content_tag_no = ?")
public class ContentTagEntity
        extends BaseAuditRegEntity {

    /** 컨텐츠 태그 번호 (PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "content_tag_no")
    @Comment("컨텐츠 태그 번호 (PK)")
    private Integer contentTagNo;

    /** 참조 태그 번호 */
    @Column(name = "ref_tag_no")
    @Comment("참조 태그 번호")
    private Integer refTagNo;

    /** 참조 글 번호 */
    @Column(name = "ref_post_no")
    @Comment("참조 글 번호")
    private Integer refPostNo;

    /** 참조 컨텐츠 타입 */
    @Column(name = "ref_content_type")
    @Comment("참조 컨텐츠 타입")
    private String refContentType;

    /** 태그 정보 */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ref_tag_no", referencedColumnName = "tag_no", updatable = false, insertable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    private TagSmpEntity tag;

    /** 태그 */
    @Transient
    private String tagNm;

    @Transient
    /** 태그 카테고리 */
    private String ctgr;

    /* ----- */

    /**
     * 생성자.
     *
     * @param refTagNo - 참조 태그 번호
     * @param clsfKey - 게시글 번호와 컨텐츠 타입 정보를 포함하는 분류 키 객체
     */
    public ContentTagEntity(final Integer refTagNo, final BaseClsfKey clsfKey) {
        this.refTagNo = refTagNo;
        this.refPostNo = clsfKey.getPostNo();
        this.refContentType = clsfKey.getContentType();
    }
}
