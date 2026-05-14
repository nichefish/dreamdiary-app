package io.nicheblog.dreamdiary.feature.admin.menu.controller;

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
 * MenuPageController
 * <pre>
 *  메뉴 관리 페이지 컨트롤러.
 * </pre>
 *
 * @author nichefish
 */
@Controller
@RequiredArgsConstructor
@Log4j2
public class MenuPageController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.MENU_ADMIN_PAGE;             // 기본 URL
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.MENU;        // 작업 카테고리 (로그 적재용)

    /**
     * 메뉴 관리 화면은 Vue SPA로 위임한다.
     */
    @GetMapping(Url.MENU_ADMIN_PAGE)
    @Secured({Constant.ROLE_MNGR})
    public String menuPage() {
        return "redirect:/vue-app/admin/menu";
    }
}
