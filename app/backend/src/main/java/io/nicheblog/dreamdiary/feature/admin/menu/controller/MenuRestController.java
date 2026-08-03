package io.nicheblog.dreamdiary.feature.admin.menu.controller;

import io.nicheblog.dreamdiary.feature.admin.menu.model.*;
import io.nicheblog.dreamdiary.feature.admin.menu.service.MenuService;
import io.nicheblog.dreamdiary.auth.permission.PermissionKey;
import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.infrastructure.code.Code;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.infrastructure.log.type.ActvtyCtgr;
import io.nicheblog.dreamdiary.infrastructure.web.controller.impl.BaseControllerImpl;
import io.nicheblog.dreamdiary.infrastructure.web.model.AjaxResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * MenuRestController
 * <pre>
 *  메뉴 관리 API 컨트롤러.
 * </pre>
 *
 * @author nichefish
 */
@Controller
@RequiredArgsConstructor
@Log4j2
public class MenuRestController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.MENU_ADMIN_PAGE;             // 기본 URL
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.MENU;        // 작업 카테고리 (로그 적재용)

    private final MenuService menuService;

    /**
     * 메뉴 관리 (메인) 목록 조회 (Ajax)
     * (menu.admin.menu permission 필요.)
     *
     * @param searchParam 검색 조건을 담은 파라미터 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @GetMapping(Url.MENU_MAIN_LIST_AJAX)
    @PreAuthorize("hasAuthority('menu.admin.menu')")
    @ResponseBody
    public ResponseEntity<AjaxResponse> mainMenuListAjax(
            final @ModelAttribute("searchParam") MenuSearchParam searchParam
    ) throws Exception {

        // 페이징 정보 생성:: 공백시 pageSize=10, pageNo=1
        final Sort sort = Sort.by(Sort.Direction.ASC, "sortOrder");
        final List<MenuDto> menuList = menuService.getMainMenuList(searchParam, sort);
        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.getMessage("common.result.success");

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg).withList(menuList));
    }

    /**
     * 사이드바 메뉴 (useYn=Y) 목록 조회 (Ajax)
     * Vue SPA 사이드바 메뉴 렌더링용. mode=MNGR 은 menu.view.admin permission 이 있을 때만 관리자 메뉴를 반환.
     *
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @GetMapping(Url.MENUS)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> userMenuListAjax(
            @RequestParam(value = "mode", defaultValue = "USER") final String mode,
            @RequestParam(value = "includeHidden", defaultValue = "false") final boolean includeHidden
    ) throws Exception {

        final boolean mngrModeRequested = Code.AUTH_MNGR.equals(mode);
        final boolean canUseMngrMode = AuthUtils.hasPermission(PermissionKey.MENU_VIEW_ADMIN);
        final List<MenuDto> menuList = mngrModeRequested && canUseMngrMode
                ? (includeHidden ? menuService.getMngrMenuMetaList() : menuService.getMngrMenuList())
                : (includeHidden ? menuService.getUserMenuMetaList() : menuService.getUserMenuList());
        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.getMessage("common.result.success");

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg).withList(menuList));
    }

    /**
     * 메뉴 등록/수정 (Ajax)
     * (menu.admin.menu permission 필요.)
     *
     * @param menu 등록/수정 처리할 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @PostMapping(value = {Url.MENUS, Url.MENU})
    @PreAuthorize("hasAuthority('menu.admin.menu')")
    @ResponseBody
    public ResponseEntity<AjaxResponse> menuRegistAjax(
            final @PathVariable(value = "id", required = false) Integer id,
            final @Valid MenuPostDto menu
    ) throws Exception {

        final boolean isModify = id != null;
        if (isModify) menu.setId(id);
        final ServiceResponse result = isModify ? menuService.modify(menu) : menuService.regist(menu);
        final boolean isSuccess = result.getRslt();
        final String rsltMsg = isSuccess ? MessageUtils.getMessage("common.result.success") : MessageUtils.getMessage("common.result.failure");

        return ResponseEntity.ok(AjaxResponse.fromResponseWithObj(result, rsltMsg));
    }

    /**
     * 메뉴 관리 상세 조회 (Ajax)
     * (menu.admin.menu permission 필요.)
     *
     * @param id 식별자
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @GetMapping(Url.MENU)
    @PreAuthorize("hasAuthority('menu.admin.menu')")
    @ResponseBody
    public ResponseEntity<AjaxResponse> menuDetailAjax(
            final @PathVariable("id") Integer id
    ) throws Exception {

        final MenuDto retrievedDto = menuService.getDtlDto(id);
        // 다국어 번역 목록 주입 (locale 별 메뉴명/설명, ko 는 menu_name 기준이라 제외)
        if (retrievedDto != null) {
            retrievedDto.setI18nList(menuService.getMenuI18nList(id));
        }
        final boolean isSuccess = retrievedDto != null;
        final String rsltMsg = MessageUtils.getMessage("common.result.success");

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg).withObj(retrievedDto));
    }

    /**
     * 메뉴 상태 변경 (Ajax)
     * (menu.admin.menu permission 필요.)
     *
     * @param id 식별자
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @PatchMapping(Url.MENU)
    @PreAuthorize("hasAuthority('menu.admin.menu')")
    @ResponseBody
    public ResponseEntity<AjaxResponse> menuPatchAjax(
            final @PathVariable("id") Integer id,
            final @RequestBody MenuPatchDto patchDto
    ) throws Exception {

        final ServiceResponse result = menuService.patch(id, patchDto);
        final boolean isSuccess = result.getRslt();
        final String rsltMsg = MessageUtils.getMessage("common.result.success");

        return ResponseEntity.ok(AjaxResponse.fromResponseWithObj(result, rsltMsg));
    }

    /**
     * 관리자 > 메뉴 관리 > 정렬 순서 저장 (드래그앤드랍 결과 반영) (Ajax)
     * (menu.admin.menu permission 필요.)
     *
     * @param menuParam 키+정렬 순서 목록을 담은 파라미터
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @PutMapping(Url.MENUS_SORT_ORDERS)
    @PreAuthorize("hasAuthority('menu.admin.menu')")
    @ResponseBody
    public ResponseEntity<AjaxResponse> menuSortOrdrAjax(
            final @RequestBody MenuParam menuParam
    ) throws Exception {

        final ServiceResponse result = menuService.sortOrder(menuParam.getSortOrders());
        final boolean isSuccess = result.getRslt();
        final String rsltMsg = isSuccess ? MessageUtils.getMessage("common.result.success") : MessageUtils.getMessage("common.result.failure");

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg));
    }

    /**
     * 관리자 > 메뉴 관리 > 서브메뉴 부모 이동/정렬 반영 (Ajax)
     * (관리자MNGR만 접근 가능)
     *
     * @param moveParam 메뉴 이동 payload
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @PutMapping(Url.MENUS_TREE)
    @PreAuthorize("hasAuthority('menu.admin.menu')")
    @ResponseBody
    public ResponseEntity<AjaxResponse> menuTreeMoveAjax(
            final @RequestBody MenuTreeMoveParam moveParam
    ) throws Exception {

        final ServiceResponse result = menuService.moveTree(moveParam);
        final boolean isSuccess = result.getRslt();
        final String rsltMsg = isSuccess ? MessageUtils.getMessage("common.result.success") : MessageUtils.getMessage("common.result.failure");

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg));
    }

    /**
     * 메뉴 관리 삭제 (Ajax)
     * (menu.admin.menu permission 필요.)
     *
     * @param id 식별자
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @DeleteMapping(Url.MENU)
    @PreAuthorize("hasAuthority('menu.admin.menu')")
    @ResponseBody
    public ResponseEntity<AjaxResponse> menuDeleteAjax(
            final @PathVariable("id") Integer id
    ) throws Exception {

        final ServiceResponse result = menuService.delete(id);
        final String rsltMsg = MessageUtils.getMessage("common.result.success");

        return ResponseEntity.ok(AjaxResponse.fromResponseWithObj(result, rsltMsg));
    }
}
