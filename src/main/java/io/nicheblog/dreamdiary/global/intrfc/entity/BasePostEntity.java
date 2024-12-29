package io.nicheblog.dreamdiary.global.intrfc.entity;

import io.nicheblog.dreamdiary.global.Constant;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

/**
 * BasePostEntity
 * <pre>
 *  (공통/상속) 게시판 속성 Entity.
 *  "All classes in the hierarchy must be annotated with @SuperBuilder."
 * </pre>
 *
 * @author nichefish
 */
@MappedSuperclass
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
public class BasePostEntity
        extends BaseClsfEntity {

    /** 제목 */
    @Column(name = "title")
    protected String title;

    /** 내용 */
    @Column(name = "cn")
    protected String cn;

    /** 글분류 코드 */
    @Column(name = "ctgr_cd", length = 20)
    @Comment("글분류 코드")
    protected String ctgrCd;

    /** 글분류 코드 이름 */
    @Transient
    protected String ctgrNm;

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
    private String mdfable = Constant.MDFABLE_REGSTR;
}
