package io.nicheblog.dreamdiary.adapter.snmp.controller;

import io.nicheblog.dreamdiary.adapter.snmp.model.SnmpApiParam;
import io.nicheblog.dreamdiary.adapter.snmp.util.SnmpUtils;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.extension.log.actvty.ActvtyCtgr;
import io.nicheblog.dreamdiary.extension.log.actvty.model.LogActvtyParam;
import io.nicheblog.dreamdiary.extension.log.actvty.aspect.LogActvtyRestControllerAspect;
import io.nicheblog.dreamdiary.global.intrfc.controller.impl.BaseControllerImpl;
import io.nicheblog.dreamdiary.global.model.AjaxResponse;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * SnmpApiController
 * <pre>
 *  Snmp 수동 송신 관련 API 컨트롤러.
 * </pre>
 *
 * @author nichefish
 * @see LogActvtyRestControllerAspect
 */
@RestController
@CrossOrigin(origins="*", allowedHeaders="*")   // CORS 에러 해결 위한 조치
@Log4j2
public class SnmpApiController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.ADMIN_PAGE;
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.ADMIN;      // 작업 카테고리 (로그 적재용)

    /**
     * API:: SNMP 내역을 송신한다.
     * 비로그인 사용자도 외부에서 접근 가능. (인증 없음)
     * 
     * @param snmpApiParam - SNMP API 파라미터 객체
     * @param logParam 로그 기록을 위한 파라미터 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @PostMapping(Url.URL_API_SNMP_SEND_AJAX)
    @ResponseBody
    public ResponseEntity<AjaxResponse> snmpSendAjax (
            final SnmpApiParam snmpApiParam,
            final LogActvtyParam logParam
        ) throws Exception {

        final AjaxResponse ajaxResponse = new AjaxResponse();

        SnmpUtils.sendSnmpMessage(snmpApiParam);

        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.getMessage(MessageUtils.RSLT_SUCCESS);

        // 응답 결과 세팅
        ajaxResponse.setAjaxResult(isSuccess, rsltMsg);
        // 로그 관련 세팅
        logParam.setResult(isSuccess, rsltMsg, actvtyCtgr);

        return ResponseEntity.ok(ajaxResponse);
    }

}