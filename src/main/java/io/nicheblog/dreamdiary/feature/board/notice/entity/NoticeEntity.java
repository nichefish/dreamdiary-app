package io.nicheblog.dreamdiary.feature.board.notice.entity;

import io.nicheblog.dreamdiary.feature.attachable._shared.entity.BaseAttachableEntity;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.comment.entity.embed.CommentEmbed;
import io.nicheblog.dreamdiary.feature.attachable.comment.entity.embed.CommentEmbedModule;
import io.nicheblog.dreamdiary.feature.attachable.managt.entity.embed.ManagtEmbed;
import io.nicheblog.dreamdiary.feature.attachable.managt.entity.embed.ManagtEmbedModule;
import io.nicheblog.dreamdiary.feature.attachable.tag.entity.embed.TagEmbed;
import io.nicheblog.dreamdiary.feature.attachable.tag.entity.embed.TagEmbedModule;
import io.nicheblog.dreamdiary.feature.attachable.viewer.entity.embed.ViewerEmbed;
import io.nicheblog.dreamdiary.feature.attachable.viewer.entity.embed.ViewerEmbedModule;
import io.nicheblog.dreamdiary.feature.file.entity.embed.FileEmbed;
import io.nicheblog.dreamdiary.feature.file.entity.embed.FileEmbedModule;
import io.nicheblog.dreamdiary.infrastructure.code.Code;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;

/**
 * NoticeEntity
 * <pre>
 *  공지사항 Entity.
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(name = "notice")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@Where(clause = "deleted_at IS NULL")
@SQLDelete(sql = "UPDATE notice SET deleted_at = NOW() WHERE id = ?")
public class NoticeEntity
        extends BaseAttachableEntity
        implements FileEmbedModule, CommentEmbedModule, TagEmbedModule, ManagtEmbedModule, ViewerEmbedModule {

    /** 필수: 컨텐츠 타입 */
    @Builder.Default
    private static final ContentType CONTENT_TYPE = ContentType.NOTICE;

    /** 글 번호 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("공지사항 번호")
    private Integer id;

    /** 컨텐츠 타입 */
    @Builder.Default
    @Column(name = "content_type", columnDefinition = "VARCHAR(50) DEFAULT 'NOTICE'")
    @Comment("컨텐츠 타입")
    private String contentType = CONTENT_TYPE.key;

    /** 글분류 코드 :: join을 제거하고 메모리 캐시 처리 */
    @Column(name = "ctgr_cd", length = 50)
    private String ctgrCd;

    /** 글분류 코드 이름 :: join을 제거하고 메모리 캐시 처리 */
    @Transient
    private String ctgrNm;

    /** 제목 */
    @Column(name = "title")
    private String title;

    /** 내용 */
    @Column(name = "content")
    private String content;

    /* ----- */

    /** 중요 여부 (Y/N) */
    @Builder.Default
    @Column(name = "imprtc_yn", length = 1, columnDefinition = "CHAR(1) DEFAULT 'N'")
    @Comment("중요 여부")
    private String imprtcYn = "N";

    /** 상단고정 여부 (Y/N) */
    @Builder.Default
    @Column(name = "fxd_yn", length = 1, columnDefinition = "CHAR(1) DEFAULT 'N'")
    @Comment("상단고정 여부")
    private String fxdYn = "N";

    /** 조회수 */
    @Builder.Default
    @Column(name = "hit_cnt")
    private Integer hitCnt = 0;

    /** 수정권한 */
    @Builder.Default
    @Column(name = "mdfable")
    @Comment("수정권한")
    private String mdfable = Code.MDFABLE_REGSTR;

    /* ----- */

    /** 팝업 노출 여부 (Y/N) */
    @Builder.Default
    @Column(name = "popup_yn", length = 1, columnDefinition = "CHAR(1) DEFAULT 'N'")
    @Comment("팝업 노출여부")
    private String popupYn = "N";

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
    /** 위임 :: 조치 정보 모듈 */
    @Embedded
    public ManagtEmbed managt;
    /** 위임 :: 열람 정보 모듈 */
    @Embedded
    public ViewerEmbed viewer;
}

