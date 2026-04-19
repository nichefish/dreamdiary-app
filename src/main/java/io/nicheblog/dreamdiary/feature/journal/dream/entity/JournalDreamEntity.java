package io.nicheblog.dreamdiary.feature.journal.dream.entity;

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
import io.nicheblog.dreamdiary.feature.journal.chapter.entity.JournalChapterSmpEntity;
import io.nicheblog.dreamdiary.feature.journal.interpretation.entity.JournalInterpretationEntity;
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
 * JournalDreamEntity
 * <pre>
 *  저널 꿈 Entity.
 *  Entity that contains each distinct dream.
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(name = "journal_dream")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@Where(clause = "deleted_at IS NULL")
@SQLDelete(sql = "UPDATE journal_dream SET deleted_at = NOW() WHERE id = ?")
public class JournalDreamEntity
        extends BaseAttachableEntity
        implements FileEmbedModule, CommentEmbedModule, TagEmbedModule, StateEmbedModule, HistoryEmbedModule {

    /** 필수: 컨텐츠 타입 */
    @Builder.Default
    private static final ContentType CONTENT_TYPE = ContentType.JOURNAL_DREAM;

    /** 저널 꿈 고유 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("저널 꿈 고유 번호")
    private Integer id;

    /** 컨텐츠 타입 */
    @Builder.Default
    @Column(name = "content_type", columnDefinition = "VARCHAR(50) DEFAULT 'JOURNAL_DREAM'")
    @Comment("컨텐츠 타입")
    private String contentType = CONTENT_TYPE.key;

    /** 제목 */
    @Column(name = "title")
    private String title;

    /** 내용 */
    @Column(name = "content")
    private String content;

    /* ----- */

    /** 저널 챕터 번호 */
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

    /** 악몽 여부 (Y/N) */
    @Builder.Default
    @Column(name = "nhtmr_yn", length = 1, columnDefinition = "CHAR(1) DEFAULT 'N'")
    @Comment("악몽 여부")
    private String nhtmrYn = "N";

    /** 입면환각 여부 (Y/N) */
    @Builder.Default
    @Column(name = "halluc_yn", length = 1, columnDefinition = "CHAR(1) DEFAULT 'N'")
    @Comment("입면환각 여부")
    private String hallucYn = "N";

    /** 타인 꿈 여부 (Y/N) */
    @Builder.Default
    @Column(name = "else_dream_yn", length = 1, columnDefinition = "CHAR(1) DEFAULT 'N'")
    @Comment("타인 꿈 여부")
    private String elseDreamYn = "N";

    /** 꿈꾼이(타인) 이름 */
    @Column(name = "else_dreamer_nm", length = 64)
    private String elseDreamerNm;

    /** 저널 해석 목록 */
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "ref_id", referencedColumnName = "id", insertable = false, updatable = false)
    @Where(clause = "ref_content_type = 'JOURNAL_DREAM'")
    @OrderBy("sortOrder ASC")
    @Comment("저널 해석 목록")
    private List<JournalInterpretationEntity> journalInterpretationList;

    /** 인덱스 변경 여부 */
    @Builder.Default
    @Transient
    private Boolean isSortOrderChanged = false;

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
    /** 위임 :: 이력 정보 모듈 */
    @Embedded
    public HistoryEmbed history;
}

