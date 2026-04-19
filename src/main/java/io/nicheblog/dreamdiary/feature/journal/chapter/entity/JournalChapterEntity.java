package io.nicheblog.dreamdiary.feature.journal.chapter.entity;

import io.nicheblog.dreamdiary.feature.attachable._shared.entity.BaseAttachableEntity;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.state.entity.embed.StateEmbed;
import io.nicheblog.dreamdiary.feature.attachable.state.entity.embed.StateEmbedModule;
import io.nicheblog.dreamdiary.feature.attachable.tag.entity.embed.TagEmbed;
import io.nicheblog.dreamdiary.feature.attachable.tag.entity.embed.TagEmbedModule;
import io.nicheblog.dreamdiary.feature.journal.chapter.type.ChapterType;
import io.nicheblog.dreamdiary.feature.journal.day.entity.JournalDaySmpEntity;
import io.nicheblog.dreamdiary.feature.journal.diary.entity.JournalDiaryEntity;
import io.nicheblog.dreamdiary.feature.journal.dream.entity.JournalDreamEntity;
import io.nicheblog.dreamdiary.feature.journal.note.entity.JournalNoteEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import java.util.List;

/**
 * JournalChapterEntity
 * <pre>
 *  저널 챕터 Entity.
 *  Entity that contains each distinct chapter.
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
public class JournalChapterEntity
        extends BaseAttachableEntity
        implements TagEmbedModule, StateEmbedModule {

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

    /** 챕터 타입 (DIARY | DREAM) */
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "chapter_type", length = 30, columnDefinition = "VARCHAR(30) DEFAULT 'DIARY'")
    @Comment("챕터 타입")
    private ChapterType chapterType = ChapterType.DIARY;

    /** 제목 */
    @Column(name = "title")
    private String title;

    /** 글분류 코드 :: join을 제거하고 메모리 캐시 처리 */
    @Column(name = "category_code", length = 50)
    @Comment("저널 일기 글분류 코드 정보")
    private String categoryCode;

    /** 글분류 코드 이름 :: join을 제거하고 메모리 캐시 처리 */
    @Transient
    private String categoryName;

    /* ----- */

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

    /** 순번 */
    @Column(name = "sort_order", columnDefinition = "INT DEFAULT 1")
    private Integer sortOrder;

    /** 저널 일기 목록 */
    @OneToMany(mappedBy = "journalChapter", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    @Comment("저널 일기 목록")
    private List<JournalDiaryEntity> journalDiaryList;

    /** 저널 노트 목록 */
    @OneToMany(mappedBy = "journalChapter", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    @Comment("저널 노트 목록")
    private List<JournalNoteEntity> journalNoteList;

    /** 저널 꿈 목록 (chapter_type = DREAM인 경우) */
    @OneToMany(mappedBy = "journalChapter", cascade = CascadeType.ALL, orphanRemoval = true)
    @Where(clause = "else_dream_yn = 'N'")
    @OrderBy("sortOrder ASC")
    @Comment("저널 꿈 목록")
    private List<JournalDreamEntity> journalDreamList;

    /** 저널 꿈 (타인) 목록 (chapter_type = DREAM인 경우) */
    @OneToMany(mappedBy = "journalChapter", cascade = CascadeType.ALL, orphanRemoval = true)
    @Where(clause = "else_dream_yn = 'Y'")
    @OrderBy("sortOrder ASC")
    @Comment("저널 꿈 (타인) 목록")
    private List<JournalDreamEntity> journalElseDreamList;

    /** 인덱스 변경 여부 */
    @Builder.Default
    @Transient
    private Boolean isSortOrderChanged = false;

    /* ----- */

    /** 위임 :: 태그 정보 모듈 */
    @Embedded
    public TagEmbed tag;
    /** 위임 :: 상태 정보 모듈 */
    @Embedded
    public StateEmbed state;
}

