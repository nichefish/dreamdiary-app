package io.nicheblog.dreamdiary.auth.security.controller;

import io.nicheblog.dreamdiary.auth.jwt.provider.JwtTokenProvider;
import io.nicheblog.dreamdiary.auth.security.exception.AlreadyAuthenticatedException;
import io.nicheblog.dreamdiary.auth.security.exception.AuthenticationFailureException;
import io.nicheblog.dreamdiary.feature.user.signup.service.UserSignupService;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.infrastructure.log.type.ActvtyCtgr;
import io.nicheblog.dreamdiary.infrastructure.web.controller.impl.BaseControllerImpl;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;

/**
 * AuthPageController
 *
 * @author nichefish
 */
@Controller
@RequiredArgsConstructor
public class AuthPageController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.USER_SIGNUP_PAGE;             // 기본 URL (신규계정 신청 폼 — 인증 메일 검증 흐름과 연관)
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.USER_SIGNUP;        // 작업 카테고리 (로그 적재용)

    private final UserSignupService userSignupService;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 사용자 신청 인증코드 메일로부터 사용자를 인증합니다.
     *
     * @param token 사용자 신청시 생성된 jwt 토큰
     * @return Vue SPA 인증 결과 화면 redirect 경로
     */
    @GetMapping(Url.API_AUTH_VERIFY)
    public String verifySecurityCode(
            final @PathVariable("token") String token
    ) {
        try {
            if (StringUtils.isEmpty(token)) throw new AuthenticationFailureException("auth.verify.token.empty");
            if (!jwtTokenProvider.validateToken(token)) throw new AuthenticationFailureException("auth.verify.token.expired");

            final String username = jwtTokenProvider.getUsernameFromToken(token);
            // 계정 승인 처리
            final boolean approved = userSignupService.cfByUsername(username).getRslt();
            if (!approved) throw new AlreadyAuthenticatedException("auth.verify.request.not-approvable");

            return "redirect:/vue-app/auth/verify-result?status=success";
        } catch (final Exception e) {
            final String rsltMsg = StringUtils.defaultIfBlank(MessageUtils.getExceptionMsg(e), "인증 링크를 다시 확인해주세요.");
            final String encodedMessage = UriUtils.encodeQueryParam(rsltMsg, StandardCharsets.UTF_8);

            return "redirect:/vue-app/auth/verify-result?status=failure&message=" + encodedMessage;
        }
    }
}
