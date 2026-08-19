package io.nicheblog.dreamdiary.feature.journal.reflection.entity;

import io.nicheblog.dreamdiary.feature.attachable._shared.entity.BaseAttachableEntity;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.comment.entity.embed.CommentEmbed;
import io.nicheblog.dreamdiary.feature.attachable.comment.entity.embed.CommentEmbedModule;
import io.nicheblog.dreamdiary.feature.attachable.history.entity.embed.HistoryEmbed;
import io.nicheblog.dreamdiary.feature.attachable.history.entity.embed.HistoryEmbedModule;
import io.nicheblog.dreamdiary.feature.attachable.state.entity.embed.StateEmbed;
import io.nicheblog.dreamdiary.feature.attachable.state.entity.embed.StateEmbedModule;
import io.nicheblog.dreamdiary.feature.file.entity.embed.FileEmbed;
import io.nicheblog.dreamdiary.feature.file.entity.embed.FileEmbedModule;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;

/**
 * 저널 Reflection(Commentary) Entity.
 *
 * <p>Reflection 은 Primary Content(Diary/Dream/Note)에 대한 해석 층(Commentary)이며, 반드시 대상
 * Entry 를 가리키는(About-A) 별도 Aggregate Root 다. Primary 스트림의 peer 가 아니므로 chapter·day 를
 * 소유하지 않고, 대상({@code refId}/{@code refContentType})을 필수로 갖는다.
 * 같은 대상 아래 형제 순번은 {@code sortOrder} 가 담당한다.</p>
 *
 * <p>골격은 흡수 이전 {@code JournalInterpretation} 을 잇는다(별도 테이블·attachable owner). 흡수가 얹은
 * chapter 소유·STI 는 버리고, About-A 를 NOT NULL 로 조인다. content_type 은 전용 테이블이라 항상
 * {@code JOURNAL_REFLECTION} 이며, attachable(state·comment) 조인의 {@code referencedColumnName}
 * 이라 실제 컬럼으로 저장한다.</p>
 *
 * <p>설계 정본: {@code docs/migration/journal/reflection-domain-model.md},
 * {@code reflection-persistence-proposal.md}. 이 클래스는 되가르기 R1(스키마 축) 스캐폴딩이며, 읽기/쓰기
 * 경로 배선은 R3/R4 에서 붙인다.</p>
 */
@Entity
@Table(name = "journal_reflection")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@Where(clause = "deleted_at IS NULL")
@SQLDelete(sql = "UPDATE journal_reflection SET deleted_at = NOW() WHERE id = ?")
public class JournalReflectionEntity
        extends BaseAttachableEntity
        implements FileEmbedModule, CommentEmbedModule, StateEmbedModule, HistoryEmbedModule {

    /** Reflection ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("reflection id")
    private Integer id;

    /** 컨텐츠 타입. 전용 테이블이라 항상 JOURNAL_REFLECTION. attachable join 의 referencedColumnName 이라 저장. */
    @Builder.Default
    @Column(name = "content_type", columnDefinition = "VARCHAR(50) DEFAULT 'JOURNAL_REFLECTION'")
    @Comment("컨텐츠 타입 (항상 JOURNAL_REFLECTION)")
    private String contentType = ContentType.JOURNAL_REFLECTION.key;

    /** 제목 (nullable) */
    @Column(name = "title")
    @Comment("제목")
    private String title;

    /** 본문(사유) */
    @Column(name = "content")
    @Comment("Reflection 본문(사유)")
    private String content;

    /* ----- target (About-A) ----- */

    /** 대상(About-A) 엔티티 번호. 필수 — Reflection 은 반드시 대상을 가리킨다. */
    @Column(name = "ref_id", nullable = false)
    @Comment("대상(About-A) 엔티티 번호")
    private Integer refId;

    /** 대상 엔티티의 컨텐츠 타입 {JOURNAL_DIARY, JOURNAL_DREAM, JOURNAL_REFLECTION}. 필수. */
    @Enumerated(EnumType.STRING)
    @Column(name = "ref_content_type", nullable = false, length = 50)
    @Comment("대상 컨텐츠 타입")
    private ContentType refContentType;

    /** 같은 대상 아래 형제 정렬 순번. 1부터 연속. */
    @Builder.Default
    @Column(name = "sort_order", columnDefinition = "INT DEFAULT 1")
    @Comment("대상 아래 정렬")
    private Integer sortOrder = 1;

    /* ----- attachable embed (owner 컬럼: File=file_group_id, History=history_triggered_*; State·Comment 는 별도 테이블) ----- */

    /** 위임 :: 첨부파일 모듈 */
    @Embedded
    public FileEmbed file;
    /** 위임 :: 댓글 정보 모듈 */
    @Embedded
    public CommentEmbed comment;
    /** 위임 :: 상태 정보 모듈 */
    @Embedded
    public StateEmbed state;
    /** 위임 :: 이력 정보 모듈 */
    @Embedded
    public HistoryEmbed history;
}