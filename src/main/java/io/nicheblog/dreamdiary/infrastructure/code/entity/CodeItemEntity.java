package io.nicheblog.dreamdiary.infrastructure.code.entity;

import io.nicheblog.dreamdiary.auth.intrfc.entity.BaseAuditEntity;
import io.nicheblog.dreamdiary.global.intrfc.entity.Sortable;
import io.nicheblog.dreamdiary.global.intrfc.entity.Usable;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.annotations.Where;

import javax.persistence.*;

/**
 * CodeItemEntity
 * <pre>
 *  상세 코드(dtlCd) Entity
 *  ※상세 코드(dtl_cd) = 분류 코드 하위의 상세 코드. 분류 코드(cl_cd)에 N:1로 귀속된다.
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(name = "code_item")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@ToString
@Where(clause = "deleted_at IS NULL")
public class CodeItemEntity
        extends BaseAuditEntity
        implements Usable, Sortable {

    /** 내부 PK (id) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    /** 상세 코드 */
    @Column(name = "dtl_cd", length=50)
    private String dtlCd;

    /** 공통코드 */
    @Column(name = "cl_cd", length=50)
    private String clCd;

    /** 분류 코드 정보 */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cl_cd", referencedColumnName = "cl_cd", insertable = false, updatable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    private CodeGroupEntity clCdInfo;

    /** 상세 코드이름 */
    @Column(name = "dtl_cd_nm", length=50)
    private String dtlCdNm;

    /** 상세 코드설명 */
    @Column(name = "description", length=2000)
    private String description;

    /** 정렬 순서 */
    @Column(name = "sort_order", columnDefinition = "INT DEFAULT 0")
    private Integer sortOrder;

    /** 사용 여부 (Y/N) */
    @Builder.Default
    @Column(name = "use_yn", length = 1, columnDefinition = "CHAR DEFAULT 'Y'")
    private String useYn = "N";

    /** 시스템 보호 여부 (Y/N) */
    @Builder.Default
    @Column(name = "protected_yn")
    @Comment("시스템 보호 여부 (Y/N)")
    private String protectedYn = "N";

    /* ---- */

}
