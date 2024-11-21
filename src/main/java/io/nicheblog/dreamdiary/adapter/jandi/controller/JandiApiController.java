package io.nicheblog.dreamdiary.adapter.jandi.controller;

import io.nicheblog.dreamdiary.adapter.jandi.model.JandiApiRespnsDto;
import io.nicheblog.dreamdiary.adapter.jandi.model.JandiParam;
import io.nicheblog.dreamdiary.adapter.jandi.service.JandiApiService;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global._common.log.actvty.ActvtyCtgr;
import io.nicheblog.dreamdiary.global._common.log.actvty.model.LogActvtyParam;
import io.nicheblog.dreamdiary.global.aspect.log.LogActvtyRestControllerAspect;
import io.nicheblog.dreamdiary.global.intrfc.controller.impl.BaseControllerImpl;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * JandiApiController
 * <pre>
 *  JANDI:: (incoming/outgoing) webhook API 컨트롤러.
 * </pre>
 *
 * @see LogActvtyRestControllerAspect
 * @author nichefish
 */
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")   // CORS 에러 해결 위한 조치
@RequiredArgsConstructor
@Log4j2
@Tag(name = "잔디 메신저 API", description = "잔디 메신저 API입니다.")
public class JandiApiController
        extends BaseControllerImpl {

    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.JANDI;      // 작업 카테고리 (로그 적재용)

    private final JandiApiService jandiApiService;

    /**
     * JANDI :: 잔디 메신저로 웹훅 메세지 전송
     *
     * @param logParam 로그 기록을 위한 파라미터 객체
     * @param jandiParam 잔디 파라미터 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @Operation(
            summary = "잔디 메신저로 웹훅 메세지 전송",
            description = "잔디 메신저로 웹훅 메세지를 전송한다."
    )
    @PostMapping(Url.API_JANDI_SND_MSG)
    public ResponseEntity<JandiApiRespnsDto> sendMsg(
            final LogActvtyParam logParam,
            final JandiParam jandiParam
    ) throws Exception {

        final JandiApiRespnsDto apiResponse = new JandiApiRespnsDto();
        log.info("requestUrl: {}, jandiParam: {}", request.getRequestURL(), jandiParam.toString());

        final boolean isSuccess = jandiApiService.sendMsg(jandiParam);
        final String rsltMsg = MessageUtils.getMessage(isSuccess ? MessageUtils.RSLT_SUCCESS : MessageUtils.RSLT_FAILURE);

        // 응답 결과 세팅
        apiResponse.setApiResult(isSuccess, rsltMsg);
        // 로그 관련 세팅
        logParam.setResult(isSuccess, rsltMsg);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(apiResponse);
    }

    /**
     * JANDI :: 잔디 메신저로부터 웹훅 메세지 수신
     */
    // @Operation(
    //         summary = "잔디 메신저로부터 웹훅 메세지 수신",
    //         description = "잔디 메신저로부터 웹훅 메세지를 수신한다."
    // )
    // @PostMapping(Url.API_JANDI_RCV_MSG)
    // public ResponseEntity<JandiApiRespnsDto> receiveMsg(
    //         final @RequestBody @Nullable JandiApiRcvMsgDto rcvMsg,
    //         final LogActvtyParam logParam
    // ) {
//
    //     JandiApiRespnsDto apiResponse = new JandiApiRespnsDto();
//
    //     log.info("requestUrl: {}", request.getRequestURL());
//
    //     final boolean isSuccess = false;
    //     final String rsltMsg = "";
    //     try {
    //         isSuccess = jandiApiService.receiveMsg(rcvMsg);
    //         rsltMsg = MessageUtils.getMessage(isSuccess ? MessageUtils.RSLT_SUCCESS : MessageUtils.RSLT_FAILURE);
    //     } catch (Exception e) {
    //         isSuccess = false;
    //         rsltMsg = MessageUtils.getExceptionMsg(e);
    //         logParam.setExceptionInfo(e);
    //     } finally {
    //         apiResponse.setApiResult(isSuccess, rsltMsg);
    //         // 로그 관련 세팅
    //         logParam.setResult(isSuccess, rsltMsg);
    //         publisher.publishEvent(new LogActvtyEvent(this, logParam));
    //     }
    //
    //     return ResponseEntity
    //        .status(HttpStatus.OK)
    //        .body(apiResponse);
    // }
}
