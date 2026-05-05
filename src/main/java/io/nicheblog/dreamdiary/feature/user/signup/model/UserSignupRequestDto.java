package io.nicheblog.dreamdiary.feature.user.signup.model;

import io.nicheblog.dreamdiary.feature.user.account.model.UserDto;
import io.nicheblog.dreamdiary.global.validator.Regex;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * UserSignupRequestDto
 * <pre>
 *  사용자(계정) 신청 Dto — `user_signup_request` 저장 레코드에 대응하는 페이로드.
 * </pre>
 *
 * 명명 규약: 영속 신청 레코드·그에 바인딩되는 전송 객체는 {@code UserSignupRequest*}; 화면/API/유스케이스는 {@code UserSignup*} 로 구분한다.
 *
 * 변경 전: {@link UserDto} 와 동일한 username·nickname·emailId 등을 서브클래스에서 다시 선언해 필드가 이중으로 존재했고,
 * WebMvc {@code @ModelAttribute} 바인딩에서 일부 문자열 프로퍼티가 요청 파라미터에 있어도 검증 대상 인스턴스에는 null 로 남는 현상이 있었다.
 * 변경 후: 공통 필드는 {@link UserDto} 단일 선언에만 두고, 신청 전용 비밀번호 제약만 서브클래스에 둔다.
 *
 * @author nichefish
 */
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserSignupRequestDto
        extends UserDto {

    /** 비밀번호 (신청 시에만 강한 형식·길이 제약) */
    @NotEmpty
    @Size(min = 9, max = 15, message = "{msg.user.pw.size}")
    @Pattern(regexp = Regex.PW_REGEX, message = "{msg.user.pw.pattern}")
    private String password;
}
