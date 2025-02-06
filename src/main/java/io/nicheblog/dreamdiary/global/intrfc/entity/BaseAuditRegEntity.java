package io.nicheblog.dreamdiary.global.intrfc.entity;

import io.nicheblog.dreamdiary.auth.security.config.AuditConfig;
import io.nicheblog.dreamdiary.auth.security.entity.AuditorInfo;
import io.nicheblog.dreamdiary.auth.security.util.AuditorUtils;
import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.StringUtils;
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
 * @see AuditConfig
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

    /** 등록자 정보 :: join 제거하고 캐시 처리 */
    @Transient
    protected AuditorInfo regstrInfo;

    /* ----- */

    /**
     * (현재 로그인 중인 사용자) 수정자 여부 체크
     */
    public Boolean isRegstr() {
        if (StringUtils.isEmpty(this.regstrId)) return false;
        return this.regstrId.equals(AuthUtils.getLgnUserId());
    }

    /**
     * 등록자 정보 반환 :: 캐시 처리
     * @return AuditorInfo
     */
    public AuditorInfo getRegstrInfo() {
        if (StringUtils.isEmpty(this.regstrId)) return null;
        if (this.regstrInfo == null) {
            this.regstrInfo = AuditorUtils.getAuditorInfo(this.regstrId);
        }
        return this.regstrInfo;
    }
}

