package io.nicheblog.dreamdiary.auth.policy.model;

import io.nicheblog.dreamdiary.auth.intrfc.model.BaseAuditDto;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * AuthPolicyQueryDto
 * <pre>
 *  인증 정책 조회 Dto.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class AuthPolicyQueryDto
        extends BaseAuditDto {

    /** 인증 정책 고유 번호 (PK) */
    private Integer authPolicyNo;

    /** 로그인 최대 시도 횟수 */
    private Integer lgnTryLmt;

    /** 비밀번호 변경 일자 */
    private Integer pwChgDy;

    /** 미접속시 계정 잠금 일자 */
    private Integer lgnLockDy;

    /** 비밀번호 초기화 값 */
    private String pwForReset;
}
