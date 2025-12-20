package io.nicheblog.dreamdiary.domain.jrnl.day.entity;

import io.nicheblog.dreamdiary.extension.clsf.meta.entity.MetaSmpEntity;
import io.nicheblog.dreamdiary.global.intrfc.entity.BaseAuditRegEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.*;
import org.hibernate.annotations.Cache;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * JrnlDayMetaContentEntity
 * <pre>
 *  저널 일자 메타 Entity. (사용 용이성을 위해 엔티티 분리)
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
@Where(clause = "ref_content_type='JRNL_DAY' AND del_yn='N'")
@SQLDelete(sql = "UPDATE meta_content SET del_yn = 'Y' WHERE meta_content_no = ?")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class JrnlDayMetaContentEntity
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
    @Fetch(FetchMode.JOIN)
    @NotFound(action = NotFoundAction.IGNORE)
    private MetaSmpEntity meta;

    /** 메타 이름 */
    @Transient
    private String metaNm;

    /** 메타 카테고리 */
    @Transient
    private String ctgr;

    /** 참조 컨텐츠 (저널 일자)  */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ref_post_no", referencedColumnName = "post_no", insertable = false, updatable = false)
    @Fetch(FetchMode.JOIN)
    @NotFound(action = NotFoundAction.IGNORE)
    @Comment("저널 일자 정보")
    private JrnlDaySmpEntity jrnlDay;
}
