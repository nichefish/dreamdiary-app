package io.nicheblog.dreamdiary.auth.controller;

import io.jsonwebtoken.JwtException;
import io.nicheblog.dreamdiary.auth.model.AuthInfo;
import io.nicheblog.dreamdiary.auth.provider.JwtTokenProvider;
import io.nicheblog.dreamdiary.domain.user.info.model.UserPwChgParam;
import io.nicheblog.dreamdiary.domain.user.my.service.UserMyService;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global._common.log.actvty.ActvtyCtgr;
import io.nicheblog.dreamdiary.global._common.log.actvty.model.LogActvtyParam;
import io.nicheblog.dreamdiary.global.aspect.log.LogActvtyRestControllerAspect;
import io.nicheblog.dreamdiary.global.model.AjaxResponse;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.security.PermitAll;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

/**
 * AuthRestController
 * <pre>
 *  인증 관련 컨트롤러.
 * </pre>
 *
 * @author nichefish
 * @see LogActvtyRestControllerAspect
 */
@RestController
@RequiredArgsConstructor
@Log4j2
public class AuthRestController {

    private final UserMyService userMyService;
    private final JwtTokenProvider jwtTokenProvider;

    @Getter
    private final String baseUrl = Url.AUTH_LGN_FORM;
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.LGN;      // 작업 카테고리 (로그 적재용)

    /**
     * 인증 정보 조회
     * (JWT 토큰 검증 및 사용자 정보 추출)
     *
     * @param request HTTP 요청 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @GetMapping(Url.AUTH_INFO)
    public ResponseEntity<AjaxResponse> getAuthInfo(
            final HttpServletRequest request
    ) {

        final AjaxResponse ajaxResponse = new AjaxResponse();

        try {
            // JWT 검증 및 사용자 정보 추출
            final String jwtToken = jwtTokenProvider.resolveToken(request);
            final Authentication authentication = jwtTokenProvider.getDirectAuthentication(jwtToken);
            final AuthInfo authInfo = (AuthInfo) authentication.getPrincipal();

            ajaxResponse.setRsltObj(authInfo);
            ajaxResponse.setAjaxResult(true, MessageUtils.getMessage(MessageUtils.RSLT_SUCCESS));

            return ResponseEntity.ok(ajaxResponse);
        } catch (final JwtException jwtException) {
            ajaxResponse.setAjaxResult(false, MessageUtils.getExceptionMsg("JwtException"));
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ajaxResponse);
        } catch (final Exception e) {
            // 그 외 일반적인 예외 처리
            ajaxResponse.setAjaxResult(false, MessageUtils.getMessage(MessageUtils.RSLT_FAILURE));
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ajaxResponse);
        }
    }


    /**
     * 비밀번호 강제 변경 처리 (장기간 비밀번호 미변경시, 또는 비밀번호 리셋시)
     * (비로그인 사용자도 외부에서 접근 가능)
     *
     * @param userPwChgParam 비밀번호 변경을 위한 파라미터 객체 (유효성 검사 적용)
     * @param logParam 로그 기록을 위한 파라미터 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @PostMapping(Url.AUTH_LGN_PW_CHG_AJAX)
    @PermitAll
    @ResponseBody
    public ResponseEntity<AjaxResponse> lgnPwChgAjax(
            final @Valid UserPwChgParam userPwChgParam,
            final LogActvtyParam logParam
    ) throws Exception {

        final AjaxResponse ajaxResponse = new AjaxResponse();

        final boolean isSuccess = userMyService.lgnPwChg(userPwChgParam);
        final String rsltMsg = MessageUtils.getMessage(isSuccess ? MessageUtils.RSLT_SUCCESS : MessageUtils.RSLT_FAILURE);

        // 응답 결과 세팅
        ajaxResponse.setAjaxResult(isSuccess, rsltMsg);
        // 로그 관련 세팅
        logParam.setResult(isSuccess, rsltMsg);

        return ResponseEntity.ok(ajaxResponse);
    }

    /**
     * 세션 강제 만료 처리 (중복 로그인 '기존 아이디 끊기'에서 취소 선택시)
     *
     * @param request HTTP 요청 객체
     * @param logParam 로그 기록을 위한 파라미터 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @PostMapping(Url.AUTH_EXPIRE_SESSION_AJAX)
    @PermitAll
    @ResponseBody
    public ResponseEntity<AjaxResponse> expireSessionAjax(
            final HttpServletRequest request,
            final LogActvtyParam logParam
    ) {

        final AjaxResponse ajaxResponse = new AjaxResponse();

        // 세션 만료 처리
        final HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();

        // 응답 결과 세팅
        ajaxResponse.setAjaxResult(true, MessageUtils.RSLT_SUCCESS);
        // 로그 관련 세팅
        logParam.setResult(true, MessageUtils.RSLT_SUCCESS);

        return ResponseEntity.ok(ajaxResponse);
    }
}
