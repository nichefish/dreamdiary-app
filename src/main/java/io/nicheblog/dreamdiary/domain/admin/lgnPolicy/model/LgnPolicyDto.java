package io.nicheblog.dreamdiary.domain.admin.lgnPolicy.model;

import io.nicheblog.dreamdiary.global.intrfc.model.BaseAuditDto;
import io.nicheblog.dreamdiary.global.validator.Regex;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.Max;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

/**
 * LgnPolicyDto
 * <pre>
 *  로그인 정책 정보 Dto.
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
public class LgnPolicyDto
        extends BaseAuditDto {

    /** 로그인 정책 고유 번호 (PK) */
    private Integer lgnPolicyNo;

    /** 로그인 최대 시도 횟수 */
    @Positive
    @Max(value = 999)
    private Integer lgnTryLmt;

    /** 비밀번호 변경 일자 */
    @Positive
    @Max(value = 365)
    private Integer pwChgDy;

    /** 미접속시 계정 잠금 일자 */
    @Positive
    @Max(value = 365)
    private Integer lgnLockDy;

    /** 비밀번호 초기화 값 */
    @Size(min = 9, max = 20)
    @Pattern(regexp = Regex.PW_REGEX, message = "비밀번호가 형식에 맞지 않습니다.")
    private String pwForReset;
}
