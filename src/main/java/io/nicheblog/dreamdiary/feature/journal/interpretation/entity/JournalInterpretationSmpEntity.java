package io.nicheblog.dreamdiary.feature.journal.interpretation.entity;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.journal.day.entity.JournalDaySmpEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * JournalInterpretationSmpEntity
 * <pre>
 *  저널 해석 Entity. (연관관계 간소화)
 *  Entity that contains each distinct interpretation.
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(name = "journal_interpretation")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@Where(clause = "deleted_at IS NULL")
@SQLDelete(sql = "UPDATE journal_interpretation SET deleted_at = NOW() WHERE id = ?")
public class JournalInterpretationSmpEntity {

    /** 필수: 컨텐츠 타입 */
    @Builder.Default
    private static final ContentType CONTENT_TYPE = ContentType.JOURNAL_INTERPRETATION;

    /** 저널 해석 고유 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("저널 해석 고유 번호")
    private Integer id;

    /** 컨텐츠 타입 */
    @Builder.Default
    @Column(name = "content_type", columnDefinition = "VARCHAR(50) DEFAULT 'JOURNAL_INTERPRETATION'")
    @Comment("컨텐츠 타입")
    private String contentType = CONTENT_TYPE.key;

    /* ----- */

    /** 순번 */
    @Column(name = "sort_order", columnDefinition = "INT DEFAULT 1")
    private Integer sortOrder;

    /** 참조 엔티티 번호 */
    @Column(name = "ref_id")
    @Comment("참조 엔티티 번호")
    private Integer refId;

    /** 참조 컨텐츠 타입 */
    @Enumerated(EnumType.STRING)
    @Column(name = "ref_content_type", length = 50)
    @Comment("참조 컨텐츠 타입")
    private ContentType refContentType;

    /** 저널 일자 번호 */
    @Column(name = "journal_day_id")
    @Comment("저널 일자 번호")
    private Integer journalDayId;

    /** 저널 일자 정보 */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "journal_day_id", referencedColumnName = "id", insertable = false, updatable = false)
    @Fetch(FetchMode.JOIN)
    @NotFound(action = NotFoundAction.IGNORE)
    @Comment("저널 일자 정보")
    private JournalDaySmpEntity journalDay;
}
