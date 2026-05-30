package io.nicheblog.dreamdiary.feature.user.signup.controller;

import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.infrastructure.log.type.ActvtyCtgr;
import io.nicheblog.dreamdiary.infrastructure.web.controller.impl.BaseControllerImpl;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * UserSignupPageController
 * <pre>
 *  사용자 계정 신청 페이지 컨트롤러.
 * </pre>
 *
 * 명명 규약: 기능·화면 진입 계열은 {@code UserSignup*}, 저장 레코드(신청) 모델은 {@code UserSignupRequest*} 로 구분한다.
 *
 * 변경(signup-3): FTL 렌더 → Vue SPA 리다이렉트로 전환.
 * 비로그인 접근 가능 화면이므로 {@code /vue-app/user/signup} 으로 리다이렉트한다.
 *
 * @author nichefish
 */
@Controller
@RequiredArgsConstructor
@Log4j2
public class UserSignupPageController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.USER_SIGNUP_PAGE;
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.USER_SIGNUP;      // 작업 카테고리 (로그 적재용)

    /**
     * 계정 정보 신청 화면 조회
     * (비로그인 사용자도 외부에서 접근 가능.) (인증 없음)
     *
     * 변경(signup-3): FTL 렌더 → Vue SPA 리다이렉트.
     *
     * @return {@link String} -- Vue SPA 리다이렉트 경로
     */
    @GetMapping(Url.USER_SIGNUP_PAGE)
    public String userSignupRegForm() {
        return "redirect:/vue-app/user/signup";
    }
}