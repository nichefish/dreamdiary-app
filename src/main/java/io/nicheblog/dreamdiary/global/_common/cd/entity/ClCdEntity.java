package io.nicheblog.dreamdiary.global._common.cd.entity;

import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global._common._clsf.state.entity.embed.StateEmbed;
import io.nicheblog.dreamdiary.global._common._clsf.state.entity.embed.StateEmbedModule;
import io.nicheblog.dreamdiary.global.intrfc.entity.BaseAuditEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.*;
import org.springframework.util.CollectionUtils;

import javax.persistence.Entity;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ClCdEntity
 * <pre>
 *  분류 코드(clCd) Entity.
 *  ※분류 코드(cl_cd) = 상위 분류 코드. 상세 코드(dtl_cd)를 1:N 묶음으로 관리한다.
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(name = "cmm_cl_cd")
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@ToString
@Where(clause = "del_yn='N'")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class ClCdEntity
        extends BaseAuditEntity
        implements StateEmbedModule {

    @PostLoad
    private void onLoad() {
        this.dtlCdCnt = (CollectionUtils.isEmpty(this.dtlCdList)) ? 0 : this.dtlCdList.size();
        if (this.clCtgrCdInfo != null) this.clCtgrNm = this.clCtgrCdInfo.getDtlCdNm();
    }

    /** 분류 코드 */
    @Id
    @Column(name = "cl_cd", length=50)
    private String clCd;

    /** 분류 코드 이름 */
    @Column(name = "cl_cd_nm")
    private String clCdNm;

    /** 분류 코드 분류 코드 */
    @Column(name = "cl_ctgr_cd", length=50)
    private String clCtgrCd;

    /** 분류 코드 코드 정보 (복합키 조인) */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumnsOrFormulas({
            @JoinColumnOrFormula(formula = @JoinFormula(value = "\'" + Constant.CL_CTGR_CD + "\'", referencedColumnName = "cl_cd")),
            @JoinColumnOrFormula(column = @JoinColumn(name = "cl_ctgr_cd", referencedColumnName = "dtl_cd", insertable = false, updatable = false))
    })
    @Fetch(value = FetchMode.JOIN)
    @NotFound(action = NotFoundAction.IGNORE)
    @Comment("분류 코드 코드 정보")
    private DtlCdEntity clCtgrCdInfo;

    /** 분류 코드 분류 코드명 */
    @Transient
    private String clCtgrNm;

    /** 분류 코드 설명 */
    @Column(name = "dc", length=2000)
    private String dc;

    /** 분류 코드 정보 */
    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "cl_cd", referencedColumnName = "cl_cd", insertable = false, updatable = false)
    @OrderBy("state.sortOrdr ASC")
    @NotFound(action = NotFoundAction.IGNORE)
    @ToString.Exclude
    private List<DtlCdEntity> dtlCdList;

    /** 상세 코드 개수 */
    @Transient
    @Builder.Default
    private Integer dtlCdCnt = 0;

    /* ----- */

    /**
     * 서브엔티티 List 처리를 위한 Setter
     * 한 번 Entity가 생성된 이후부터는 새 List를 할당하면 안 되고 계속 JPA 이력이 추적되어야 한다.
     *
     * @param dtlCdList - 설정할 객체 리스트
     */
    public void setDtlCdList(final List<DtlCdEntity> dtlCdList) {
        if (CollectionUtils.isEmpty(dtlCdList)) return;
        if (this.dtlCdList == null) {
            this.dtlCdList = new ArrayList<>(dtlCdList);
        } else {
            this.dtlCdList.clear();
            this.dtlCdList.addAll(dtlCdList);
        }
    }

    /* ----- */

    /** 위임 :: 상태 관리 모듈 */
    @Embedded
    public StateEmbed state;
}
