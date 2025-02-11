package io.nicheblog.dreamdiary.domain.admin.web.controller;

import io.nicheblog.dreamdiary.domain.admin.menu.SiteMenu;
import io.nicheblog.dreamdiary.domain.admin.menu.model.PageNm;
import io.nicheblog.dreamdiary.extension.log.actvty.ActvtyCtgr;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.intrfc.controller.impl.BaseControllerImpl;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

/**
 * ErrorPageController
 * <pre>
 *  에러 화면 조회 컨트롤러.
 *  (스프링 기본 제공 ErrorController 구현 : WhiteLabel 에러페이지 override)
 * </pre>
 *
 * @author nichefish
 */
@Controller
@Log4j2
public class ErrorPageController
        extends BaseControllerImpl
        implements ErrorController {

    @Getter
    private final String baseUrl = Url.ERROR_PAGE;
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.DEFAULT;      // 작업 카테고리 (로그 적재용)

    /**
     * 기본 에러 화면(/error) :: 에러 코드에 따라 반환 페이지 분기
     *
     * @param request 요청 객체
     * @return {@link String} -- 에러 상태 코드에 따라 반환할 뷰 경로
     */
    @GetMapping(Url.ERROR)
    public String handleError(HttpServletRequest request) {
        final Object statusObj = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        // 응답 코드에 따른 페이지 분기
        if (statusObj == null) return "/view/global/_common/error/error_page";

        final int statusCode = Integer.parseInt(statusObj.toString());
        final HttpStatus status = HttpStatus.resolve(statusCode);
        if (status == null) return "/view/global/_common/error/error_page";
        return switch (status) {
            case BAD_REQUEST -> "/view/global/_common/error/error_bad_request";
            case FORBIDDEN -> "/view/global/_common/error/error_access_denied";
            case NOT_FOUND -> "/view/global/_common/error/error_not_found";
            default -> "/view/global/_common/error/error_page";
        };
    }

    /**
     * 에러 화면 (404 NOT FOUND).
     * (비로그인 사용자도 외부에서 접근 가능.)
     *
     * @param model ModelMap 객체
     * @return {@link String} -- 에러 화면 뷰 경로
     */
    @GetMapping(Url.ERROR_NOT_FOUND)
    public String errorNotFound(
            final ModelMap model
    ) {

        /* 사이트 메뉴 설정 */
        model.addAttribute("menuLabel", SiteMenu.ERROR);
        model.addAttribute("pageNm", PageNm.DEFAULT);

        // 에러 화면 반환
        return "/view/global/_common/error/error_not_found";
    }

    /**
     * 에러 화면 (비인가 접근).
     * (비로그인 사용자도 외부에서 접근 가능.) (인증 없음)
     *
     * @param model ModelMap 객체
     * @return {@link String} -- 에러 화면 뷰 경로
     */
    @GetMapping(Url.ERROR_ACCESS_DENIED)
    public String errorAccessDenied(
            final ModelMap model
    ) {

        /* 사이트 메뉴 설정 */
        model.addAttribute("menuLabel", SiteMenu.ERROR);
        model.addAttribute("pageNm", PageNm.DEFAULT);

        // 에러 화면으로 리다이렉트 리다리렉트
        return "/view/global/_common/error/error_access_denied";
    }

    /**
     * 에러 화면 (공통).
     * (비로그인 사용자도 외부에서 접근 가능.) (인증 없음)
     *
     * @param model ModelMap 객체
     * @return {@link String} -- 에러 화면 뷰 경로
     */
    @GetMapping(Url.ERROR_PAGE)
    public String errorPage(
            final ModelMap model
    ) {

        /* 사이트 메뉴 설정 */
        model.addAttribute("menuLabel", SiteMenu.ERROR);
        model.addAttribute("pageNm", PageNm.DEFAULT);

        // 에러 화면으로 리다이렉트 리다리렉트
        return "/view/global/_common/error/error_page";
    }
}
