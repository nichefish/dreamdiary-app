package io.nicheblog.dreamdiary.feature.journal.chapter.entity;

import io.nicheblog.dreamdiary.feature.attachable._shared.entity.BaseAttachableEntity;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.state.entity.embed.StateEmbed;
import io.nicheblog.dreamdiary.feature.attachable.state.entity.embed.StateEmbedModule;
import io.nicheblog.dreamdiary.feature.journal.chapter.type.ChapterType;
import io.nicheblog.dreamdiary.feature.journal.day.entity.JournalDaySmpEntity;
import io.nicheblog.dreamdiary.feature.journal.entry.entity.JournalEntryEntity;
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
 *  Journal chapter aggregate root.
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
        implements StateEmbedModule {

    @Builder.Default
    private static final ContentType CONTENT_TYPE = ContentType.JOURNAL_CHAPTER;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("journal chapter id")
    private Integer id;

    @Builder.Default
    @Column(name = "content_type", columnDefinition = "VARCHAR(50) DEFAULT 'JOURNAL_CHAPTER'")
    @Comment("content type")
    private String contentType = CONTENT_TYPE.key;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "chapter_type", length = 30, columnDefinition = "VARCHAR(30) DEFAULT 'DIARY'")
    @Comment("chapter type")
    private ChapterType chapterType = ChapterType.DIARY;

    @Column(name = "title")
    private String title;

    @Column(name = "category_code", length = 50)
    @Comment("journal chapter category code")
    private String categoryCode;

    @Transient
    private String categoryName;

    @Column(name = "journal_day_id")
    @Comment("journal day id")
    private Integer journalDayId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "journal_day_id", referencedColumnName = "id", insertable = false, updatable = false)
    @Fetch(FetchMode.JOIN)
    @NotFound(action = NotFoundAction.IGNORE)
    @Comment("journal day")
    private JournalDaySmpEntity journalDay;

    @Column(name = "sort_order", columnDefinition = "INT DEFAULT 1")
    private Integer sortOrder;

    @OneToMany(mappedBy = "journalChapter", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    @Comment("journal entry list")
    private List<JournalEntryEntity> journalEntryList;

    @Builder.Default
    @Transient
    private Boolean isSortOrderChanged = false;

    @Embedded
    public StateEmbed state;
}
