package io.nicheblog.dreamdiary.feature.journal.day.entity;

import io.nicheblog.dreamdiary.feature.clsf._shared.entity.BaseClsfEntity;
import io.nicheblog.dreamdiary.feature.clsf._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.clsf.meta.entity.embed.MetaEmbed;
import io.nicheblog.dreamdiary.feature.clsf.meta.entity.embed.MetaEmbedModule;
import io.nicheblog.dreamdiary.feature.clsf.tag.entity.embed.TagEmbed;
import io.nicheblog.dreamdiary.feature.clsf.tag.entity.embed.TagEmbedModule;
import io.nicheblog.dreamdiary.feature.journal.dream.entity.JournalDreamEntity;
import io.nicheblog.dreamdiary.feature.journal.chapter.entity.JournalChapterEntity;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.*;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import java.util.Date;
import java.util.List;

/**
 * JournalDayEntity
 * <pre>
 *  저널 일자 Entity.
 *  Single Day that contains each distinct dream/diary.
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(name = "journal_day")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@Where(clause = "deleted_at IS NULL")
@SQLDelete(sql = "UPDATE journal_day SET deleted_at = NOW() WHERE id = ?")
@NamedEntityGraph(
    name = "JournalDayEntity.withTags",
    attributeNodes = {
        @NamedAttributeNode(value = "tag", subgraph = "TagEmbed")
    },
    subgraphs = {
        @NamedSubgraph(
            name = "TagEmbed",
            attributeNodes = @NamedAttributeNode("list")  // tag.list 즉시 로딩
        )
    }
)
public class JournalDayEntity
        extends BaseClsfEntity
        implements TagEmbedModule, MetaEmbedModule {

    /** 저널 일자 고유 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("저널 일자 고유 ID")
    private Integer id;

    /** 필수: 컨텐츠 타입 */
    @Builder.Default
    @Column(name = "content_type", columnDefinition = "VARCHAR(50) DEFAULT 'JOURNAL_DAY'")
    @Comment("컨텐츠 타입")
    private String contentType = ContentType.JOURNAL_DAY.key;

    /* ----- */

    /** 저널 일자 */
    @Column(name = "journal_dt")
    @Temporal(TemporalType.DATE)
    @DateTimeFormat(pattern = DateUtils.PTN_DATE)
    @Comment("저널 일자")
    private Date journalDt;

    /** 날짜미상 여부 (Y/N) */
    @Builder.Default
    @Column(name = "dt_unknown_yn", length = 1, columnDefinition = "CHAR(1) DEFAULT 'N'")
    @Comment("날짜미상 여부 (Y/N)")
    private String dtUnknownYn = "N";

    /** 년도 */
    @Column(name = "yy")
    @Comment("년도")
    private Integer yy;

    /** 월 */
    @Column(name = "mnth")
    @Comment("월")
    private Integer mnth;

    /** 주 시작일자 (월요일 기준) */
    @Column(name = "week_start_dt")
    @Temporal(TemporalType.DATE)
    @DateTimeFormat(pattern = DateUtils.PTN_DATE)
    @Comment("주 시작일자 (월요일 기준)")
    private Date weekStartDt;

    /** 대략일자 (날짜미상시 해당일자 이후에 표기) */
    @Column(name = "aprxmt_dt")
    @Temporal(TemporalType.DATE)
    @DateTimeFormat(pattern = DateUtils.PTN_DATE)
    @Comment("대략일자 (날짜미상시 해당일자 이후에 표기)")
    private Date aprxmtDt;

    /** 날씨 */
    @Column(name = "weather")
    @Comment("날씨")
    private String weather;

    /** 저널 챕터 목록 */
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "journal_day_id", referencedColumnName = "id", insertable = false, updatable = false)
    @Fetch(FetchMode.SUBSELECT)
    @BatchSize(size = 10)
    @OrderBy("sortOrder ASC")
    @NotFound(action = NotFoundAction.IGNORE)
    @Comment("저널 챕터 목록")
    private List<JournalChapterEntity> journalChapterList;

    /** 저널 꿈 목록 */
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "journal_day_id", referencedColumnName = "id", insertable = false, updatable = false)
    @Fetch(FetchMode.SUBSELECT)
    @BatchSize(size = 10)
    @Where(clause = "else_dream_yn = 'N'")
    @OrderBy("sortOrder ASC")
    @NotFound(action = NotFoundAction.IGNORE)
    @Comment("저널 꿈 목록")
    private List<JournalDreamEntity> journalDreamList;

    /** 저널 꿈 (타인) 목록 */
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "journal_day_id", referencedColumnName = "id", insertable = false, updatable = false)
    @Fetch(FetchMode.SUBSELECT)
    @BatchSize(size = 10)
    @Where(clause = "else_dream_yn = 'Y'")
    @OrderBy("sortOrder ASC")
    @NotFound(action = NotFoundAction.IGNORE)
    @Comment("저널 꿈 (타인) 목록")
    private List<JournalDreamEntity> journalElseDreamList;

    /* ----- */

    /** 위임 :: 태그 정보 모듈 */
    @Embedded
    public TagEmbed tag;
    /** 위임 :: 메타 정보 모듈 */
    @Embedded
    public MetaEmbed meta;
}

