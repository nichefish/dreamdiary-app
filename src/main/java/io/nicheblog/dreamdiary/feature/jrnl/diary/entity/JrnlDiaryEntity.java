package io.nicheblog.dreamdiary.feature.jrnl.diary.entity;

import io.nicheblog.dreamdiary.feature.clsf._shared.entity.BaseClsfEntity;
import io.nicheblog.dreamdiary.feature.clsf._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.clsf.comment.entity.embed.CommentEmbed;
import io.nicheblog.dreamdiary.feature.clsf.comment.entity.embed.CommentEmbedModule;
import io.nicheblog.dreamdiary.feature.clsf.file.entity.embed.AtchFileEmbed;
import io.nicheblog.dreamdiary.feature.clsf.file.entity.embed.AtchFileEmbedModule;
import io.nicheblog.dreamdiary.feature.clsf.history.entity.embed.HistoryEmbed;
import io.nicheblog.dreamdiary.feature.clsf.history.entity.embed.HistoryEmbedModule;
import io.nicheblog.dreamdiary.feature.clsf.state.entity.embed.StateEmbed;
import io.nicheblog.dreamdiary.feature.clsf.state.entity.embed.StateEmbedModule;
import io.nicheblog.dreamdiary.feature.clsf.tag.entity.embed.TagEmbed;
import io.nicheblog.dreamdiary.feature.clsf.tag.entity.embed.TagEmbedModule;
import io.nicheblog.dreamdiary.feature.jrnl.chapter.entity.JrnlChapterEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;

/**
 * JrnlDiaryEntity
 * <pre>
 *  저널 일기 Entity.
 *  Entity that contains each distinct diary.
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(name = "jrnl_diary")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@Where(clause = "del_yn='N'")
@SQLDelete(sql = "UPDATE jrnl_diary SET del_yn = 'Y' WHERE post_no = ?")
public class JrnlDiaryEntity
        extends BaseClsfEntity
        implements AtchFileEmbedModule, CommentEmbedModule, TagEmbedModule, StateEmbedModule, HistoryEmbedModule {

    /** 필수: 컨텐츠 타입 */
    @Builder.Default
    private static final ContentType CONTENT_TYPE = ContentType.JRNL_DIARY;

    /** 저널 꿈 고유 번호 (PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_no")
    @Comment("저널 일기 고유 번호")
    private Integer postNo;

    /** 컨텐츠 타입 */
    @Builder.Default
    @Column(name = "content_type", columnDefinition = "VARCHAR(50) DEFAULT 'JRNL_DIARY'")
    @Comment("컨텐츠 타입")
    private String contentType = CONTENT_TYPE.key;

    /** 제목 */
    @Column(name = "title")
    private String title;

    /** 내용 */
    @Column(name = "cn")
    private String cn;

    /* ----- */

    /** 저널 챕터 정보 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jrnl_chapter_no", nullable = false)
    @Comment("저널 챕터 정보")
    private JrnlChapterEntity jrnlChapter;

    /** 순번 */
    @Column(name = "idx", columnDefinition = "INT DEFAULT 1")
    private Integer idx;

    /** 인덱스 변경 여부 */
    @Builder.Default
    @Transient
    private Boolean isIdxChanged = false;

    /** 저널 챕터 변경 여부 */
    @Builder.Default
    @Transient
    private Boolean isChapterChanged = false;

    /** 이전 저널 챕터 번호 */
    @Transient
    private Integer prevJrnlChapterNo;

    /* ----- */

    /** 위임 :: 첨부파일 모듈 */
    @Embedded
    public AtchFileEmbed file;
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
