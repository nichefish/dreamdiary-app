package io.nicheblog.dreamdiary.feature.user.my.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDate;

/**
 * 내 정보 수정 요청.
 * <p>
 * 로그인 사용자가 직접 관리하는 개인 프로필 필드만 허용한다.
 * 계정 식별자·이메일·권한·허용 IP·재직 정보는 별도 보안·관리 계약이므로 포함하지 않는다.
 * </p>
 *
 * @author nichefish
 */
@Getter
@Setter
@NoArgsConstructor
public class UserMyUpdateRequest {

    /** 표시 이름. */
    @NotBlank
    @Size(max = 20)
    private String nickname;

    /** 개인 연락처. 빈 문자열은 저장 시 {@code null}로 정규화한다. */
    @Size(max = 20)
    private String phoneNumber;

    /** 생년월일. */
    @PastOrPresent
    private LocalDate brthdy;

    /** 음력 여부(Y/N). */
    @NotBlank
    @Pattern(regexp = "^[YN]$")
    private String lunarYn = "N";

    /** 자기소개. 빈 문자열은 저장 시 {@code null}로 정규화한다. */
    @Size(max = 4000)
    private String proflCn;
}
