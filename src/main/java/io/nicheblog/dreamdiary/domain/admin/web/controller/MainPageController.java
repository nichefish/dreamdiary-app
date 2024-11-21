package io.nicheblog.dreamdiary.domain.admin.web.controller;

import io.nicheblog.dreamdiary.domain.admin.menu.SiteMenu;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global._common.log.actvty.ActvtyCtgr;
import io.nicheblog.dreamdiary.global.aspect.log.LogActvtyPageControllerAspect;
import io.nicheblog.dreamdiary.global.intrfc.controller.impl.BaseControllerImpl;
import lombok.Getter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * MainPageController
 * <pre>
 *  메인 화면 페이지 컨트롤러.
 * </pre>
 *
 * @see LogActvtyPageControllerAspect
 * @author nichefish
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
        model.addAttribute(Constant.SITE_MENU, SiteMenu.MAIN_PORTAL.setAcsPageInfo(Constant.PAGE_MAIN));

        // TODO: 접근 권한 통제

        // 메인 화면 꾸밀때까지 공지사항 목록 화면으로 리다이렉트
        return "redirect:" + Url.NOTICE_LIST;
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
        model.addAttribute(Constant.SITE_MENU, SiteMenu.ADMIN_MAIN.setAcsPageInfo(Constant.PAGE_MAIN));

        // TODO: 접근 권한 통제

        // 메인 화면 꾸밀때까지 사이트 관리 화면으로 리다이렉트
        return "redirect:" + Url.ADMIN_PAGE;
    }
}
