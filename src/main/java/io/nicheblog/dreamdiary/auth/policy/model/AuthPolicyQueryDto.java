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

    /** 인증 정책 고유 ID */
    private Integer id;

    /** 로그인 시도 제한 횟수 */
    private Integer loginAttemptLimit;

    /** 로그인 시도 누적 시간 창(분) */
    private Integer loginAttemptWindowMinutes;

    /** 계정 잠금 지속 시간(분) */
    private Integer accountLockDurationMinutes;

    /** 비밀번호 변경 주기(일) */
    private Integer passwordChangeCycleDays;

    /** 미로그인 시 잠금 일수 */
    private Integer inactiveLockDays;

    /** 비밀번호 재설정 토큰 만료 시간(분) */
    private Integer passwordResetTokenExpiryMinutes;

    /** 비밀번호 초기화 값 */
    private String pwForReset;
}
