package io.nicheblog.dreamdiary.domain.user.info.model;

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

    /** 사용자 ID */
    @NotEmpty
    private String userId;

    /** 현재 패스워드 */
    @NotEmpty
    private String currPw;

    /** 변경할 패스워드 */
    @NotEmpty
    @Size(min = 9, max = 15, message = "비밀번호는 9자 이상 15자 이하로 입력해야 합니다.")
    @Pattern(regexp = Regex.PW_REGEX, message = "비밀번호가 형식에 맞지 않습니다.")
    private String newPw;
}
