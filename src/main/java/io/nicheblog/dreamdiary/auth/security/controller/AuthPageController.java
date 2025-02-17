package io.nicheblog.dreamdiary.auth.security.controller;

import io.nicheblog.dreamdiary.auth.jwt.provider.JwtTokenProvider;
import io.nicheblog.dreamdiary.auth.security.exception.AlreadyAuthenticatedException;
import io.nicheblog.dreamdiary.auth.security.exception.AuthenticationFailureException;
import io.nicheblog.dreamdiary.domain.user.info.entity.UserEntity;
import io.nicheblog.dreamdiary.domain.user.info.service.UserService;
import io.nicheblog.dreamdiary.domain.user.reqst.service.UserReqstService;
import io.nicheblog.dreamdiary.extension.log.actvty.ActvtyCtgr;
import io.nicheblog.dreamdiary.extension.log.actvty.model.LogActvtyParam;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.intrfc.controller.impl.BaseControllerImpl;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class AuthPageController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.LGN_POLICY_FORM;             // 기본 URL
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.LGN_POLICY;        // 작업 카테고리 (로그 적재용)

    private final UserService userService;
    private final UserReqstService userReqstService;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 사용자 신청 인증코드 메일로부터 사용자를 인증합니다.
     *
     * @param token 사용자 신청시 생성된 jwt 토큰
     * @param logParam 로그 기록을 위한 파라미터 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @GetMapping(Url.AUTH_VERIFY + "/{token}")
    public String verifySecurityCode(
            final @PathVariable("token") String token,
            final LogActvtyParam logParam,
            final ModelMap model
    ) throws Exception {

        boolean isSuccess = false;
        String rsltMsg = null;

        try {
            if (StringUtils.isEmpty(token)) throw new AuthenticationFailureException("인증에 필요한 정보가 없습니다.");
            if (!jwtTokenProvider.validateToken(token)) throw new AuthenticationFailureException("이미 만료된 인증 코드입니다.");
            
            final String userId = jwtTokenProvider.getUsernameFromToken(token);
            final UserEntity user = userService.getDtlEntity(userId);
            final boolean isAlreadyCf = "Y".equals(user.acntStus.getCfYn());
            // 이미 가입된 계정이면 미처리.
            if (isAlreadyCf) throw new AlreadyAuthenticatedException("이미 인증된 계정입니다.");
            // 계정 승인 처리
            userReqstService.cf(user.getUserNo());

            isSuccess = true;
            rsltMsg = MessageUtils.RSLT_SUCCESS;

            return "/view/auth/security/verify_success";
        } catch (final Exception e) {
            rsltMsg = e.getMessage();
            model.addAttribute("errorMsg", rsltMsg);

            return "/view/auth/security/verify_failure";
        } finally {
            // 로그 관련 세팅
            logParam.setResult(isSuccess, rsltMsg);
        }
    }
}
