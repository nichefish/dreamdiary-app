package io.nicheblog.dreamdiary.auth.security.controller;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.admin.menu.type.PageName;
import io.nicheblog.dreamdiary.feature.admin.menu.type.SiteMenu;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.infrastructure.log.type.ActvtyCtgr;
import io.nicheblog.dreamdiary.infrastructure.web.controller.impl.BaseControllerImpl;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Nullable;
import javax.annotation.security.PermitAll;

/**
 * LoginPageController
 * <pre>
 *  로그인 페이지 컨트롤러.
 * </pre>
 *
 * @author nichefish
 */
@Controller
@RequiredArgsConstructor
@Log4j2
public class LoginPageController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.APP_AUTH_LGN_FORM;
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.LGN;      // 작업 카테고리 (로그 적재용)

    /**
     * 로그인 화면 조회
     *
     * @param dupLoginAt 중복 로그인 여부를 나타내는 파라미터 (nullable)
     * @param model 뷰에 데이터를 전달하는 ModelMap 객체
     * @return {@link String} -- 로그인 화면 뷰 경로
     */
    @RequestMapping(Url.APP_AUTH_LGN_FORM)
    @PermitAll
    public String loginForm(
            final @RequestParam("dupLoginAt") @Nullable String dupLoginAt
    ) {
        // 로그인 상태일 경우:: 메인 화면으로 리다이렉트
        if (AuthUtils.isAuthenticated()) return "redirect:" + Url.MAIN;

        // 레거시 로그인 URL → Vue SPA 로그인 화면으로 리다이렉트
        if ("Y".equals(dupLoginAt)) return "redirect:" + Url.VUE_SIGN_IN + "?dupLoginAt=Y";
        return "redirect:" + Url.VUE_SIGN_IN;
    }

    /**
     * GET으로 로그인 처리/로그아웃 페이지 접근시 로그인 화면으로 리다이렉트
     */
    @GetMapping({ Url.API_AUTH_LGN_PROC, Url.API_AUTH_LGOUT})
    public String authRedirection(
            //
    ) {

        log.info("'GET' access in loginProc!");

        return "redirect:" + Url.VUE_SIGN_IN;
    }
}
