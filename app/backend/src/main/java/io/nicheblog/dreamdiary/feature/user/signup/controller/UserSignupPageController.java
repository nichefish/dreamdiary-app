package io.nicheblog.dreamdiary.feature.user.signup.controller;

import io.nicheblog.dreamdiary.feature.admin.menu.type.PageName;
import io.nicheblog.dreamdiary.feature.admin.menu.type.SiteMenu;
import io.nicheblog.dreamdiary.feature.user.account.model.UserDto;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.infrastructure.code.Code;
import io.nicheblog.dreamdiary.infrastructure.code.service.CodeLookupService;
import io.nicheblog.dreamdiary.infrastructure.log.type.ActvtyCtgr;
import io.nicheblog.dreamdiary.infrastructure.web.controller.impl.BaseControllerImpl;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * UserSignupPageController
 * <pre>
 *  사용자 계정 신청 페이지 컨트롤러.
 * </pre>
 *
 * 명명 규약: 기능·화면 진입 계열은 {@code UserSignup*}, 저장 레코드(신청) 모델은 {@code UserSignupRequest*} 로 구분한다.
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

    private final CodeLookupService codeLookupService;

    /**
     * 계정 정보 신청 화면 조회
     * (비로그인 사용자도 외부에서 접근 가능.) (인증 없음)
     *
     * @param model 뷰에 데이터를 전달하기 위한 ModelMap 객체
     * @return {@link String} -- 화면 뷰 경로
     */
    @GetMapping(Url.USER_SIGNUP_PAGE)
    public String userSignupRegForm(
            final ModelMap model
    ) {

        /* 사이트 메뉴 설정 */
        model.addAttribute("menuLabel", SiteMenu.USER_SIGNUP);
        model.addAttribute("pageName", PageName.REG);

        // 빈 객체 주입 (freemarker error prevention)
        model.addAttribute("user", new UserDto());
        // 등록/수정 화면 플래그 세팅
        model.addAttribute(Constant.FORM_MODE, "regist");
        /**
         * 신청 화면 기본값(gmail.com 도메인 선택 등)을 Freemarker·부트스트랩 JSON 과 공유한다.
         * 변경 전: user_reqst 폼에 isReg 변수가 없어 Freemarker 등록 분기(isReg)가 기대와 다르게 동작할 수 있었다.
         * 변경 후: 등록 전용 신청 URL 이므로 명시적으로 true 를 세팅한다.
         */
        model.addAttribute("isReg", Boolean.TRUE);
        // 코드 정보 모델에 추가
        codeLookupService.setCdListToModel(Code.AUTH_CD, model);
        codeLookupService.setCdListToModel(Code.CMPY_CD, model);
        codeLookupService.setCdListToModel(Code.TEAM_CD, model);
        codeLookupService.setCdListToModel(Code.EMPLYM_CD, model);
        codeLookupService.setCdListToModel(Code.RANK_CD, model);

        return "/view/feature/user/signup/user_signup_page";
    }
}
