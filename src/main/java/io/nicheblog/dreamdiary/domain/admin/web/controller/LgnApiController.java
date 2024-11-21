package io.nicheblog.dreamdiary.domain.admin.web.controller;

import io.nicheblog.dreamdiary.domain.user.info.model.UserPwChgParam;
import io.nicheblog.dreamdiary.domain.user.my.service.UserMyService;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global._common.log.actvty.ActvtyCtgr;
import io.nicheblog.dreamdiary.global._common.log.actvty.model.LogActvtyParam;
import io.nicheblog.dreamdiary.global.aspect.log.LogActvtyRestControllerAspect;
import io.nicheblog.dreamdiary.global.intrfc.controller.impl.BaseControllerImpl;
import io.nicheblog.dreamdiary.global.model.AjaxResponse;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.security.PermitAll;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

/**
 * LgnApiController
 * <pre>
 *  로그인 API 컨트롤러.
 * </pre>
 *
 * @see LogActvtyRestControllerAspect
 * @author nichefish
 */
@RestController
@RequiredArgsConstructor
@Log4j2
public class LgnApiController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.AUTH_LGN_FORM;
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.LGN;      // 작업 카테고리 (로그 적재용)

    private final UserMyService userMyService;

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

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ajaxResponse);
    }

    /**
     * 세션 강제 만료 처리 (중복 로그인 '기존 아이디 끊기'에서 취소 선택시)
     *
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @PostMapping(Url.AUTH_EXPIRE_SESSION_AJAX)
    @PermitAll
    @ResponseBody
    public ResponseEntity<AjaxResponse> expireSessionAjax(
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

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ajaxResponse);
    }
}
