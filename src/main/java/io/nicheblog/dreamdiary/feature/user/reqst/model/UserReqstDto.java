package io.nicheblog.dreamdiary.feature.user.reqst.model;

import io.nicheblog.dreamdiary.feature.user.info.model.UserDto;
import io.nicheblog.dreamdiary.global.validator.Regex;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * UserReqstDto
 * <pre>
 *  사용자(계정) 신청 Dto.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserReqstDto
        extends UserDto {

    /** 아이디 */
    @NotEmpty
    private String userId;
    /** 비밀번호 */
    @NotEmpty
    @Size(min = 9, max = 15, message = "비밀번호는 9자 이상 15자 이하로 입력해야 합니다.")
    @Pattern(regexp = Regex.PW_REGEX, message = "비밀번호가 형식에 맞지 않습니다.")
    private String password;
    /** 표시이름 */
    @NotEmpty
    private String nickNm;

    /** 이메일 */
    private String email;
    /** E-mail ID */
    @NotEmpty
    private String emailId;
    /** E-mail 뒷부분 */
    @NotEmpty
    private String emailDomain;
    /** 연락처 */
    private String cttpc;

    /** 계정 설명 (관리자용) */
    private String cn;

    /* ----- */

    /**
     * Getter :: 이메일 반환 (override)
     */
    public String getEmail() {
        if (StringUtils.isNotEmpty(this.email)) return this.email;
        if (StringUtils.isEmpty(this.emailId) || StringUtils.isEmpty(this.emailDomain)) return null;
        return this.emailId + "@" + this.emailDomain;
    }
}
