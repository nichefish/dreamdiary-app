package io.nicheblog.dreamdiary.feature.jrnl.sumry.entity;

import io.nicheblog.dreamdiary.feature.clsf._shared.entity.BaseClsfEntity;
import io.nicheblog.dreamdiary.feature.clsf._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.clsf.tag.entity.embed.TagEmbed;
import io.nicheblog.dreamdiary.feature.clsf.tag.entity.embed.TagEmbedModule;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import java.util.List;

/**
 * JrnlSumryEntity
 * <pre>
 *  저널 결산 Entity.
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(name = "jrnl_sumry")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@Where(clause = "del_yn='N'")
@SQLDelete(sql = "UPDATE jrnl_sumry SET del_yn = 'Y' WHERE id = ?")
public class JrnlSumryEntity
        extends BaseClsfEntity
        implements TagEmbedModule {

    /** 저널 꿈 고유 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("저널 결산 고유 번호")
    private Integer id;

    /** 필수: 컨텐츠 타입 */
    @Builder.Default
    @Column(name = "content_type", columnDefinition = "VARCHAR(50) DEFAULT 'JRNL_SUMRY'")

    @Comment("컨텐츠 타입")
    private String contentType = ContentType.JRNL_SUMRY.key;

    /** 제목 */
    @Column(name = "title")
    private String title;

    /** 내용 */
    @Column(name = "cn")
    private String cn;

    /* ----- */

    /** 꿈 기록 완료 여부 (Y/N) */
    @Builder.Default
    @Column(name = "dream_compt_yn", length = 1, columnDefinition = "CHAR(1) DEFAULT 'N'")
    @Comment("꿈 기록 완료 여부 (Y/N)")
    private String dreamComptYn = "N";

    /* ----- */

    /** 결산 년도 */
    @Column(name = "yy", unique = true)
    private Integer yy;

    /** 꿈 일수 */
    @Column(name = "dream_day_cnt")
    private Integer dreamDayCnt;

    /** 꿈 갯수 */
    @Column(name = "dream_cnt")
    private Integer dreamCnt;

    /** 저널 리뷰 목록 */
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "jrnl_sumry_id", referencedColumnName = "id", insertable = false, updatable = false)
    @Fetch(FetchMode.SUBSELECT)
    @BatchSize(size = 10)
    @OrderBy("idx ASC")
    @NotFound(action = NotFoundAction.IGNORE)
    @Comment("저널 결산 리뷰 목록")
    private List<JrnlSumryReviewEntity> jrnlSumryReviewList;

    /* ----- */

    /**
     * 생성자.
     *
     * @param yy - 생성할 엔티티에 설정할 연도 값
     */
    public JrnlSumryEntity(final Integer yy) {
        this.yy = yy;
    }

    /* ----- */

    /** 위임 :: 태그 정보 모듈 */
    @Embedded
    public TagEmbed tag;
}
