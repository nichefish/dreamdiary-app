package io.nicheblog.dreamdiary.feature.admin.auth.policy.controller;

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
 * AuthPolicyPageController
 */
@Controller
@RequiredArgsConstructor
@Log4j2
public class AuthPolicyPageController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.AUTH_POLICY_PAGE;
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.AUTH_POLICY;

    @GetMapping(Url.AUTH_POLICY_PAGE)
    @Secured(Constant.ROLE_MNGR)
    public String authPolicyForm() {
        return "redirect:/vue-app/admin/auth-policy";
    }
}
