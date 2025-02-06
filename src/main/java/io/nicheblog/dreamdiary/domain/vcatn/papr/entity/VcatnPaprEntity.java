package io.nicheblog.dreamdiary.domain.vcatn.papr.entity;

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
import org.hibernate.annotations.*;
import org.springframework.util.CollectionUtils;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * VcatnPaprEntity
 * <pre>
 *  휴가계획서 Entity.
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(name = "vcatn_papr")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@AllArgsConstructor
@RequiredArgsConstructor
@ToString(callSuper = true)
@Where(clause = "del_yn='N'")
@SQLDelete(sql = "UPDATE vcatn_papr SET del_yn = 'Y' WHERE post_no = ?")
public class VcatnPaprEntity
        extends BasePostEntity
        implements CommentEmbedModule, TagEmbedModule, ManagtEmbedModule, ViewerEmbedModule {

    /** 필수: 컨텐츠 타입 */
    @Builder.Default
    private static final ContentType CONTENT_TYPE = ContentType.VCATN_PAPR;

    /** 글 번호 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_no")
    @Comment("글 번호")
    private Integer postNo;

    /** 컨텐츠 타입 */
    @Builder.Default
    @Column(name = "content_type", columnDefinition = "VARCHAR(50) DEFAULT 'VCATN_PAPR'")
    @Comment("컨텐츠 타입")
    private String contentType = CONTENT_TYPE.key;

    /** 글분류 코드 :: join을 제거하고 메모리 캐시 처리 */
    @Column(name = "ctgr_cd", length = 50)
    @Comment("휴가계획서 글분류 코드 정보")
    private String ctgrCd;

    /** 글분류 코드 이름 :: join을 제거하고 메모리 캐시 처리 */
    @Transient
    private String ctgrNm;
    
    /* ----- */

    /** 휴가 일정 목록 */
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumnOrFormula(column = @JoinColumn(name = "ref_post_no", referencedColumnName = "post_no"))
    @Fetch(FetchMode.SELECT)
    @BatchSize(size = 10)
    @OrderBy("bgnDt ASC")
    @NotFound(action = NotFoundAction.IGNORE)
    @Comment("휴가 일정 목록")
    private List<VcatnSchdulEntity> schdulList;

    /** 확인 여부 (Y/N) */
    @Builder.Default
    @Column(name = "cf_yn")
    @Comment("휴가계획서 확인 여부 (Y/N)")
    private String cfYn = "N";

    /* ----- */

    /**
     * 서브엔티티 List 처리를 위한 Setter Override
     * 한 번 Entity가 생성된 이후부터는 새 List를 할당하면 안 되고 계속 JPA 이력이 추적되어야 한다.
     *
     * @param itemList - 설정할 객체 리스트
     */
    public void setSchdulList(final List<VcatnSchdulEntity> itemList) {
        if (CollectionUtils.isEmpty(itemList)) return;
        if (this.schdulList == null) {
            this.schdulList = new ArrayList<>(itemList);
        } else {
            this.schdulList.clear();
            this.schdulList.addAll(itemList);
        }
    }

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
