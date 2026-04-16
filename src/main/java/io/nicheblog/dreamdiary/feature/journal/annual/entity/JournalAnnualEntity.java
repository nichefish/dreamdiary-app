package io.nicheblog.dreamdiary.feature.journal.annual.entity;

import io.nicheblog.dreamdiary.feature.attachable._shared.entity.BaseAttachableEntity;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.tag.entity.embed.TagEmbed;
import io.nicheblog.dreamdiary.feature.attachable.tag.entity.embed.TagEmbedModule;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import java.util.List;

/**
 * JournalAnnualEntity
 * <pre>
 *  저널 결산 Entity.
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(name = "journal_annual")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@Where(clause = "deleted_at IS NULL")
@SQLDelete(sql = "UPDATE journal_annual SET deleted_at = NOW() WHERE id = ?")
public class JournalAnnualEntity
        extends BaseAttachableEntity
        implements TagEmbedModule {

    /** 저널 꿈 고유 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("저널 결산 고유 번호")
    private Integer id;

    /** 필수: 컨텐츠 타입 */
    @Builder.Default
    @Column(name = "content_type", columnDefinition = "VARCHAR(50) DEFAULT 'JOURNAL_ANNUAL'")

    @Comment("컨텐츠 타입")
    private String contentType = ContentType.JOURNAL_ANNUAL.key;

    /** 제목 */
    @Column(name = "title")
    private String title;

    /** 내용 */
    @Column(name = "content")
    private String content;

    /* ----- */

    /** 꿈 기록 완료 여부 (Y/N) */
    @Builder.Default
    @Column(name = "dream_compt_yn", length = 1, columnDefinition = "CHAR(1) DEFAULT 'N'")
    @Comment("꿈 기록 완료 여부 (Y/N)")
    private String dreamComptYn = "N";

    /* ----- */

    /** 결산 년도 */
    @Column(name = "yy", unique = true)
    private Integer yy;

    /** 꿈 일수 */
    @Column(name = "dream_day_cnt")
    private Integer dreamDayCnt;

    /** 꿈 갯수 */
    @Column(name = "dream_cnt")
    private Integer dreamCnt;

    /** 저널 리뷰 목록 */
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "journal_annual_id", referencedColumnName = "id", insertable = false, updatable = false)
    @Fetch(FetchMode.SUBSELECT)
    @BatchSize(size = 10)
    @OrderBy("sortOrder ASC")
    @NotFound(action = NotFoundAction.IGNORE)
    @Comment("저널 결산 리뷰 목록")
    private List<JournalAnnualReviewEntity> journalAnnualReviewList;

    /* ----- */

    /**
     * 생성자.
     *
     * @param yy - 생성할 엔티티에 설정할 연도 값
     */
    public JournalAnnualEntity(final Integer yy) {
        this.yy = yy;
    }

    /* ----- */

    /** 위임 :: 태그 정보 모듈 */
    @Embedded
    public TagEmbed tag;
}

