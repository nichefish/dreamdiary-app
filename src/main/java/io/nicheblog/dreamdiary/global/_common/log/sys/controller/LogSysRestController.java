package io.nicheblog.dreamdiary.global._common.log.sys.controller;

import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global._common.log.actvty.ActvtyCtgr;
import io.nicheblog.dreamdiary.global._common.log.actvty.model.LogActvtyParam;
import io.nicheblog.dreamdiary.global._common.log.sys.model.LogSysDto;
import io.nicheblog.dreamdiary.global._common.log.sys.service.LogSysService;
import io.nicheblog.dreamdiary.global.intrfc.controller.impl.BaseControllerImpl;
import io.nicheblog.dreamdiary.global.model.AjaxResponse;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * LogSysRestController
 * <pre>
 *  로그 관리 > 시스템 로그 관리 API 컨트롤러.
 * </pre>
 *
 * @author nichefish
 */
@RestController
@RequiredArgsConstructor
@Log4j2
public class LogSysRestController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.LOG_SYS_LIST;             // 기본 URL
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.LOG_SYS;        // 작업 카테고리 (로그 적재용)

    private final LogSysService logSysService;

    /**
     * 시스템 로그 상세 조회 (Ajax)
     * (관리자MNGR만 접근 가능.)
     *
     * @param key 식별자
     * @param logParam 로그 기록을 위한 파라미터 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @GetMapping(Url.LOG_SYS_DTL_AJAX)
    @Secured({Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> logActvtyDtlAjax(
            final @RequestParam("logSysNo") Integer key,
            final LogActvtyParam logParam
    ) throws Exception {

        final AjaxResponse ajaxResponse = new AjaxResponse();

        final LogSysDto.DTL rsDto = logSysService.getDtlDto(key);
        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.getMessage(MessageUtils.RSLT_SUCCESS);

        // 응답 결과 세팅
        ajaxResponse.setRsltObj(rsDto);
        ajaxResponse.setAjaxResult(isSuccess, rsltMsg);
        // 로그 관련 세팅
        logParam.setResult(isSuccess, rsltMsg, actvtyCtgr);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ajaxResponse);
    }
}
