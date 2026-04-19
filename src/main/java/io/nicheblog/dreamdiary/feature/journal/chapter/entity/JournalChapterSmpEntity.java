package io.nicheblog.dreamdiary.feature.journal.chapter.entity;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.journal.chapter.type.ChapterType;
import io.nicheblog.dreamdiary.feature.journal.day.entity.JournalDaySmpEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * JournalChapterSmpEntity
 * <pre>
 *  저널 챕터 Entity. (연관관계 간소화)
 *  Entity that contains each distinct dream.
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(name = "journal_chapter")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@Where(clause = "deleted_at IS NULL")
@SQLDelete(sql = "UPDATE journal_chapter SET deleted_at = NOW() WHERE id = ?")
public class JournalChapterSmpEntity {

    /** 필수: 컨텐츠 타입 */
    @Builder.Default
    private static final ContentType CONTENT_TYPE = ContentType.JOURNAL_CHAPTER;

    /** 저널 챕터 고유 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("저널 챕터 고유 번호")
    private Integer id;

    /** 컨텐츠 타입 */
    @Builder.Default
    @Column(name = "content_type", columnDefinition = "VARCHAR(50) DEFAULT 'JOURNAL_CHAPTER'")
    @Comment("컨텐츠 타입")
    private String contentType = CONTENT_TYPE.key;

    /* ----- */

    /** 챕터 타입 (DIARY | DREAM) */
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "chapter_type", length = 30, columnDefinition = "VARCHAR(30) DEFAULT 'DIARY'")
    @Comment("챕터 타입")
    private ChapterType chapterType = ChapterType.DIARY;

    /** 순번 */
    @Column(name = "sort_order", columnDefinition = "INT DEFAULT 1")
    private Integer sortOrder;

    /** 저널 일자 번호  */
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

