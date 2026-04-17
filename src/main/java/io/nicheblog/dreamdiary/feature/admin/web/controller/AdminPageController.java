package io.nicheblog.dreamdiary.feature.admin.web.controller;

import io.nicheblog.dreamdiary.auth.security.model.RoleDto;
import io.nicheblog.dreamdiary.auth.security.service.RoleService;
import io.nicheblog.dreamdiary.feature.admin.menu.type.PageNm;
import io.nicheblog.dreamdiary.feature.admin.menu.type.SiteMenu;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.infrastructure.log.actvty.ActvtyCtgr;
import io.nicheblog.dreamdiary.infrastructure.web.controller.impl.BaseControllerImpl;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.HashMap;
import java.util.List;

/**
 * AdminPageController
 * <pre>
 *  사이트 관리 > 사이트 관리 페이지 컨트롤러.
 * </pre>
 *
 * @author nichefish
 */
@Controller
@RequiredArgsConstructor
@Log4j2
public class AdminPageController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.ADMIN_PAGE;
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.ADMIN;      // 작업 카테고리 (로그 적재용)

    private final RoleService roleService;

    /**
     * 사이트 관리 > 사이트 관리 화면 조회
     * (관리자MNGR만 접근 가능.)
     * 
     * @param model 뷰에 전달할 데이터를 저장하는 ModelMap 객체
     * @return {@link String} -- 뷰 이름을 나타내는 문자열
     */
    @GetMapping(Url.ADMIN_PAGE)
    @Secured(Constant.ROLE_MNGR)
    public String adminPage(
            final ModelMap model
    ) throws Exception {

        /* 사이트 메뉴 설정 */
        model.addAttribute("menuLabel", SiteMenu.ADMIN_PAGE);
        model.addAttribute("pageNm", PageNm.DEFAULT);

        // 권한 정보 조회
        List<RoleDto> roleList = roleService.getListDto(new HashMap<>());
        model.addAttribute("roleList", roleList);

        return "/view/feature/admin/admin_page";
    }

    /**
     * 사이트 관리 > 테스트 페이지
     * (관리자MNGR만 접근 가능.)
     *
     * @param model 뷰에 전달할 데이터를 저장하는 ModelMap 객체
     * @return {@link String} -- 뷰 이름을 나타내는 문자열
     */
    @GetMapping(Url.ADMIN_TEST)
    @Secured(Constant.ROLE_MNGR)
    public String testPage(
            final ModelMap model
    ) {

        /* 사이트 메뉴 설정 */
        model.addAttribute("menuLabel", SiteMenu.ADMIN_PAGE);

        return "/view/feature/admin/test_page";
    }
}
