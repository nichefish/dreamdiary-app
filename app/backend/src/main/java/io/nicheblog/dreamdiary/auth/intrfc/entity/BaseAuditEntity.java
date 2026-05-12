package io.nicheblog.dreamdiary.auth.intrfc.entity;

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
import org.hibernate.annotations.UpdateTimestamp;
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
 * @see AuditConfig
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
    @Column(name = "updated_by", length = 20, insertable = false)
    protected String updatedBy;

    /** 수정일시 */
    @LastModifiedDate
    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(pattern = DateUtils.PTN_DATETIME)
    @Column(name = "updated_at", insertable = false)
    protected Date updatedAt;

    /** 수정자 정보 :: join 제거하고 캐시 처리 */
    @Transient
    protected AuditorInfo updatedByInfo;

    /* ----- */

    /**
     * 수정자 정보 반환 :: 캐시 처리
     * 
     * @return {@link AuditorInfo}
     */
    public AuditorInfo getUpdatedByInfo() {
        if (StringUtils.isEmpty(this.updatedBy)) return null;
        if (this.updatedByInfo == null) {
            this.updatedByInfo = AuditorUtils.getAuditorInfo(this.updatedBy);
        }
        return this.updatedByInfo;
    }

    /**
     * (현재 로그인 중인 사용자) 수정자 여부 체크
     * 
     * @return {@link Boolean} 수정자 여부
     */
    public Boolean isUpdatedBy() {
        if (StringUtils.isEmpty(this.updatedBy)) return false;
        return this.updatedBy.equals(AuthUtils.getLoginUsername());
    }
}

