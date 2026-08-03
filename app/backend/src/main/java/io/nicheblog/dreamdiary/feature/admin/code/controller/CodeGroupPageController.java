package io.nicheblog.dreamdiary.feature.admin.code.controller;

import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.infrastructure.log.type.ActvtyCtgr;
import io.nicheblog.dreamdiary.infrastructure.web.controller.impl.BaseControllerImpl;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
@Log4j2
public class CodeGroupPageController extends BaseControllerImpl {
    @Getter
    private final String baseUrl = Url.CODE_ADMIN_PAGE;
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.CODE;

    @GetMapping(Url.CODE_ADMIN_PAGE)
    @PreAuthorize("hasAuthority('menu.admin.code')")
    public String codeGroupList() {
        return "redirect:/vue-app/admin/code";
    }
}
