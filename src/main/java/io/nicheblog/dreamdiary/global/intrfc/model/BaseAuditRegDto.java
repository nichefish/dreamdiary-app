package io.nicheblog.dreamdiary.global.intrfc.model;

import io.nicheblog.dreamdiary.auth.security.model.AuditorDto;
import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
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
    protected String regstrId;

    /** 등록자 이름 */
    protected String regstrNm;

    /** 등록일시 */
    protected String regDt;

    /** 등록자 정보 */
    protected AuditorDto regstrInfo;

    /** 등록자 여부 */
    @Builder.Default
    protected Boolean isRegstr = false;

    /** 처리성공여부 = 서비스 레벨에서 결과값 반환시 사용 */
    @Builder.Default
    protected Boolean isSuccess = false;

    /* ----- */

    /**
     * 마스킹 처리한 사용자ID 반환
     *
     * @return {@link String} -- 마스킹 처리된 사용자 ID
     * @throws Exception 마스킹 처리 중 발생할 수 있는 예외
     */
    public String getMaskedRegstrId() throws Exception {
        return CryptoUtils.Mask.nameMasking(this.getRegstrId());
    }

    public Boolean getIsRegstr() {
        if (StringUtils.isEmpty(this.regstrId)) return false;
        return this.regstrId.equals(AuthUtils.getLgnUserId());
    }
}
