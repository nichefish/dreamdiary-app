package io.nicheblog.dreamdiary.domain.user.reqst.model;

import io.nicheblog.dreamdiary.domain.user.info.model.UserAcsIpDto;
import io.nicheblog.dreamdiary.domain.user.info.model.UserAuthRoleDto;
import io.nicheblog.dreamdiary.domain.user.info.model.UserDto;
import io.nicheblog.dreamdiary.global.validator.Regex;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.stream.Collectors;

/**
 * UserDto
 * <pre>
 *  사용자(계정) 정보 Dto.
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

    /** 비밀번호 */
    @NotEmpty
    @Size(min = 9, max = 15, message = "비밀번호는 9자 이상 15자 이하로 입력해야 합니다.")
    @Pattern(regexp = Regex.PW_REGEX, message = "비밀번호가 형식에 맞지 않습니다.")
    private String password;

    /** 사용자 권한 정보(문자열) (multiselect parameter) */
    private String authListStr;

    /** 접속IP 사용 여부 체크 */
    @Builder.Default
    private Boolean useAcsIp = false;

    /** 접속 IP 사용 여부 (Y/N) */
    @Builder.Default
    @Size(min = 1, max = 1)
    @Pattern(regexp = "^[YN]$")
    private String useAcsIpYn = "N";

    /** 접속 IP 정보 */
    private String acsIpListStr;

    /** 접속 IP 정보 */
    private List<UserAcsIpDto> acsIpList;

    /** 계정 설명 (관리자용) */
    private String cn;

    /* ----- */

    /**
     * Getter :: 이메일 반환 (override)
     */
    public String getEmail() {
        if (!StringUtils.isEmpty(this.email)) return this.email;
        if (!StringUtils.isEmpty(this.emailId) || !StringUtils.isEmpty(this.emailDomain)) return null;
        return this.emailId + "@" + this.emailDomain;
    }

    /**
     * Getter :: 권한 목록 문자열
     */
    public String getAuthListStr() {
        if (this.authList != null) return this.authList.stream()
                .map(UserAuthRoleDto::getAuthCd)
                .collect(Collectors.joining(","));
        return this.authListStr;
    }
}
