package io.nicheblog.dreamdiary.feature.journal.diary.entity;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.journal.chapter.entity.JournalChapterSmpEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * JournalDiarySmpEntity
 * <pre>
 *  저널 일기 Entity. (연관관계 간소화)
 *  Entity that contains each distinct diary.
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(name = "journal_diary")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@Where(clause = "deleted_at IS NULL")
@SQLDelete(sql = "UPDATE journal_diary SET deleted_at = NOW() WHERE id = ?")
public class JournalDiarySmpEntity {

    /** 필수: 컨텐츠 타입 */
    @Builder.Default
    private static final ContentType CONTENT_TYPE = ContentType.JOURNAL_DIARY;

    /** 저널 꿈 고유 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("저널 일기 고유 번호")
    private Integer id;

    /** 컨텐츠 타입 */
    @Builder.Default
    @Column(name = "content_type", columnDefinition = "VARCHAR(50) DEFAULT 'JOURNAL_DIARY'")
    @Comment("컨텐츠 타입")
    private String contentType = CONTENT_TYPE.key;

    /* ----- */

    /** 저널 챕터 번호  */
    @Column(name = "journal_chapter_id")
    @Comment("저널 챕터 번호")
    private Integer journalChapterId;

    /** 저널 챕터 정보 */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "journal_chapter_id", referencedColumnName = "id", insertable = false, updatable = false)
    @Fetch(FetchMode.JOIN)
    @NotFound(action = NotFoundAction.IGNORE)
    @Comment("저널 챕터 정보")
    private JournalChapterSmpEntity journalChapter;

    /** 순번 */
    @Column(name = "sort_order", columnDefinition = "INT DEFAULT 1")
    private Integer sortOrder;
}

