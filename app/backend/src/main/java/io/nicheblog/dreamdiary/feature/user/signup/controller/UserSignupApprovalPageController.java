package io.nicheblog.dreamdiary.feature.user.signup.controller;

import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.infrastructure.log.type.ActvtyCtgr;
import io.nicheblog.dreamdiary.infrastructure.web.controller.impl.BaseControllerImpl;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * UserSignupApprovalPageController
 * <pre>
 *  사용자 관리 > 계정 신청 승인관리 화면 컨트롤러.
 * </pre>
 *
 * 변경 전: 계정 승인/반려 액션이 계정관리(user_list) 화면 상단 카드에 혼재되어 있었다.
 * 변경 후: 승인 대기 처리 흐름은 전용 페이지(`/app/user/signup/list.do`)로 분리하여, 계정관리(사용자 마스터 유지보수)와 역할을 구분한다.
 *
 * 변경(signup-3): FTL SSR 데이터 주입 → Vue SPA 리다이렉트로 전환.
 * 데이터는 REST `GET /api/user/signup-requests` 로 조회한다.
 *
 * @author nichefish
 */
@Controller
@RequiredArgsConstructor
@Log4j2
public class UserSignupApprovalPageController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.USER_SIGNUP_LIST;
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.USER_SIGNUP;      // 작업 카테고리 (로그 적재용)

    /**
     * 사용자 관리 > 계정 신청 승인관리 목록 화면 조회
     * (관리자MNGR만 접근 가능.)
     *
     * 변경(signup-3): FTL 렌더 → Vue SPA 리다이렉트.
     *
     * @return {@link String} -- Vue SPA 리다이렉트 경로
     */
    @GetMapping(Url.USER_SIGNUP_LIST)
    @Secured(Constant.ROLE_MNGR)
    public String userSignupApprovalList() {
        // 변경: 계정 신청 승인이 계정 관리(/admin/users)의 `계정 신청 승인` 탭으로 흡수됐다.
        return "redirect:/vue-app/admin/users?tab=signup";
    }
}