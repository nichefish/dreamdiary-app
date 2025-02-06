package io.nicheblog.dreamdiary.global._common.log.sys.entity;

import io.nicheblog.dreamdiary.auth.security.entity.AuditorInfo;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global._common.cd.entity.DtlCdEntity;
import io.nicheblog.dreamdiary.global.intrfc.entity.BaseCrudEntity;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * LogSysEntity
 * <pre>
 *  시스템 로그 Entity.
 * </pre>
 *
 * @author nichefish
 */
@Entity
@Table(name = "log_sys")
@DynamicInsert      // null인 값은 (null로 insert하는 대신) insert에서 제외
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class LogSysEntity
        extends BaseCrudEntity
        implements Serializable {

    /** 로그 고유 번호 (PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_sys_no")
    @Comment("로그 고유 번호 (PK)")
    private Integer logSysNo;

    /** 작업자 ID */
    @Builder.Default
    @Column(name = "user_id", length = 20)
    @Comment("작업자 ID")
    private String userId = Constant.SYSTEM_ACNT;

    /** 작업자 정보 */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", insertable = false, updatable = false)
    @Fetch(value = FetchMode.JOIN)
    @NotFound(action = NotFoundAction.IGNORE)
    @Comment("작업자 정보")
    private AuditorInfo userInfo;

    /** 작업일시 */
    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(pattern = DateUtils.PTN_DATETIME)
    @Column(name = "log_dt", updatable = false)
    @Comment("작업일시")
    private Date logDt;

    /** 작업 구분 코드 (ex. 게시판, 공지사항, ...) (기능/모듈 단위) */
    @Column(name = "actvty_ctgr_cd", length = 400)
    @Comment("작업 구분 코드")
    private String actvtyCtgrCd;

    /** 작업 구분 코드 정보 (복합키 조인) */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumnsOrFormulas({
            @JoinColumnOrFormula(formula = @JoinFormula(value = "\'" + Constant.ACTVTY_CTGR_CD + "\'", referencedColumnName = "cl_cd")),
            @JoinColumnOrFormula(column = @JoinColumn(name = "actvty_ctgr_cd", referencedColumnName = "dtl_cd", insertable = false, updatable = false))
    })
    @Fetch(value = FetchMode.JOIN)
    @NotFound(action = NotFoundAction.IGNORE)
    @Comment("작업 구분 코드 정보")
    private DtlCdEntity actvtyCtgrInfo;

    /** 작업 내용 */
    @Column(name = "cn", length = 400)
    @Comment("작업 내용")
    private String cn;

    /** 작업 결과 */
    @Column(name = "rslt")
    @Comment("작업 결과")
    private Boolean rslt;

    /** 작업 결과 메세지 */
    @Column(name = "rslt_msg")
    @Comment("작업 결과 메세지")
    private String rsltMsg;

    /** 익셉션 이름 */
    @Column(name = "exception_nm")
    @Comment("익셉션 이름")
    private String exceptionNm;

    /** 익셉션 메세지 */
    @Column(name = "exception_msg")
    @Comment("익셉션 메세지")
    private String exceptionMsg;
}
