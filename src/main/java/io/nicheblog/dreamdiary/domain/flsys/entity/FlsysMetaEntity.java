package io.nicheblog.dreamdiary.domain.flsys.entity;

import io.nicheblog.dreamdiary.extension.ContentType;
import io.nicheblog.dreamdiary.extension.comment.entity.embed.CommentEmbed;
import io.nicheblog.dreamdiary.extension.comment.entity.embed.CommentEmbedModule;
import io.nicheblog.dreamdiary.extension.managt.entity.embed.ManagtEmbed;
import io.nicheblog.dreamdiary.extension.managt.entity.embed.ManagtEmbedModule;
import io.nicheblog.dreamdiary.extension.tag.entity.embed.TagEmbed;
import io.nicheblog.dreamdiary.extension.tag.entity.embed.TagEmbedModule;
import io.nicheblog.dreamdiary.extension.viewer.entity.embed.ViewerEmbed;
import io.nicheblog.dreamdiary.extension.viewer.entity.embed.ViewerEmbedModule;
import io.nicheblog.dreamdiary.global.intrfc.entity.BasePostEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;

/**
 * FlsysMetaEntity
 * <pre>
 *  파일시스템 메타정보 Entity
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(name = "flsys_meta")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@AllArgsConstructor
@RequiredArgsConstructor
@Where(clause = "del_yn='N'")
@SQLDelete(sql = "UPDATE flsys_meta SET del_yn = 'Y' WHERE post_no = ?")
public class FlsysMetaEntity
        extends BasePostEntity
        implements CommentEmbedModule, TagEmbedModule, ManagtEmbedModule, ViewerEmbedModule {

    /** 필수: 컨텐츠 타입 */
    @Builder.Default
    private static final ContentType CONTENT_TYPE = ContentType.FLSYS_META;

    /** 글 번호 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_no")
    @Comment("글 번호")
    private Integer postNo;

    /** 컨텐츠 타입 */
    @Builder.Default
    @Column(name = "content_type", columnDefinition = "VARCHAR(50) DEFAULT 'FLSYS_META'")
    @Comment("컨텐츠 타입")
    private String contentType = CONTENT_TYPE.key;

    /** 글분류 코드 :: join을 제거하고 메모리 캐시 처리 */
    @Column(name = "ctgr_cd", length = 50)
    @Comment("파일시스템 메타 글분류 코드 정보")
    private String ctgrCd;

    /** 글분류 코드 이름 :: join을 제거하고 메모리 캐시 처리 */
    @Transient
    private String ctgrNm;

    /* ----- */

    /** 파일절대경로 */
    @Column(name = "file_path", length = 500)
    @Comment("파일 경로")
    private String filePath;

    /** 상위파일절대경로 */
    @Column(name = "upper_file_path", length = 500)
    @Comment("상위 파일 경로")
    private String upperFilePath;

    /* ----- */

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
