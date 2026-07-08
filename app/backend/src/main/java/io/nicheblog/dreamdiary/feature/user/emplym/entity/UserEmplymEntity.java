package io.nicheblog.dreamdiary.feature.user.emplym.entity;

import io.nicheblog.dreamdiary.feature.user.account.entity.UserEntity;
import io.nicheblog.dreamdiary.global.intrfc.entity.BaseCrudEntity;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import io.nicheblog.dreamdiary.infrastructure.code.Code;
import io.nicheblog.dreamdiary.infrastructure.code.entity.CodeItemEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.*;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;

import java.time.LocalDateTime;
import java.time.LocalDate;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * UserEmplymEntity
 * <pre>
 *  사용자 정보 Entity :: 계정(User) 정보에 귀속됨
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(name = "user_emplym")
@DynamicInsert      // null인 값은 (null로 insert하는 대신) insert에서 제외
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"user"}, callSuper = true)
@Where(clause = "deleted_at IS NULL")
@SQLDelete(sql = "UPDATE user_emplym SET DELETED_AT = 'Y' WHERE id = ?")
public class UserEmplymEntity
        extends BaseCrudEntity {

    @PostLoad
    private void onLoad() {
        // 코드 이름 세팅
        if (this.cmpyCdInfo != null) this.cmpyNm = this.cmpyCdInfo.getCodeName();
        if (this.teamCdInfo != null) this.teamNm = this.teamCdInfo.getCodeName();
        if (this.emplymCdInfo != null) this.emplymNm = this.emplymCdInfo.getCodeName();
        if (this.rankCdInfo != null) this.rankNm = this.rankCdInfo.getCodeName();
    }

    /** 사용자 인사정보 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("사용자 인사정보 ID")
    private Integer id;

    /** 사용자 정보 (FK) */
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @Fetch(FetchMode.JOIN)
    @NotFound(action = NotFoundAction.IGNORE)
    @Comment("계정 정보")
    private UserEntity user;

    /** 직원명 */
    @Column(name = "user_nm", length = 20)
    @Comment("직원명")
    private String userNm;

    /** 업무 Email 주소 */
    @Column(name = "emplym_email", length = 100)
    @Comment("업무 Email 주소")
    private String emplymEmail;

    /** 업무 연락처 */
    @Column(name = "emplym_phone_number", length = 20)
    @Comment("업무 연락처")
    private String emplymPhoneNumber;

    /** 소속(회사) 코드 */
    @Column(name = "cmpy_cd", length = 20)
    @Comment("소속(회사)코드")
    private String cmpyCd;

    /** 소속(회사) 코드 정보 (복합키 조인) */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumnsOrFormulas({
            @JoinColumnOrFormula(formula=@JoinFormula(value="'"+ Code.CMPY_CD+"'", referencedColumnName="group_code")),
            @JoinColumnOrFormula(column=@JoinColumn(name="cmpy_cd", referencedColumnName="code", insertable=false, updatable=false))
    })
    @Fetch(value= FetchMode.JOIN)
    @NotFound(action=NotFoundAction.IGNORE)
    private CodeItemEntity cmpyCdInfo;

    @Transient
    private String cmpyNm;

    /** 소속(팀) 코드 */
    @Column(name = "team_cd", length = 20)
    @Comment("소속(팀)코드")
    private String teamCd;

    /** 소속(팀) 코드 정보 (복합키 조인) */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumnsOrFormulas({
            @JoinColumnOrFormula(formula=@JoinFormula(value="'"+Code.TEAM_CD+"'", referencedColumnName="group_code")),
            @JoinColumnOrFormula(column=@JoinColumn(name="team_cd", referencedColumnName="code", insertable=false, updatable=false))
    })
    @Fetch(value= FetchMode.JOIN)
    @NotFound(action=NotFoundAction.IGNORE)
    private CodeItemEntity teamCdInfo;

    @Transient
    private String teamNm;

    /** 재직구분 코드 */
    @Column(name = "emplym_cd", length = 20)
    @Comment("소속(팀)코드")
    private String emplymCd;

    /** 재직구분 코드 정보 (복합키 조인) */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumnsOrFormulas({
            @JoinColumnOrFormula(formula=@JoinFormula(value="'"+Code.RANK_CD+"'", referencedColumnName="group_code")),
            @JoinColumnOrFormula(column=@JoinColumn(name="emplym_cd", referencedColumnName="code", insertable=false, updatable=false))
    })
    @Fetch(value= FetchMode.JOIN)
    @NotFound(action=NotFoundAction.IGNORE)
    private CodeItemEntity emplymCdInfo;

    @Transient
    private String emplymNm;

    /** 직급코드 */
    @Column(name = "rank_cd", length = 20)
    @Comment("직급코드")
    private String rankCd;

    /** 직급 코드 정보 (복합키 조인) */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumnsOrFormulas({
        @JoinColumnOrFormula(formula=@JoinFormula(value="'"+ Code.RANK_CD+"'", referencedColumnName="group_code")),
        @JoinColumnOrFormula(column=@JoinColumn(name="rank_cd", referencedColumnName="code", insertable=false, updatable=false))
    })
    @Fetch(value= FetchMode.JOIN)
    @NotFound(action=NotFoundAction.IGNORE)
    private CodeItemEntity rankCdInfo;

    @Transient
    private String rankNm;

    /** 수습 여부 (Y/N) */
    @Builder.Default
    @Column(name = "apntc_yn", length = 1, columnDefinition = "CHAR(1) DEFAULT 'N'")
    @Comment("수습여부")
    private String apntcYn = "N";

    /** 입사일 */
    @DateTimeFormat(pattern = DateUtils.PTN_DATE)
    @Column(name = "ecny_dt")
    @Comment("입사일")
    private LocalDate ecnyDt;

    /** 퇴사 여부 (Y/N) */
    @Builder.Default
    @Column(name = "retire_yn", length = 1, columnDefinition = "CHAR(1) DEFAULT 'N'")
    @Comment("퇴사여부")
    private String retireYn = "N";

    /** 퇴사일 */
    @DateTimeFormat(pattern = DateUtils.PTN_DATE)
    @Column(name = "retire_dt")
    @Comment("퇴사일")
    private LocalDateTime retireDt;

    /** (급여계좌) 은행 */
    @Column(name = "acnt_bank", length = 20)
    private String acntBank;

    /** (급여계좌) 계좌번호 */
    @Column(name = "acnt_no", length = 20)
    private String acntNo;

    /** 인사정보 설명 (관리자용) */
    @Column(name = "emplym_cn", length = 4000)
    @Comment("인사정보 설명")
    private String emplymCn;
}
