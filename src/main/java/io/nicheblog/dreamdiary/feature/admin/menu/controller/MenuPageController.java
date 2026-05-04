package io.nicheblog.dreamdiary.feature.admin.menu.controller;

import io.nicheblog.dreamdiary.feature.admin.menu.model.MenuSearchParam;
import io.nicheblog.dreamdiary.feature.admin.menu.type.PageName;
import io.nicheblog.dreamdiary.feature.admin.menu.type.SubmenuExpandType;
import io.nicheblog.dreamdiary.feature.admin.menu.type.SiteMenu;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.infrastructure.log.type.ActvtyCtgr;
import io.nicheblog.dreamdiary.infrastructure.web.controller.impl.BaseControllerImpl;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private final String baseUrl = Url.MENU_PAGE;             // 기본 URL
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.MENU;        // 작업 카테고리 (로그 적재용)

    /**
     * 관리자 > 메뉴 관리 > 메뉴 관리 화면 조회
     * (관리자MNGR만 접근 가능.)
     *
     * @param searchParam 검색 조건을 담은 파라미터 객체
     * @param model 뷰에 데이터를 전달하기 위한 ModelMap 객체
     * @return {@link String} -- 화면 뷰 경로
     */
    @GetMapping(Url.MENU_PAGE)
    @Secured({Constant.ROLE_MNGR})
    public String menuPage(
            final @ModelAttribute("searchParam") MenuSearchParam searchParam,
            final ModelMap model
    ) throws Exception {

        /* 사이트 메뉴 설정 */
        model.addAttribute("menuLabel", SiteMenu.MENU);
        model.addAttribute("pageName", PageName.DEFAULT);

        // enum 데이터를 모델에 추가 (코드테이블 의존 제거)
        final List<Map<String, String>> submenuExpandTypes = Arrays.stream(SubmenuExpandType.values())
                .map(type -> Map.of("code", type.name(), "codeName", type.desc))
                .collect(Collectors.toList());
        model.addAttribute("SUBMENU_EXPAND_TYPES", submenuExpandTypes);

        return "/view/feature/admin/menu/menu_page";
    }
}
