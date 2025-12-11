package io.nicheblog.dreamdiary.domain.admin.web.controller;

import io.nicheblog.dreamdiary.domain.admin.menu.SiteMenu;
import io.nicheblog.dreamdiary.domain.admin.menu.model.PageNm;
import io.nicheblog.dreamdiary.extension.log.actvty.ActvtyCtgr;
import io.nicheblog.dreamdiary.extension.log.actvty.aspect.LogActvtyPageControllerAspect;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.intrfc.controller.impl.BaseControllerImpl;
import io.nicheblog.dreamdiary.global.model.AjaxResponse;
import lombok.Getter;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * MainPageController
 * <pre>
 *  메인 화면 페이지 컨트롤러.
 * </pre>
 *
 * @author nichefish
 * @see LogActvtyPageControllerAspect
 */
@Controller
public class MainPageController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.MAIN;
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.DEFAULT;      // 작업 카테고리 (로그 적재용)

    /**
     * 메인 화면 :: 사용자
     *
     * @param model 뷰에 전달할 데이터를 저장하는 ModelMap 객체
     * @return {@link String} -- 뷰 이름을 나타내는 문자열
     */
    @GetMapping(value = {Url.ROOT, Url.MAIN})
    public String mainPage(
            final ModelMap model
    ) {

        /* 사이트 메뉴 설정 */
        model.addAttribute("menuLabel", SiteMenu.MAIN);
        model.addAttribute("pageNm", PageNm.DEFAULT);

        // TODO: 접근 권한 통제

        // 메인 화면 꾸밀때까지 저널 일자 화면으로 리다이렉트
        return "redirect:" + Url.JRNL_DAY_PAGE;
    }

    /**
     * 메인 화면 :: 관리자
     *
     * @param model 뷰에 전달할 데이터를 저장하는 ModelMap 객체
     * @return {@link String} -- 뷰 이름을 나타내는 문자열
     */
    @GetMapping(value = {Url.ADMIN_MAIN})
    public String adminMainPage(
            final ModelMap model
    ) {

        /* 사이트 메뉴 설정 */
        model.addAttribute("menuLabel", SiteMenu.ADMIN_MAIN);
        model.addAttribute("pageNm", PageNm.DEFAULT);

        // TODO: 접근 권한 통제

        // 메인 화면 꾸밀때까지 사이트 관리 화면으로 리다이렉트
        return "redirect:" + Url.ADMIN_PAGE;
    }

    /**
     * alive-check
     *
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @GetMapping(value = Url.API_ALIVE_CHECK)
    @ResponseBody
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    public ResponseEntity<AjaxResponse> aliveCheck() {

        return ResponseEntity.ok(new AjaxResponse());
    }
}
