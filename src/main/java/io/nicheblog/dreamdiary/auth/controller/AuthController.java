package io.nicheblog.dreamdiary.auth.controller;

import io.jsonwebtoken.JwtException;
import io.nicheblog.dreamdiary.auth.model.AuthInfo;
import io.nicheblog.dreamdiary.auth.provider.JwtTokenProvider;
import io.nicheblog.dreamdiary.global.model.AjaxResponse;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * AuthController
 *
 * @author nichefish
 */
@RestController
@RequiredArgsConstructor
@Log4j2
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/api/auth/getAuthInfo.do")
    public ResponseEntity<AjaxResponse> getAuthInfo(
            final HttpServletRequest request
    ) {

        AjaxResponse ajaxResponse = new AjaxResponse();

        try {
            // JWT 검증 및 사용자 정보 추출
            String jwtToken = jwtTokenProvider.resolveToken(request);
            Authentication authentication = jwtTokenProvider.getDirectAuthentication(jwtToken);
            AuthInfo authInfo = (AuthInfo) authentication.getPrincipal();

            ajaxResponse.setRsltObj(authInfo);
            ajaxResponse.setAjaxResult(true, MessageUtils.getMessage(MessageUtils.RSLT_SUCCESS));

            return ResponseEntity.ok(ajaxResponse);
        } catch (JwtException jwtException) {
            ajaxResponse.setAjaxResult(false, MessageUtils.getMessage(MessageUtils.INVALID_TOKEN));
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ajaxResponse);
        } catch (Exception e) {
            // 그 외 일반적인 예외 처리
            ajaxResponse.setAjaxResult(false, MessageUtils.getMessage(MessageUtils.RSLT_FAILURE));
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ajaxResponse);
        }
    }
}
