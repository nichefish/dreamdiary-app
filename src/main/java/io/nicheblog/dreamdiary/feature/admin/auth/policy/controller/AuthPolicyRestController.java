package io.nicheblog.dreamdiary.feature.admin.auth.policy.controller;

import io.nicheblog.dreamdiary.feature.admin.auth.policy.model.AuthPolicyDto;
import io.nicheblog.dreamdiary.feature.admin.auth.policy.service.AuthPolicyService;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.infrastructure.log.type.ActvtyCtgr;
import io.nicheblog.dreamdiary.infrastructure.web.controller.impl.BaseControllerImpl;
import io.nicheblog.dreamdiary.infrastructure.web.model.AjaxResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * AuthPolicyRestController
 * <pre>
 *  인증 정책 관리 API 컨트롤러.
 *  싱글톤 리소스: 경로에 id 없이 GET(조회), PUT(갱신) 만 사용한다.
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
    private final String baseUrl = Url.AUTH_POLICY;             // 기본 URL (API)
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.AUTH_POLICY;        // 작업 카테고리 (로그 적재용)

    private final AuthPolicyService authPolicyService;

    /**
     * 인증 정책 단건 조회 (Ajax)
     * (관리자MNGR만 접근 가능.)
     *
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @GetMapping(Url.AUTH_POLICY)
    @Secured(Constant.ROLE_MNGR)
    @ResponseBody
    public ResponseEntity<AjaxResponse> authPolicyGet(
    ) throws Exception {

        final AuthPolicyDto dto = authPolicyService.getDtlDto();
        final boolean isSuccess = dto != null && dto.getId() != null;
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg).withObj(dto));
    }

    /**
     * 인증 정책 갱신 (Ajax, PUT)
     * (관리자MNGR만 접근 가능.)
     *
     * @param authPolicy 갱신할 객체 (본문 JSON)
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @PutMapping(Url.AUTH_POLICY)
    @Secured(Constant.ROLE_MNGR)
    @ResponseBody
    public ResponseEntity<AjaxResponse> authPolicyPut(
            final @Valid @RequestBody AuthPolicyDto authPolicy
    ) throws Exception {

        final ServiceResponse result = authPolicyService.regist(authPolicy);
        final boolean isSuccess = result.getRslt();
        final String rsltMsg = isSuccess ? MessageUtils.RSLT_SUCCESS : MessageUtils.RSLT_FAILURE;

        return ResponseEntity.ok(AjaxResponse.fromResponseWithObj(result, rsltMsg));
    }
}
