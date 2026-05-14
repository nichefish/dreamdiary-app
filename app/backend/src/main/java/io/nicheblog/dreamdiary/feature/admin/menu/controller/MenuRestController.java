package io.nicheblog.dreamdiary.feature.admin.menu.controller;

import io.nicheblog.dreamdiary.feature.admin.menu.model.*;
import io.nicheblog.dreamdiary.feature.admin.menu.service.MenuService;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
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
     * (관리자MNGR만 접근 가능.)
     *
     * @param searchParam 검색 조건을 담은 파라미터 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @GetMapping(Url.MENU_MAIN_LIST_AJAX)
    @Secured({Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> mainMenuListAjax(
            final @ModelAttribute("searchParam") MenuSearchParam searchParam
    ) throws Exception {

        // 페이징 정보 생성:: 공백시 pageSize=10, pageNo=1
        final Sort sort = Sort.by(Sort.Direction.ASC, "sortOrder");
        final List<MenuDto> menuList = menuService.getMainMenuList(searchParam, sort);
        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg).withList(menuList));
    }

    /**
     * 사이드바 메뉴 (사용자, useYn=Y) 목록 조회 (Ajax)
     * Vue SPA 사이드바 메뉴 렌더링용. 비관리자 메뉴만 반환.
     *
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @GetMapping(Url.MENUS)
    @ResponseBody
    public ResponseEntity<AjaxResponse> userMenuListAjax() throws Exception {

        final List<MenuDto> menuList = menuService.getUserMenuList();
        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg).withList(menuList));
    }

    /**
     * 메뉴 등록/수정 (Ajax)
     * (관리자MNGR만 접근 가능.)
     *
     * @param menu 등록/수정 처리할 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @PostMapping(value = {Url.MENUS, Url.MENU})
    @Secured({Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> menuRegistAjax(
            final @PathVariable(value = "id", required = false) Integer id,
            final @Valid MenuPostDto menu
    ) throws Exception {

        final boolean isModify = id != null;
        if (isModify) menu.setId(id);
        final ServiceResponse result = isModify ? menuService.modify(menu) : menuService.regist(menu);
        final boolean isSuccess = result.getRslt();
        final String rsltMsg = isSuccess ? MessageUtils.RSLT_SUCCESS : MessageUtils.RSLT_FAILURE;

        return ResponseEntity.ok(AjaxResponse.fromResponseWithObj(result, rsltMsg));
    }

    /**
     * 메뉴 관리 상세 조회 (Ajax)
     * (관리자MNGR만 접근 가능.)
     *
     * @param id 식별자
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @GetMapping(Url.MENU)
    @Secured({Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> menuDetailAjax(
            final @PathVariable("id") Integer id
    ) throws Exception {

        final MenuDto retrievedDto = menuService.getDtlDto(id);
        final boolean isSuccess = retrievedDto != null;
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg).withObj(retrievedDto));
    }

    /**
     * 메뉴 상태 변경 (Ajax)
     * (관리자MNGR만 접근 가능.)
     *
     * @param id 식별자
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @PatchMapping(Url.MENU)
    @Secured({Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> menuPatchAjax(
            final @PathVariable("id") Integer id,
            final @RequestBody MenuPatchDto patchDto
    ) throws Exception {

        final ServiceResponse result = menuService.patch(id, patchDto);
        final boolean isSuccess = result.getRslt();
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        return ResponseEntity.ok(AjaxResponse.fromResponseWithObj(result, rsltMsg));
    }

    /**
     * 관리자 > 메뉴 관리 > 정렬 순서 저장 (드래그앤드랍 결과 반영) (Ajax)
     * (관리자MNGR만 접근 가능.)
     *
     * @param menuParam 키+정렬 순서 목록을 담은 파라미터
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @PutMapping(Url.MENUS_SORT_ORDERS)
    @Secured({Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> menuSortOrdrAjax(
            final @RequestBody MenuParam menuParam
    ) throws Exception {

        final ServiceResponse result = menuService.sortOrder(menuParam.getSortOrders());
        final boolean isSuccess = result.getRslt();
        final String rsltMsg = isSuccess ? MessageUtils.RSLT_SUCCESS : MessageUtils.RSLT_FAILURE;

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
    @Secured({Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> menuTreeMoveAjax(
            final @RequestBody MenuTreeMoveParam moveParam
    ) throws Exception {

        final ServiceResponse result = menuService.moveTree(moveParam);
        final boolean isSuccess = result.getRslt();
        final String rsltMsg = isSuccess ? MessageUtils.RSLT_SUCCESS : MessageUtils.RSLT_FAILURE;

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg));
    }

    /**
     * 메뉴 관리 삭제 (Ajax)
     * (관리자MNGR만 접근 가능.)
     *
     * @param id 식별자
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @DeleteMapping(Url.MENU)
    @Secured({Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> menuDeleteAjax(
            final @PathVariable("id") Integer id
    ) throws Exception {

        final ServiceResponse result = menuService.delete(id);
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        return ResponseEntity.ok(AjaxResponse.fromResponseWithObj(result, rsltMsg));
    }
}
