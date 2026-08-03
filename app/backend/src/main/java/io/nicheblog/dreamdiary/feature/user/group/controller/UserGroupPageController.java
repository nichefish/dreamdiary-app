package io.nicheblog.dreamdiary.feature.user.group.controller;

import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.infrastructure.log.type.ActvtyCtgr;
import io.nicheblog.dreamdiary.infrastructure.web.controller.impl.BaseControllerImpl;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * UserGroupPageController
 * <pre>
 *  사용자 그룹 관리 화면 진입. Vue SPA 로 리다이렉트.
 * </pre>
 *
 * @author nichefish
 */
@Controller
@RequiredArgsConstructor
@Log4j2
public class UserGroupPageController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.USER_GROUP_PAGE;
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.USER_GROUP;

    @GetMapping(Url.USER_GROUP_PAGE)
    @PreAuthorize("hasAuthority('menu.admin.user_group')")
    public String userGroupPage() {
        return "redirect:/vue-app/admin/user-groups";
    }
}
