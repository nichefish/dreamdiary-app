package io.nicheblog.dreamdiary.feature.journal.diary.entity;

import io.nicheblog.dreamdiary.feature.clsf._shared.entity.BaseClsfEntity;
import io.nicheblog.dreamdiary.feature.clsf._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.clsf.comment.entity.embed.CommentEmbed;
import io.nicheblog.dreamdiary.feature.clsf.comment.entity.embed.CommentEmbedModule;
import io.nicheblog.dreamdiary.feature.file.entity.embed.FileEmbed;
import io.nicheblog.dreamdiary.feature.file.entity.embed.FileEmbedModule;
import io.nicheblog.dreamdiary.feature.clsf.history.entity.embed.HistoryEmbed;
import io.nicheblog.dreamdiary.feature.clsf.history.entity.embed.HistoryEmbedModule;
import io.nicheblog.dreamdiary.feature.clsf.state.entity.embed.StateEmbed;
import io.nicheblog.dreamdiary.feature.clsf.state.entity.embed.StateEmbedModule;
import io.nicheblog.dreamdiary.feature.clsf.tag.entity.embed.TagEmbed;
import io.nicheblog.dreamdiary.feature.clsf.tag.entity.embed.TagEmbedModule;
import io.nicheblog.dreamdiary.feature.journal.chapter.entity.JournalChapterEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;

/**
 * JournalDiaryEntity
 * <pre>
 *  저널 일기 Entity.
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
public class JournalDiaryEntity
        extends BaseClsfEntity
        implements FileEmbedModule, CommentEmbedModule, TagEmbedModule, StateEmbedModule, HistoryEmbedModule {

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

    /** 제목 */
    @Column(name = "title")
    private String title;

    /** 내용 */
    @Column(name = "content")
    private String content;

    /* ----- */

    /** 저널 챕터 정보 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journal_chapter_id", nullable = false)
    @Comment("저널 챕터 정보")
    private JournalChapterEntity journalChapter;

    /** 순번 */
    @Column(name = "sort_order", columnDefinition = "INT DEFAULT 1")
    private Integer sortOrder;

    /** 인덱스 변경 여부 */
    @Builder.Default
    @Transient
    private Boolean isSortOrderChanged = false;

    /** 저널 챕터 변경 여부 */
    @Builder.Default
    @Transient
    private Boolean isChapterChanged = false;

    /** 이전 저널 챕터 번호 */
    @Transient
    private Integer prevJournalChapterId;

    /* ----- */

    /** 위임 :: 첨부파일 모듈 */
    @Embedded
    public FileEmbed file;
    /** 위임 :: 댓글 정보 모듈 */
    @Embedded
    public CommentEmbed comment;
    /** 위임 :: 태그 정보 모듈 */
    @Embedded
    public TagEmbed tag;
    /** 위임 :: 상태 정보 모듈 */
    @Embedded
    public StateEmbed state;
    @Embedded
    public HistoryEmbed history;
}

