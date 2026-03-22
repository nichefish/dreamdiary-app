package io.nicheblog.dreamdiary.feature.clsf.sectn.entity;

import io.nicheblog.dreamdiary.feature.clsf.ContentType;
import io.nicheblog.dreamdiary.feature.clsf._shared.entity.BaseClsfEntity;
import io.nicheblog.dreamdiary.feature.clsf.comment.entity.embed.CommentEmbed;
import io.nicheblog.dreamdiary.feature.clsf.comment.entity.embed.CommentEmbedModule;
import io.nicheblog.dreamdiary.feature.clsf.file.entity.embed.AtchFileEmbed;
import io.nicheblog.dreamdiary.feature.clsf.file.entity.embed.AtchFileEmbedModule;
import io.nicheblog.dreamdiary.feature.clsf.tag.entity.embed.TagEmbed;
import io.nicheblog.dreamdiary.feature.clsf.tag.entity.embed.TagEmbedModule;
import io.nicheblog.dreamdiary.global.intrfc.entity.Usable;
import io.nicheblog.dreamdiary.infrastructure.cd.Code;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;

/**
 * SectnEntity
 * <pre>
 *  단락 Entity.
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(name = "sectn")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@Where(clause = "del_yn='N'")
@SQLDelete(sql = "UPDATE sectn SET del_yn = 'Y' WHERE post_no = ?")
public class SectnEntity
        extends BaseClsfEntity
        implements Usable, AtchFileEmbedModule, CommentEmbedModule, TagEmbedModule {

    /** 필수: 컨텐츠 타입 */
    @Builder.Default
    private static final ContentType CONTENT_TYPE = ContentType.SECTN;

    /** 내용 번호 (PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_no")
    @Comment("내용 번호 (key)")
    private Integer postNo;

    /** 컨텐츠 타입 */
    @Builder.Default
    @Column(name = "content_type", columnDefinition = "VARCHAR(50) DEFAULT 'SECTN'")
    @Comment("컨텐츠 타입")
    private String contentType = CONTENT_TYPE.key;

    /** 글분류 코드 :: join을 제거하고 메모리 캐시 처리 */
    @Column(name = "ctgr_cd", length = 50)
    @Comment("단락 글분류 코드 정보")
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

    /** 중요 여부 (Y/N) */
    @Builder.Default
    @Column(name = "imprtc_yn", length = 1, columnDefinition = "CHAR(1) DEFAULT 'N'")
    @Comment("중요 여부")
    protected String imprtcYn = "N";

    /** 상단고정 여부 (Y/N) */
    @Builder.Default
    @Column(name = "fxd_yn", length = 1, columnDefinition = "CHAR(1) DEFAULT 'N'")
    @Comment("상단고정 여부")
    protected String fxdYn = "N";

    /** 조회수 */
    @Builder.Default
    @Column(name = "hit_cnt")
    protected Integer hitCnt = 0;

    /** 수정권한 */
    @Builder.Default
    @Column(name = "mdfable")
    @Comment("수정권한")
    private String mdfable = Code.MDFABLE_REGSTR;

    /* ----- */

    /** 원글 번호 */
    @Column(name = "ref_post_no")
    @Comment("원글 번호")
    private Integer refPostNo;

    /** 원글 컨텐츠 타입 */
    @Column(name = "ref_content_type")
    @Comment("원글 컨텐츠 타입")
    private String refContentType;

    /** 정렬 순서 */
    @Column(name = "idx", columnDefinition = "INT DEFAULT 0")
    private Integer idx;

    /** 사용 여부 (Y/N) */
    @Builder.Default
    @Column(name = "use_yn", length = 1, columnDefinition = "CHAR DEFAULT 'Y'")
    private String useYn = "N";

    /* ----- */

    /** 만료 여부 (Y/N) */
    @Builder.Default
    @Column(name = "deprc_yn", length = 1, columnDefinition = "CHAR DEFAULT 'N'")
    @Comment("만료 여부")
    private String deprcYn = "N";

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
}
