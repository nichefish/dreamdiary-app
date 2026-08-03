package io.nicheblog.dreamdiary.feature.user.account.controller;

import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.infrastructure.log.type.ActvtyCtgr;
import io.nicheblog.dreamdiary.infrastructure.web.controller.impl.BaseControllerImpl;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * UserPageController
 */
@Controller
@RequiredArgsConstructor
@Log4j2
public class UserPageController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.USER_LIST;
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.USER;

    /**
     * 계정관리 화면은 Vue SPA로 위임한다.
     */
    @GetMapping({Url.USER_LIST, Url.USER_REGIST_FORM})
    @PreAuthorize("hasAuthority('menu.admin.user_account')")
    public String userAccountPage() {
        return "redirect:/vue-app/admin/users";
    }

    /**
     * 기존 상세 URL은 Vue SPA 상세 모달로 위임한다.
     */
    @GetMapping(Url.USER_DETAIL)
    @PreAuthorize("hasAuthority('menu.admin.user_account')")
    public String userDetailPage(final @RequestParam("id") Integer id) {
        return "redirect:/vue-app/admin/users?id=" + id;
    }

    /**
     * 기존 수정 URL은 Vue SPA 수정 모달로 위임한다.
     */
    @GetMapping(Url.USER_MODIFY_FORM)
    @PreAuthorize("hasAuthority('menu.admin.user_account')")
    public String userModifyPage(final @RequestParam("id") Integer id) {
        return "redirect:/vue-app/admin/users?id=" + id + "&mode=edit";
    }
}
