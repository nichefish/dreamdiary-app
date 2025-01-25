package io.nicheblog.dreamdiary.auth.controller;

import io.nicheblog.dreamdiary.auth.provider.OAuth2Provider;
import io.nicheblog.dreamdiary.global.Url;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * OAuth2Controller
 *
 * @author nichefish
 */
@Controller
@RequiredArgsConstructor
@Log4j2
public class OAuth2Controller {

    private final OAuth2Provider oauth2Provider;

    /**
     * OAuth2:: Google RedirectUri 스크립트 커스터마이즈를 위한 직접 페이지 세팅
     *
     * @return view
     */
    @GetMapping(Url.OAUTH2_GOOGLE_REDIRECT_URI)
    public String oauth2Google() {
        // 자동으로 처리해주므로 단순 redirect만 수행
        return "/view/auth/oauth2/oauth2_popup_google";
    }

    /**
     * OAuth2:: Google RedirectUri 스크립트 커스터마이즈를 위한 직접 페이지 세팅
     *
     * @return view
     */
    @GetMapping(Url.OAUTH2_NAVER_REDIRECT_URI)
    public String oauth2Naver() {
        // 자동으로 처리해주므로 단순 redirect만 수행
        return "/view/auth/oauth2/oauth2_popup_naver";
    }
}
