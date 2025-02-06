package io.nicheblog.dreamdiary.domain.jrnl.day.entity;

import io.nicheblog.dreamdiary.domain.jrnl.diary.entity.JrnlDiaryEntity;
import io.nicheblog.dreamdiary.domain.jrnl.dream.entity.JrnlDreamEntity;
import io.nicheblog.dreamdiary.extension.ContentType;
import io.nicheblog.dreamdiary.extension.tag.entity.embed.TagEmbed;
import io.nicheblog.dreamdiary.extension.tag.entity.embed.TagEmbedModule;
import io.nicheblog.dreamdiary.global.intrfc.entity.BaseClsfEntity;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.*;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Entity;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * JrnlDayEntity
 * <pre>
 *  저널 일자 Entity.
 *  Single Day that contains each distinct dream/diary.
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(name = "jrnl_day")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@Where(clause = "del_yn='N'")
@SQLDelete(sql = "UPDATE jrnl_day SET del_yn = 'Y' WHERE post_no = ?")
public class JrnlDayEntity
        extends BaseClsfEntity
        implements TagEmbedModule {

    /** 필수: 컨텐츠 타입 */
    @Builder.Default
    private static final ContentType CONTENT_TYPE = ContentType.JRNL_DAY;
    /** 필수(Override): 글분류 코드 */
    @Builder.Default
    private static final String CTGR_CL_CD = CONTENT_TYPE.name() + "_CTGR_CD";

    /** 저널 일자 고유 번호 (PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_no")
    @Comment("저널 일자 고유 번호 (PK)")
    private Integer postNo;

    /** 컨텐츠 타입 */
    @Builder.Default
    @Column(name = "content_type", columnDefinition = "VARCHAR(50) DEFAULT 'JRNL_DAY'")
    @Comment("컨텐츠 타입")
    private String contentType = CONTENT_TYPE.key;

    /* ----- */

    /** 저널 일자 */
    @Column(name = "jrnl_dt")
    @Temporal(TemporalType.DATE)
    @DateTimeFormat(pattern = DateUtils.PTN_DATE)
    @Comment("저널 일자")
    private Date jrnlDt;

    /** 날짜미상 여부 (Y/N) */
    @Builder.Default
    @Column(name = "dt_unknown_yn", length = 1, columnDefinition = "CHAR(1) DEFAULT 'N'")
    @Comment("날짜미상여부")
    private String dtUnknownYn = "N";

    /** 년도 */
    @Column(name = "yy")
    @Comment("년도")
    private Integer yy;

    /** 월 */
    @Column(name = "mnth")
    @Comment("월")
    private Integer mnth;

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

    /** 저널 일기 목록 */
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "jrnl_day_no", referencedColumnName = "post_no", insertable = false, updatable = false)
    @Fetch(FetchMode.SUBSELECT)
    @BatchSize(size = 10)
    @OrderBy("idx ASC")
    @NotFound(action = NotFoundAction.IGNORE)
    @Comment("댓글 목록")
    private List<JrnlDiaryEntity> jrnlDiaryList;

    /** 저널 꿈 목록 */
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "jrnl_day_no", referencedColumnName = "post_no", insertable = false, updatable = false)
    @Fetch(FetchMode.SUBSELECT)
    @BatchSize(size = 10)
    @Where(clause = "else_dream_yn = 'N'")
    @OrderBy("idx ASC")
    @NotFound(action = NotFoundAction.IGNORE)
    @Comment("댓글 목록")
    private List<JrnlDreamEntity> jrnlDreamList;

    /** 저널 꿈 (타인) 목록 */
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "jrnl_day_no", referencedColumnName = "post_no", insertable = false, updatable = false)
    @Fetch(FetchMode.SUBSELECT)
    @BatchSize(size = 10)
    @Where(clause = "else_dream_yn = 'Y'")
    @OrderBy("idx ASC")
    @NotFound(action = NotFoundAction.IGNORE)
    @Comment("댓글 목록")
    private List<JrnlDreamEntity> jrnlElseDreamList;

    /* ----- */

    /** 위임 :: 태그 정보 모듈 */
    @Embedded
    public TagEmbed tag;
}
