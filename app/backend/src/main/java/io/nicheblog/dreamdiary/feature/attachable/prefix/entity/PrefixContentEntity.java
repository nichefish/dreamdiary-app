package io.nicheblog.dreamdiary.feature.attachable.prefix.entity;

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
 * PrefixContentEntity
 * <pre>
 *   콘텐츠가 선택한 말머리를 나타내는 attachable 연결 엔티티.
 *   (ref_id, ref_content_type) = {@link BaseAttachableKey}로 콘텐츠를 가리키고, prefix_id로
 *   선택된 말머리를 기록한다. 콘텐츠당 0..1이며, 단일 active 행 보장은 앱 로직(선택 upsert)이
 *   담당한다. meta_content와 동일하게 soft-delete하므로 DB UNIQUE는 두지 않는다
 *   (soft-delete 행이 유니크 키를 점유하는 충돌 방지). 콘텐츠 엔티티는 prefix FK를 직접 들지 않고
 *   PrefixEmbed로 이 연결을 조립한다.
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(name = "prefix_content")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@Where(clause = "deleted_at IS NULL")
@SQLDelete(sql = "UPDATE prefix_content SET deleted_at = NOW() WHERE id = ?")
public class PrefixContentEntity
        extends BaseAuditRegEntity {

    /** 말머리-컨텐츠 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("말머리-컨텐츠 ID")
    private Integer id;

    /** 선택된 말머리 ID */
    @Column(name = "prefix_id")
    @Comment("선택된 말머리 ID")
    private Integer prefixId;

    /** 참조 글 번호 */
    @Column(name = "ref_id")
    @Comment("참조 글 번호")
    private Integer refId;

    /** 참조 컨텐츠 타입 */
    @Column(name = "ref_content_type")
    @Comment("참조 컨텐츠 타입")
    private String refContentType;

    /** 선택된 말머리 (표시용) */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "prefix_id", referencedColumnName = "id", updatable = false, insertable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    private PrefixEntity prefix;

    /* ----- */

    /**
     * 생성자.
     *
     * @param prefixId - 선택된 말머리 ID
     * @param attachableKey - 글 번호와 컨텐츠 타입을 포함하는 분류 키 객체
     */
    public PrefixContentEntity(final Integer prefixId, final BaseAttachableKey attachableKey) {
        this.prefixId = prefixId;
        this.refId = attachableKey.getId();
        this.refContentType = attachableKey.getContentType();
    }
}
