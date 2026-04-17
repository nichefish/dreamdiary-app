package io.nicheblog.dreamdiary.feature.admin.auth.policy.model;

import io.nicheblog.dreamdiary.auth.intrfc.model.BaseAuditDto;
import io.nicheblog.dreamdiary.global.validator.Regex;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.Max;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

/**
 * AuthPolicyDto
 * <pre>
 *  인증 정책 정보 Dto.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class AuthPolicyDto
        extends BaseAuditDto {

    /** 인증 정책 고유 ID */
    private Integer id;

    /** 로그인 시도 제한 횟수 */
    @Positive
    @Max(value = 999)
    private Integer loginAttemptLimit;

    /** 로그인 시도 누적 시간 창(분) */
    @Positive
    @Max(value = 999)
    private Integer loginAttemptWindowMinutes;

    /** 계정 잠금 지속 시간(분) */
    @Positive
    @Max(value = 9999)
    private Integer accountLockDurationMinutes;

    /** 비밀번호 변경 주기(일) */
    @Positive
    @Max(value = 365)
    private Integer passwordChangeCycleDays;

    /** 미로그인 시 잠금 일수 */
    @Positive
    @Max(value = 365)
    private Integer inactiveLockDays;

    /** 비밀번호 재설정 토큰 만료 시간(분) */
    @Positive
    @Max(value = 10080)
    private Integer passwordResetTokenExpiryMinutes;

    /** 비밀번호 초기화 값 */
    @Size(min = 9, max = 20)
    @Pattern(regexp = Regex.PW_REGEX, message = "비밀번호가 형식에 맞지 않습니다.")
    private String pwForReset;
}
