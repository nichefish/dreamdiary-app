package io.nicheblog.dreamdiary.global.intrfc.entity;

import io.nicheblog.dreamdiary.global._common.auth.entity.AuditorInfo;
import io.nicheblog.dreamdiary.global._common.auth.util.AuthUtils;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.*;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.util.Date;

/**
 * BaseAuditEntity
 * <pre>
 *  (공통/상속) Audit 정보 Entity. (기존 등록자 + 수정자 정보 추가)
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
@EntityListeners(AuditingEntityListener.class)
public class BaseAuditEntity
        extends BaseAuditRegEntity {

    /** 수정자 ID */
    @LastModifiedBy
    @Column(name = "mdfusr_id", length = 20, insertable = false)
    protected String mdfusrId;

    /** 수정일시 */
    @LastModifiedDate
    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(pattern = DateUtils.PTN_DATETIME)
    @Column(name = "mdf_dt", insertable = false)
    protected Date mdfDt;

    /** 수정자 정보 */
    @ManyToOne
    @JoinColumn(name = "mdfusr_id", referencedColumnName = "user_id", insertable = false, updatable = false)
    @Fetch(value = FetchMode.JOIN)
    @NotFound(action = NotFoundAction.IGNORE)
    protected AuditorInfo mdfusrInfo;

    /* ----- */

    /**
     * (현재 로그인 중인 사용자) 수정자 여부 체크
     */
    public Boolean isMdfusr() {
        if (StringUtils.isEmpty(this.mdfusrId)) return false;
        return this.mdfusrId.equals(AuthUtils.getLgnUserId());
    }
}

