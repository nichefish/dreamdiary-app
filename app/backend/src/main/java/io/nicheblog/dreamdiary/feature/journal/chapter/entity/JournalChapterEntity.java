package io.nicheblog.dreamdiary.feature.journal.chapter.entity;

import io.nicheblog.dreamdiary.feature.attachable._shared.entity.BaseAttachableEntity;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.prefix.entity.embed.PrefixEmbed;
import io.nicheblog.dreamdiary.feature.attachable.prefix.entity.embed.PrefixEmbedModule;
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
        implements StateEmbedModule, PrefixEmbedModule {

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

    /**
     * 시스템 요약 챕터 여부.
     * 사용자 선택 Prefix와 분리하여, 말머리 변경이 요약 동작을 바꾸지 않게 한다.
     */
    @Builder.Default
    @Column(name = "summary_yn", nullable = false, length = 1, columnDefinition = "CHAR(1) DEFAULT 'N'")
    @Comment("system summary chapter (Y/N)")
    private String summaryYn = "N";

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

    /**
     * 일반 챕터가 선택한 개인 말머리(0..1).
     * 시스템 요약·DREAM 챕터는 선택하지 않으며, 연결은 prefix_content에 저장한다.
     */
    @Embedded
    public PrefixEmbed prefix;

    @Embedded
    public StateEmbed state;
}
