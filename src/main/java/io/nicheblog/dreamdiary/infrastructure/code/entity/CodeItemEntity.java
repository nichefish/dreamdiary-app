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
@IdClass(CodeItemKey.class)      // 분류 코드+상세 코드 복합키 적용
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

    /** 상세 코드 */
    @Id
    @Column(name = "dtl_cd", length=50)
    private String dtlCd;

    /** 공통코드 */
    @Id
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

    /**
     * Key 반환
     * @return {@link CodeItemKey}
     */
    public CodeItemKey getKey() {
        return new CodeItemKey(this.getClCd(), this.getDtlCd());
    }
    
    /**
     * 생성자.
     *
     * @param clCd 분류 코드
     * @param dtlCd 상세 코드
     */
    public CodeItemEntity(final String clCd, final String dtlCd) {
        this.clCd = clCd;
        this.dtlCd = dtlCd;
    }

    /**
     * 생성자.
     *
     * @param key 분류 코드와 상세 코드로 이루어진 복합키.
     */
    public CodeItemEntity(final CodeItemKey key) {
        this(key.getClCd(), key.getDtlCd());
    }

}
