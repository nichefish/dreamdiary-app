package io.nicheblog.dreamdiary.global.intrfc.entity;

import io.nicheblog.dreamdiary.auth.entity.AuditorInfo;
import io.nicheblog.dreamdiary.auth.util.AuthUtils;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.util.Date;

/**
 * BaseAuditRegEntity
 * <pre>
 *  (공통/상속) Audit 정보 Entity. (수정자 없이 등록자만)
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
@EntityListeners({AuditingEntityListener.class})
public class BaseAuditRegEntity
        extends BaseCrudEntity {

    /** 등록자 ID */
    @CreatedBy
    @Column(name = "regstr_id", length = 20, updatable = false)
    protected String regstrId;

    /** 등록일시 */
    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(pattern = DateUtils.PTN_DATETIME)
    @Column(name = "reg_dt", updatable = false)
    protected Date regDt;

    /** 등록자 정보 */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "regstr_id", referencedColumnName = "user_id", insertable = false, updatable = false)
    @Fetch(value = FetchMode.JOIN)
    @NotFound(action = NotFoundAction.IGNORE)
    protected AuditorInfo regstrInfo;

    /* ----- */

    /**
     * (현재 로그인 중인 사용자) 수정자 여부 체크
     */
    public Boolean isRegstr() {
        if (StringUtils.isEmpty(this.regstrId)) return false;
        return this.regstrId.equals(AuthUtils.getLgnUserId());
    }
}

