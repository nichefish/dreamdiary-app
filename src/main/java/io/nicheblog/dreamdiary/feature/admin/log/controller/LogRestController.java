package io.nicheblog.dreamdiary.feature.admin.log.controller;

import io.nicheblog.dreamdiary.feature.admin.log.model.LogQueryDto;
import io.nicheblog.dreamdiary.feature.admin.log.service.LogQueryService;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.infrastructure.log.model.LogParam;
import io.nicheblog.dreamdiary.infrastructure.log.type.ActvtyCtgr;
import io.nicheblog.dreamdiary.infrastructure.web.controller.impl.BaseControllerImpl;
import io.nicheblog.dreamdiary.infrastructure.web.model.AjaxResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * LogRestController
 * <pre>
 *  로그 관리 > 활동 로그 관리 API 컨트롤러.
 * </pre>
 *
 * @author nichefish
 */
@RestController
@RequiredArgsConstructor
@Log4j2
public class LogRestController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.LOG_LIST;             // 기본 URL
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.LOG;        // 작업 카테고리 (로그 적재용)

    private final LogQueryService logQueryService;

    /**
     * 활동 로그 상세 조회 (Ajax)
     * (관리자MNGR만 접근 가능.)
     *
     * @param key 식별자
     * @param logParam 로그 기록을 위한 파라미터 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @GetMapping(Url.LOG_DTL_AJAX)
    @Secured(Constant.ROLE_MNGR)
    @ResponseBody
    public ResponseEntity<AjaxResponse> logDtlAjax(
            final @RequestParam("logId") Integer key,
            final LogParam logParam
    ) throws Exception {

        final LogQueryDto rsDto = logQueryService.getDtlDto(key);
        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        // 로그 관련 세팅
        logParam.setResult(isSuccess, rsltMsg, actvtyCtgr);

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg).withObj(rsDto));
    }
}
