package io.nicheblog.dreamdiary.feature.admin.auth.policy.controller;

import io.nicheblog.dreamdiary.feature.admin.auth.policy.model.AuthPolicyDto;
import io.nicheblog.dreamdiary.feature.admin.auth.policy.service.AuthPolicyService;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.infrastructure.log.actvty.ActvtyCtgr;
import io.nicheblog.dreamdiary.infrastructure.web.controller.impl.BaseControllerImpl;
import io.nicheblog.dreamdiary.infrastructure.web.model.AjaxResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * AuthPolicyRestController
 * <pre>
 *  인증 정책 관리 API 컨트롤러.
 * </pre>
 *
 * @author nichefish
 */
@RestController
@RequiredArgsConstructor
@Log4j2
public class AuthPolicyRestController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.AUTH_POLICY_FORM;             // 기본 URL
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.AUTH_POLICY;        // 작업 카테고리 (로그 적재용)

    private final AuthPolicyService authPolicyService;

    /**
     * 인증 정책 등록/수정 (Ajax)
     * (관리자MNGR만 접근 가능.)
     *
     * @param authPolicy 등록/수정 처리할 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @PostMapping(Url.AUTH_POLICY_REG_AJAX)
    @Secured(Constant.ROLE_MNGR)
    @ResponseBody
    public ResponseEntity<AjaxResponse> authPolicyRegAjax(
            final @Valid AuthPolicyDto authPolicy
    ) throws Exception {

        final ServiceResponse result = authPolicyService.regist(authPolicy);
        final boolean isSuccess = result.getRslt();
        final String rsltMsg = isSuccess ? MessageUtils.RSLT_SUCCESS : MessageUtils.RSLT_FAILURE;

        return ResponseEntity.ok(AjaxResponse.fromResponseWithObj(result, rsltMsg));
    }
}
