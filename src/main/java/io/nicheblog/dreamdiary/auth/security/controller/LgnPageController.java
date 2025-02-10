package io.nicheblog.dreamdiary.auth.security.controller;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.domain.admin.menu.SiteMenu;
import io.nicheblog.dreamdiary.domain.admin.menu.model.PageNm;
import io.nicheblog.dreamdiary.extension.log.actvty.ActvtyCtgr;
import io.nicheblog.dreamdiary.extension.log.actvty.aspect.LogActvtyPageControllerAspect;
import io.nicheblog.dreamdiary.extension.log.actvty.model.LogActvtyParam;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.intrfc.controller.impl.BaseControllerImpl;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Nullable;
import javax.annotation.security.PermitAll;

/**
 * LgnPageController
 * <pre>
 *  로그인 페이지 컨트롤러.
 * </pre>
 *
 * @author nichefish
 * @see LogActvtyPageControllerAspect
 */
@Controller
@RequiredArgsConstructor
@Log4j2
public class LgnPageController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.AUTH_LGN_FORM;
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.LGN;      // 작업 카테고리 (로그 적재용)

    @Value("${remember-me.param}")
    private String REMEMBER_ME_PARAM;

    /**
     * 로그인 화면 조회
     *
     * @param dupLgnAt 중복 로그인 여부를 나타내는 파라미터 (nullable)
     * @param model 뷰에 데이터를 전달하는 ModelMap 객체
     * @return {@link String} -- 로그인 화면 뷰 경로
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @RequestMapping(Url.AUTH_LGN_FORM)
    @PermitAll
    public String lgnForm(
            final @RequestParam("dupLgnAt") @Nullable String dupLgnAt,
            final LogActvtyParam logParam,
            final ModelMap model
    ) throws Exception {

        /* 사이트 메뉴 설정 */
        model.addAttribute("menuLabel", SiteMenu.LGN_PAGE);
        model.addAttribute("pageNm", PageNm.DEFAULT);

        // 로그인 상태일 경우:: 메인 화면으로 리다이렉트
        if (AuthUtils.isAuthenticated()) return "redirect:" + Url.MAIN;

        // remember-me 관련 파라미터 세팅
        model.addAttribute("REMEMBER_ME_PARAM", REMEMBER_ME_PARAM);

        // 중복 로그인으로 인해 로그인 면으로 튕겨나왔을 경우 alert
        if ("Y".equals(dupLgnAt)) MessageUtils.alertMessage("중복 로그인 방지에 의해 로그아웃 처리되었습니다.", Url.AUTH_LGN_FORM);

        // 로그 관련 세팅
        logParam.setResult(true, MessageUtils.RSLT_SUCCESS);

        return "/view/auth/security/lgn_form";
    }

    /**
     * GET으로 로그인 처리/로그아웃 페이지 접근시 로그인 화면으로 리다이렉트
     */
    @GetMapping({ Url.AUTH_LGN_PROC, Url.AUTH_LGOUT})
    public String authRedirection(
            //
    ) {

        log.info("'GET' access in lgnProc!");

        return "redirect:" + Url.AUTH_LGN_FORM;
    }
}
