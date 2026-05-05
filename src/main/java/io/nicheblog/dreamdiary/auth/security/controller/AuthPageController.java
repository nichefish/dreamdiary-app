package io.nicheblog.dreamdiary.auth.security.controller;

import io.nicheblog.dreamdiary.auth.jwt.provider.JwtTokenProvider;
import io.nicheblog.dreamdiary.auth.security.exception.AlreadyAuthenticatedException;
import io.nicheblog.dreamdiary.auth.security.exception.AuthenticationFailureException;
import io.nicheblog.dreamdiary.feature.user.reqst.service.UserReqstService;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.infrastructure.log.type.ActvtyCtgr;
import io.nicheblog.dreamdiary.infrastructure.web.controller.impl.BaseControllerImpl;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

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
    private final String baseUrl = Url.USER_REQST_REG_FORM;             // 기본 URL (신규계정 신청 폼 — 인증 메일 검증 흐름과 연관)
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.USER_REQST;        // 작업 카테고리 (로그 적재용)

    private final UserReqstService userReqstService;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 사용자 신청 인증코드 메일로부터 사용자를 인증합니다.
     *
     * @param token 사용자 신청시 생성된 jwt 토큰
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @GetMapping(Url.API_AUTH_VERIFY)
    public String verifySecurityCode(
            final @PathVariable("token") String token,
            final ModelMap model
    ) {

        String rsltMsg = null;
        try {
            if (StringUtils.isEmpty(token)) throw new AuthenticationFailureException("msg.auth.verify.token.empty");
            if (!jwtTokenProvider.validateToken(token)) throw new AuthenticationFailureException("msg.auth.verify.token.expired");
            
            final String username = jwtTokenProvider.getUsernameFromToken(token);
            // 계정 승인 처리
            final boolean approved = userReqstService.cfByUsername(username).getRslt();
            if (!approved) throw new AlreadyAuthenticatedException("msg.auth.verify.request.not-approvable");

            return "/view/auth/security/verify_success";
        } catch (final Exception e) {
            rsltMsg = MessageUtils.getExceptionMsg(e);
            model.addAttribute("errorMsg", rsltMsg);

            return "/view/auth/security/verify_failure";
        }
    }
}
