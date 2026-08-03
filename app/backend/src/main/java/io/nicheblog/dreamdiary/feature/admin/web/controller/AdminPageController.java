package io.nicheblog.dreamdiary.feature.admin.web.controller;

import io.nicheblog.dreamdiary.auth.security.model.RoleDto;
import io.nicheblog.dreamdiary.auth.security.service.RoleService;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import io.nicheblog.dreamdiary.infrastructure.code.Code;
import io.nicheblog.dreamdiary.infrastructure.log.type.ActvtyCtgr;
import io.nicheblog.dreamdiary.infrastructure.web.controller.impl.BaseControllerImpl;
import io.nicheblog.dreamdiary.infrastructure.web.model.AjaxResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AdminPageController
 */
@Controller
@RequiredArgsConstructor
@Log4j2
public class AdminPageController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.ADMIN_PAGE;
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.ADMIN;

    private final RoleService roleService;

    @GetMapping(Url.ADMIN_PAGE)
    @PreAuthorize("hasAuthority('menu.admin.page')")
    public String adminPage() {
        return "redirect:/vue-app/admin";
    }

    @GetMapping(Url.ADMIN_PAGE_BOOTSTRAP)
    @PreAuthorize("hasAuthority('menu.admin.page')")
    @ResponseBody
    public ResponseEntity<AjaxResponse> adminPageBootstrap() throws Exception {
        final List<RoleDto> roleList = roleService.getListDto(new HashMap<>());
        final Map<String, Object> meta = new HashMap<>();
        meta.put("authMngrKey", Code.AUTH_MNGR);
        meta.put("authUserKey", Code.AUTH_USER);
        meta.put("authDevKey", Code.AUTH_DEV);
        meta.put("currYy", Integer.parseInt(DateUtils.getCurrYyStr()));

        final Map<String, Object> payload = new HashMap<>();
        payload.put("meta", meta);
        payload.put("roleList", roleList);

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(true, MessageUtils.getMessage("common.result.success")).withObj(payload));
    }
}
