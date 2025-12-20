package io.nicheblog.dreamdiary.extension.clsf.meta.entity;

import io.nicheblog.dreamdiary.global.intrfc.entity.BaseAuditRegEntity;
import io.nicheblog.dreamdiary.global.intrfc.entity.BaseClsfKey;
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
 * MetaContentEntity
 * <pre>
 *   메타-컨텐츠 Entity.
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(name = "meta_content")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@Where(clause = "del_yn='N'")
@SQLDelete(sql = "UPDATE meta_content SET del_yn = 'Y' WHERE meta_content_no = ?")
public class MetaContentEntity
        extends BaseAuditRegEntity {

    /** 메타-컨텐츠 번호 (PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "meta_content_no")
    @Comment("메타-컨텐츠 번호 (PK)")
    private Integer metaContentNo;

    /** 참조 메타 번호 */
    @Column(name = "ref_meta_no")
    @Comment("참조 메타 번호")
    private Integer refMetaNo;

    /** 참조 글 번호 */
    @Column(name = "ref_post_no")
    @Comment("참조 글 번호")
    private Integer refPostNo;

    /** 참조 컨텐츠 타입 */
    @Column(name = "ref_content_type")
    @Comment("참조 컨텐츠 타입")
    private String refContentType;

    /** 메타 정보 */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ref_meta_no", referencedColumnName = "meta_no", updatable = false, insertable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    private MetaSmpEntity meta;

    /** 메타 값 */
    @Column(name = "value")
    @Comment("메타 값")
    private String value;

    /** 메타 값 */
    @Column(name = "unit")
    @Comment("메타 값")
    private String unit;

    /** 메타 */
    @Transient
    private String metaNm;

    /** 메타 카테고리 */
    @Transient
    private String ctgr;

    /** 메타 라벨 */
    @Transient
    private String label;

    /* ----- */

    /**
     * 생성자.
     *
     * @param refMetaNo - 참조 메타 번호
     * @param clsfKey - 게시글 번호와 컨텐츠 타입 정보를 포함하는 분류 키 객체
     * @param value - 값
     */
    public MetaContentEntity(final Integer refMetaNo, final BaseClsfKey clsfKey, final String value, final String unit) {
        this.refMetaNo = refMetaNo;
        this.refPostNo = clsfKey.getPostNo();
        this.refContentType = clsfKey.getContentType();
        this.value = value;
        this.unit = unit;
    }
}
