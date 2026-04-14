package io.nicheblog.dreamdiary.auth.intrfc.model;

import io.nicheblog.dreamdiary.auth.security.model.AuditorDto;
import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.global.intrfc.model.BaseCrudDto;
import io.nicheblog.dreamdiary.global.util.crypto.CryptoUtils;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.StringUtils;

/**
 * BaseAuditRegDto
 * <pre>
 *  (공통/상속) Audit 정보 Dto (등록자 정보 추가)
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
public class BaseAuditRegDto
        extends BaseCrudDto {

    /** 등록자 ID */
    protected String createdBy;

    /** 등록자 이름 */
    protected String createdByNm;

    /** 등록일시 */
    protected String createdAt;

    /** 등록자 정보 */
    protected AuditorDto createdByInfo;

    /** 등록자 여부 */
    @Builder.Default
    protected Boolean isCreatedBy = false;

    /** 처리성공여부 = 서비스 레벨에서 결과값 반환시 사용 */
    @Builder.Default
    protected Boolean isSuccess = false;

    /* ----- */

    /**
     * 마스킹 처리한 사용자ID 반환
     *
     * @return {@link String} -- 마스킹 처리된 사용자 ID
     */
    public String getMaskedCreatedBy() throws Exception {
        return CryptoUtils.Mask.nameMasking(this.getCreatedBy());
    }

    /**
     * 등록자 여부
     *
     * @return 등록자 여부
     */
    public Boolean getIsCreatedBy() {
        if (StringUtils.isEmpty(this.createdBy)) return false;
        return this.createdBy.equals(AuthUtils.getLgnUsername());
    }

    /**
     * 등록자 여부
     *
     * @param username 사용자 계정명
     * @return 등록자 여부
     */
    public Boolean getIsCreatedBy(final String username) {
        if (StringUtils.isEmpty(this.createdBy)) return false;
        return this.createdBy.equals(username);
    }
}
