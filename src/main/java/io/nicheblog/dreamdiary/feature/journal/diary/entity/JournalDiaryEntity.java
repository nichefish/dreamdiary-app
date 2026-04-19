package io.nicheblog.dreamdiary.feature.journal.diary.entity;

import io.nicheblog.dreamdiary.feature.attachable._shared.entity.BaseAttachableEntity;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.comment.entity.embed.CommentEmbed;
import io.nicheblog.dreamdiary.feature.attachable.comment.entity.embed.CommentEmbedModule;
import io.nicheblog.dreamdiary.feature.attachable.history.entity.embed.HistoryEmbed;
import io.nicheblog.dreamdiary.feature.attachable.history.entity.embed.HistoryEmbedModule;
import io.nicheblog.dreamdiary.feature.attachable.state.entity.embed.StateEmbed;
import io.nicheblog.dreamdiary.feature.attachable.state.entity.embed.StateEmbedModule;
import io.nicheblog.dreamdiary.feature.attachable.tag.entity.embed.TagEmbed;
import io.nicheblog.dreamdiary.feature.attachable.tag.entity.embed.TagEmbedModule;
import io.nicheblog.dreamdiary.feature.file.entity.embed.FileEmbed;
import io.nicheblog.dreamdiary.feature.file.entity.embed.FileEmbedModule;
import io.nicheblog.dreamdiary.feature.journal.chapter.entity.JournalChapterEntity;
import io.nicheblog.dreamdiary.feature.journal.interpretation.entity.JournalInterpretationEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.List;

/**
 * JournalDiaryEntity
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
        extends BaseAttachableEntity
        implements FileEmbedModule, CommentEmbedModule, TagEmbedModule, StateEmbedModule, HistoryEmbedModule {

    @Builder.Default
    private static final ContentType CONTENT_TYPE = ContentType.JOURNAL_DIARY;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("journal diary id")
    private Integer id;

    @Builder.Default
    @Column(name = "content_type", columnDefinition = "VARCHAR(50) DEFAULT 'JOURNAL_DIARY'")
    @Comment("content type")
    private String contentType = CONTENT_TYPE.key;

    @Column(name = "title")
    private String title;

    @Column(name = "content")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journal_chapter_id", nullable = false)
    @Comment("journal chapter")
    private JournalChapterEntity journalChapter;

    @Column(name = "sort_order", columnDefinition = "INT DEFAULT 1")
    private Integer sortOrder;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "ref_id", referencedColumnName = "id", insertable = false, updatable = false)
    @Where(clause = "ref_content_type = 'JOURNAL_DIARY'")
    @OrderBy("sortOrder ASC")
    private List<JournalInterpretationEntity> journalInterpretationList;

    @Builder.Default
    @Transient
    private Boolean isSortOrderChanged = false;

    @Builder.Default
    @Transient
    private Boolean isChapterChanged = false;

    @Transient
    private Integer prevJournalChapterId;

    @Embedded
    public FileEmbed file;

    @Embedded
    public CommentEmbed comment;

    @Embedded
    public TagEmbed tag;

    @Embedded
    public StateEmbed state;

    @Embedded
    public HistoryEmbed history;
}
