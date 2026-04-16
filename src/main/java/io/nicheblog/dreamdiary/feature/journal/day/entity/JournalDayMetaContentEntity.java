package io.nicheblog.dreamdiary.feature.journal.day.entity;

import io.nicheblog.dreamdiary.auth.intrfc.entity.BaseAuditRegEntity;
import io.nicheblog.dreamdiary.feature.attachable.meta.entity.MetaSmpEntity;
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
 * JournalDayMetaContentEntity
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
@Where(clause = "ref_content_type='JOURNAL_DAY' AND deleted_at IS NULL")
@SQLDelete(sql = "UPDATE meta_content SET deleted_at = NOW() WHERE id = ?")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class JournalDayMetaContentEntity
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
    @JoinColumn(name = "ref_id", referencedColumnName = "id", insertable = false, updatable = false)
    @Fetch(FetchMode.JOIN)
    @NotFound(action = NotFoundAction.IGNORE)
    @Comment("저널 일자 정보")
    private JournalDaySmpEntity journalDay;
}

