package io.nicheblog.dreamdiary.feature.journal.day.entity;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.journal.day.type.JournalDatePrecision;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.time.LocalDate;

/**
 * JournalDaySmpEntity
 * <pre>
 *  저널 일자 Entity. (연관관계 제거 버전)
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
public class JournalDaySmpEntity {

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
    @Column(name = "journal_date")
    @DateTimeFormat(pattern = DateUtils.PTN_DATE)
    @Comment("저널 일자")
    private LocalDate journalDate;

    /** 저널 날짜 정밀도 (EXACT | APPROXIMATE | UNKNOWN) */
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "journal_date_precision", length = 20, columnDefinition = "VARCHAR(20) DEFAULT 'EXACT'")
    @Comment("저널 날짜 정밀도 (EXACT | APPROXIMATE | UNKNOWN)")
    private JournalDatePrecision journalDatePrecision = JournalDatePrecision.EXACT;

    /** 년도 */
    @Column(name = "yy")
    @Comment("년도")
    private Integer yy;

    /** 월 */
    @Column(name = "mnth")
    @Comment("월")
    private Integer mnth;

    /** 주 시작일자 (월요일 기준) */
    @Column(name = "week_start_date")
    @DateTimeFormat(pattern = DateUtils.PTN_DATE)
    @Comment("주 시작일자 (월요일 기준)")
    private LocalDate weekStartDt;

    /** 날씨 */
    @Column(name = "weather")
    @Comment("날씨")
    private String weather;

    /**
     * 일기 축 완결 여부 (Y/N). 엔트리 DTO 투영용 — 쓰기 잠금 SSOT는 journal_day 컬럼.
     */
    @Builder.Default
    @Column(name = "diary_resolved_yn", length = 1, columnDefinition = "CHAR(1) DEFAULT 'N'")
    @Comment("일기 축 완결(Y/N)")
    private String diaryResolvedYn = "N";

    /**
     * 꿈 축 완결 여부 (Y/N). 엔트리 DTO 투영용.
     */
    @Builder.Default
    @Column(name = "dream_resolved_yn", length = 1, columnDefinition = "CHAR(1) DEFAULT 'N'")
    @Comment("꿈 축 완결(Y/N)")
    private String dreamResolvedYn = "N";
}

