package io.nicheblog.dreamdiary.auth.controller;

import io.nicheblog.dreamdiary.global.Url;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * OAuth2Controller
 *
 * @author nichefish
 */
@Controller
public class OAuth2Controller {

    /**
     * OAuth2 Google RedirectUri 스크립트 커스터마이즈를 위한 직접 페이지 세팅
     *
     * @return view
     */
    @GetMapping(Url.OAUTH2_GOOGLE_REDIRECT_URI)
    public String oauth2Google() {

        return "/view/auth/_oauth2_redirect";
    }
}
