package io.nicheblog.dreamdiary.infrastructure.code.entity;

import io.nicheblog.dreamdiary.auth.intrfc.entity.BaseAuditEntity;
import io.nicheblog.dreamdiary.global.intrfc.entity.Usable;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

/**
 * CodeGroupEntity
 * <pre>
 *  분류 코드(clCd) Entity.
 *  ※분류 코드(cl_cd) = 상위 분류 코드. 상세 코드(dtl_cd)를 1:N 묶음으로 관리한다.
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(name = "code_group")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@ToString
@Where(clause = "deleted_at IS NULL")
public class CodeGroupEntity
        extends BaseAuditEntity
        implements Usable {

    @PostLoad
    private void onLoad() {
        this.dtlCdCnt = (CollectionUtils.isEmpty(this.dtlCdList)) ? 0 : this.dtlCdList.size();
    }

    /** 내부 PK (id) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    /** 분류 코드 (비즈니스 키, UNIQUE) */
    @Column(name = "cl_cd", length=50, unique = true)
    private String clCd;

    /** 분류 코드 이름 */
    @Column(name = "cl_cd_nm")
    private String clCdNm;

    /** 글분류 코드 :: join을 제거하고 메모리 캐시 처리 */
    @Column(name = "cl_ctgr_cd", length=50)
    private String clCtgrCd;

    /** 글분류 코드 이름 :: join을 제거하고 메모리 캐시 처리 */
    @Transient
    private String clCtgrNm;

    /** 분류 코드 설명 */
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

    /** 분류 코드 정보 */
    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "cl_cd", referencedColumnName = "cl_cd", insertable = false, updatable = false)
    @BatchSize(size = 10)
    @OrderBy("sortOrder ASC")
    @NotFound(action = NotFoundAction.IGNORE)
    @ToString.Exclude
    private List<CodeItemEntity> dtlCdList;

    /** 상세 코드 개수 */
    @Transient
    @Builder.Default
    private Integer dtlCdCnt = 0;

    /* ----- */

    /**
     * 서브엔티티 List 처리를 위한 Setter
     * 한 번 Entity가 생성된 이후부터는 새 List를 할당하면 안 되고 계속 JPA 이력이 추적되어야 한다.
     *
     * @param dtlCdList 설정할 객체 리스트
     */
    public void setDtlCdList(final List<CodeItemEntity> dtlCdList) {
        if (CollectionUtils.isEmpty(dtlCdList)) return;
        if (this.dtlCdList == null) {
            this.dtlCdList = new ArrayList<>(dtlCdList);
        } else {
            this.dtlCdList.clear();
            this.dtlCdList.addAll(dtlCdList);
        }
    }
}
