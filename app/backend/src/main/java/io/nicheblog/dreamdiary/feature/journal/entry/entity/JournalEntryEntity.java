package io.nicheblog.dreamdiary.feature.journal.entry.entity;

import io.nicheblog.dreamdiary.feature.attachable._shared.entity.BaseAttachableEntity;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.comment.entity.embed.CommentEmbed;
import io.nicheblog.dreamdiary.feature.attachable.comment.entity.embed.CommentEmbedModule;
import io.nicheblog.dreamdiary.feature.attachable.history.entity.embed.HistoryEmbed;
import io.nicheblog.dreamdiary.feature.attachable.history.entity.embed.HistoryEmbedModule;
import io.nicheblog.dreamdiary.feature.attachable.prefix.entity.embed.PrefixEmbed;
import io.nicheblog.dreamdiary.feature.attachable.prefix.entity.embed.PrefixEmbedModule;
import io.nicheblog.dreamdiary.feature.attachable.state.entity.embed.StateEmbed;
import io.nicheblog.dreamdiary.feature.attachable.state.entity.embed.StateEmbedModule;
import io.nicheblog.dreamdiary.feature.attachable.tag.entity.embed.TagEmbed;
import io.nicheblog.dreamdiary.feature.attachable.tag.entity.embed.TagEmbedModule;
import io.nicheblog.dreamdiary.feature.file.entity.embed.FileEmbed;
import io.nicheblog.dreamdiary.feature.file.entity.embed.FileEmbedModule;
import io.nicheblog.dreamdiary.feature.journal.chapter.entity.JournalChapterSmpEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "journal_entry")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@Where(clause = "deleted_at IS NULL")
@SQLDelete(sql = "UPDATE journal_entry SET deleted_at = NOW() WHERE id = ?")
public class JournalEntryEntity
        extends BaseAttachableEntity
        implements FileEmbedModule, CommentEmbedModule, TagEmbedModule, StateEmbedModule,
        HistoryEmbedModule, PrefixEmbedModule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("journal entry id")
    private Integer id;

    @Column(name = "content_type", nullable = false, length = 32)
    @Comment("JOURNAL_DIARY | JOURNAL_DREAM")
    private String contentType;

    @Column(name = "title")
    private String title;

    @Column(name = "content")
    private String content;

    @Column(name = "journal_chapter_id")
    @Comment("journal chapter id")
    private Integer journalChapterId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "journal_chapter_id", referencedColumnName = "id", insertable = false, updatable = false)
    @Fetch(FetchMode.JOIN)
    @NotFound(action = NotFoundAction.IGNORE)
    @Comment("journal chapter")
    private JournalChapterSmpEntity journalChapter;

    @Column(name = "sort_order", columnDefinition = "INT DEFAULT 1")
    private Integer sortOrder;

    /** 지정 꿈꾼 이름. 값이 있으면 타인의 꿈으로 분류한다. */
    @Column(name = "dreamer_name", length = 64)
    private String dreamerName;

    /** target(해석 대상) 엔티티 번호. nullable. Reflection이 가리키는 대상 Entry를 표시위치로만 참조한다. */
    @Column(name = "ref_id")
    @Comment("target 엔티티 번호")
    private Integer refId;

    /** target 엔티티의 컨텐츠 타입 {JOURNAL_DIARY, JOURNAL_DREAM, JOURNAL_REFLECTION}. nullable. */
    @Enumerated(EnumType.STRING)
    @Column(name = "ref_content_type", length = 50)
    @Comment("target 컨텐츠 타입")
    private ContentType refContentType;

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

    /** 콘텐츠 말머리 선택. NOTE 엔트리도 영속 contentType은 JOURNAL_DIARY지만 Scope 검증은 챕터 타입으로 분리한다. */
    @Embedded
    public PrefixEmbed prefix;
}
