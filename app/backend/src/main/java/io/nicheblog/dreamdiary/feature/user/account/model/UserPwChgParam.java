package io.nicheblog.dreamdiary.feature.user.account.model;

import io.nicheblog.dreamdiary.global.validator.Regex;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * UserPwChgParam
 * <pre>
 *  패스워드 변경 파라미터.
 * </pre>
 *
 * @author nichefish
 */
@Getter
@Setter
@NoArgsConstructor
public class UserPwChgParam {

    /** 사용자 계정명 */
    @NotEmpty
    private String username;

    /** 현재 패스워드 */
    @NotEmpty
    private String currPw;

    /** 변경할 패스워드 */
    @NotEmpty
    @Size(min = 9, max = 15, message = "{msg.user.pw.size}")
    @Pattern(regexp = Regex.PW_REGEX, message = "{msg.user.pw.pattern}")
    private String newPw;

}
