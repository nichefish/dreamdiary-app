package io.nicheblog.dreamdiary.auth.intrfc.model;

import io.nicheblog.dreamdiary.auth.security.model.AuditorDto;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * BaseAuditDto
 * <pre>
 *  (공통/상속) Audit 정보 Dto. (기존 등록자 + 수정자 정보 추가)
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
public class BaseAuditDto
        extends BaseAuditRegDto {

    /** 수정자 ID */
    protected String updatedBy;

    /** 수정자 이름 */
    protected String updatedByNm;

    /** 수정일시 */
    protected String updatedAt;

    /** 수정자 정보 */
    protected AuditorDto updatedByInfo;

    /** 수정자 여부 */
    @Builder.Default
    protected Boolean isUpdatedBy = false;
}
