package io.nicheblog.dreamdiary.feature.attachable.meta.entity;

import io.nicheblog.dreamdiary.auth.intrfc.entity.BaseAuditRegEntity;
import io.nicheblog.dreamdiary.feature.attachable._shared.entity.BaseAttachableKey;
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
@Where(clause = "deleted_at IS NULL")
@SQLDelete(sql = "UPDATE meta_content SET deleted_at = NOW() WHERE id = ?")
public class MetaContentEntity
        extends BaseAuditRegEntity {

    /** 메타-컨텐츠 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("메타-컨텐츠 ID")
    private Integer id;

    /** 메타 ID */
    @Column(name = "meta_id")
    @Comment("메타 ID")
    private Integer metaId;

    /** 참조 글 번호 */
    @Column(name = "ref_id")
    @Comment("참조 글 번호")
    private Integer refId;

    /** 참조 컨텐츠 타입 */
    @Column(name = "ref_content_type")
    @Comment("참조 컨텐츠 타입")
    private String refContentType;

    /** 메타 정보 */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "meta_id", referencedColumnName = "id", updatable = false, insertable = false)
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
     * @param metaId - 메타 ID
     * @param attachableKey - 게시글 번호와 컨텐츠 타입 정보를 포함하는 분류 키 객체
     * @param value - 값
     */
    public MetaContentEntity(final Integer metaId, final BaseAttachableKey attachableKey, final String value, final String unit) {
        this.metaId = metaId;
        this.refId = attachableKey.getId();
        this.refContentType = attachableKey.getContentType();
        this.value = value;
        this.unit = unit;
    }
}
