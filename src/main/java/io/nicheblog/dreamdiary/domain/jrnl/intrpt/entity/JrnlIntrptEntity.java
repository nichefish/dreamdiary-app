package io.nicheblog.dreamdiary.domain.jrnl.intrpt.entity;

import io.nicheblog.dreamdiary.domain.jrnl.dream.entity.JrnlDreamSmpEntity;
import io.nicheblog.dreamdiary.extension.clsf.ContentType;
import io.nicheblog.dreamdiary.extension.clsf.comment.entity.embed.CommentEmbed;
import io.nicheblog.dreamdiary.extension.clsf.comment.entity.embed.CommentEmbedModule;
import io.nicheblog.dreamdiary.extension.clsf.tag.entity.embed.TagEmbed;
import io.nicheblog.dreamdiary.extension.clsf.tag.entity.embed.TagEmbedModule;
import io.nicheblog.dreamdiary.global.intrfc.entity.BasePostEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * JrnlIntrptEntity
 * <pre>
 *  저널 해석 Entity.
 *  Entity that contains each distinct intrpt.
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(name = "jrnl_intrpt")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@Where(clause = "del_yn='N'")
@SQLDelete(sql = "UPDATE jrnl_intrpt SET del_yn = 'Y' WHERE post_no = ?")
@NamedEntityGraph(
    name = "JrnlIntrptEntity.withTags",
    attributeNodes = {
        @NamedAttributeNode(value = "tag", subgraph = "TagEmbed")
    },
    subgraphs = {
        @NamedSubgraph(
            name = "TagEmbed",
            attributeNodes = @NamedAttributeNode("list")  // tag.list 즉시 로딩
        )
    }
)
public class JrnlIntrptEntity
        extends BasePostEntity
        implements CommentEmbedModule, TagEmbedModule {

    /** 필수: 컨텐츠 타입 */
    @Builder.Default
    private static final ContentType CONTENT_TYPE = ContentType.JRNL_INTRPT;

    /** 저널 해석 고유 번호 (PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_no")
    @Comment("저널 해석 고유 번호")
    private Integer postNo;

    /** 컨텐츠 타입 */
    @Builder.Default
    @Column(name = "content_type", columnDefinition = "VARCHAR(50) DEFAULT 'JRNL_ENTRY'")
    @Comment("컨텐츠 타입")
    private String contentType = CONTENT_TYPE.key;

    /** 글분류 코드 :: join을 제거하고 메모리 캐시 처리 */
    @Column(name = "ctgr_cd", length = 50)
    @Comment("저널 해석 글분류 코드 정보")
    private String ctgrCd;

    /** 글분류 코드 이름 :: join을 제거하고 메모리 캐시 처리 */
    @Transient
    private String ctgrNm;

    /* ----- */

    /** 저널 꿈 번호  */
    @Column(name = "jrnl_dream_no")
    @Comment("저널 꿈 번호")
    private Integer jrnlDreamNo;

    /** 저널 꿈 정보 */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "jrnl_dream_no", referencedColumnName = "post_no", insertable = false, updatable = false)
    @Fetch(FetchMode.JOIN)
    @NotFound(action = NotFoundAction.IGNORE)
    @Comment("저널 꿈 정보")
    private JrnlDreamSmpEntity jrnlDream;

    /** 순번 */
    @Column(name = "idx", columnDefinition = "INT DEFAULT 1")
    private Integer idx;

    /** 정리완료 여부 (Y/N) */
    @Builder.Default
    @Column(name = "resolved_yn", length = 1, columnDefinition = "CHAR(1) DEFAULT 'N'")
    @Comment("정리완료 여부")
    private String resolvedYn = "N";

    /** 글접기 여부 (Y/N) */
    @Builder.Default
    @Column(name = "collapse_yn", length = 1, columnDefinition = "CHAR(1) DEFAULT 'N'")
    @Comment("글접기 여부")
    private String collapseYn = "N";

    /**
     * 인덱스 변경 여부
     */
    @Builder.Default
    @Transient
    private Boolean isIdxChanged = false;

    /* ----- */

    /** 위임 :: 댓글 정보 모듈 */
    @Embedded
    public CommentEmbed comment;
    /** 위임 :: 태그 정보 모듈 */
    @Embedded
    public TagEmbed tag;
}
