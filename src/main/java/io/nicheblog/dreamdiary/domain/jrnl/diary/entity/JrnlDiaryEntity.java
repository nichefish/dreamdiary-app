package io.nicheblog.dreamdiary.domain.jrnl.diary.entity;

import io.nicheblog.dreamdiary.domain.jrnl.entry.entity.JrnlEntrySmpEntity;
import io.nicheblog.dreamdiary.extension.clsf.ContentType;
import io.nicheblog.dreamdiary.extension.clsf.comment.entity.embed.CommentEmbed;
import io.nicheblog.dreamdiary.extension.clsf.comment.entity.embed.CommentEmbedModule;
import io.nicheblog.dreamdiary.extension.clsf.state.entity.embed.StateEmbed;
import io.nicheblog.dreamdiary.extension.clsf.state.entity.embed.StateEmbedModule;
import io.nicheblog.dreamdiary.extension.clsf.tag.entity.embed.TagEmbed;
import io.nicheblog.dreamdiary.extension.clsf.tag.entity.embed.TagEmbedModule;
import io.nicheblog.dreamdiary.global.intrfc.entity.BaseClsfEntity;
import io.nicheblog.dreamdiary.global.intrfc.entity.embed.AtchFileEmbed;
import io.nicheblog.dreamdiary.global.intrfc.entity.embed.AtchFileEmbedModule;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;

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
        implements AtchFileEmbedModule, CommentEmbedModule, TagEmbedModule, StateEmbedModule {

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

    /** 글분류 코드 :: join을 제거하고 메모리 캐시 처리 */
    @Column(name = "ctgr_cd", length = 50)
    @Comment("저널 일기 글분류 코드 정보")
    private String ctgrCd;

    /** 글분류 코드 이름 :: join을 제거하고 메모리 캐시 처리 */
    @Transient
    private String ctgrNm;

    /** 제목 */
    @Column(name = "title")
    protected String title;

    /** 내용 */
    @Column(name = "cn")
    protected String cn;

    /* ----- */

    /* ----- */

    /** 저널 항목 번호  */
    @Column(name = "jrnl_entry_no")
    @Comment("저널 항목 번호")
    private Integer jrnlEntryNo;

    /** 저널 항목 정보 */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "jrnl_entry_no", referencedColumnName = "post_no", insertable = false, updatable = false)
    @Fetch(FetchMode.JOIN)
    @NotFound(action = NotFoundAction.IGNORE)
    @Comment("저널 항목 정보")
    private JrnlEntrySmpEntity jrnlEntry;

    /** 순번 */
    @Column(name = "idx", columnDefinition = "INT DEFAULT 1")
    private Integer idx;

    /** 중요 여부 (Y/N) */
    @Builder.Default
    @Column(name = "imprtc_yn", length = 1, columnDefinition = "CHAR(1) DEFAULT 'N'")
    @Comment("중요 여부")
    protected String imprtcYn = "N";

    /** 정리완료 여부 (Y/N) */
    @Builder.Default
    @Column(name = "resolved_yn", length = 1, columnDefinition = "CHAR(1) DEFAULT 'N'")
    @Comment("정리완료 여부")
    private String resolvedYn = "N";

    /** 글접기 여부 (Y/N) */
    @Builder.Default
    @Column(name = "collapsed_yn", length = 1, columnDefinition = "CHAR(1) DEFAULT 'N'")
    @Comment("글접기 여부")
    private String collapsedYn = "N";

    /**
     * 인덱스 변경 여부
     */
    @Builder.Default
    @Transient
    private Boolean isIdxChanged = false;

    /**
     * 저널 항목 변경 여부
     */
    @Builder.Default
    @Transient
    private Boolean isEntryChanged = false;

    /**
     * 이전 저널 항목 번호
     */
    @Transient
    private Integer prevJrnlEntryNo;

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
}
