package io.nicheblog.dreamdiary.feature.user.my.controller;

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
 * UserMyPageController
 */
@Controller
@RequiredArgsConstructor
@Log4j2
public class UserMyPageController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.USER_MY_PAGE;
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.USER_MY;

    @GetMapping(Url.USER_MY_PAGE)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    public String myInfoPage() {
        return "redirect:/vue-app/my";
    }
}
