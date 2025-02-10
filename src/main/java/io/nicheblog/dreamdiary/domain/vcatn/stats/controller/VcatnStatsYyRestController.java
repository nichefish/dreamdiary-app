package io.nicheblog.dreamdiary.domain.vcatn.stats.controller;

import io.nicheblog.dreamdiary.domain.vcatn.stats.model.VcatnStatsTotalDto;
import io.nicheblog.dreamdiary.domain.vcatn.stats.service.VcatnStatsService;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.extension.log.actvty.ActvtyCtgr;
import io.nicheblog.dreamdiary.extension.log.actvty.model.LogActvtyParam;
import io.nicheblog.dreamdiary.extension.log.actvty.aspect.LogActvtyRestControllerAspect;
import io.nicheblog.dreamdiary.global.intrfc.controller.impl.BaseControllerImpl;
import io.nicheblog.dreamdiary.global.model.AjaxResponse;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * VcatnStatsYyRestController
 * <pre>
 *  휴가관리 > 년도별 휴가관리 API 컨트롤러.
 * </pre>
 *
 * @author nichefish
 * @see LogActvtyRestControllerAspect
 */
@RestController
@RequiredArgsConstructor
@Log4j2
public class VcatnStatsYyRestController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.VCATN_STATS_YY;
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.VCATN_STATS;      // 작업 카테고리 (로그 적재용)

    private final VcatnStatsService vcatnStatsService;

    /**
     * 휴가관리 > 년도별 휴가관리 > 업데이트 (Ajax)
     * (관리자MNGR만 접근 가능.)
     *
     * @param vcatnStatsTotal - 등록할 전체 휴가관리
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @PostMapping(Url.VCATN_STATS_YY_UPDT_AJAX)
    @Secured(Constant.ROLE_MNGR)
    @ResponseBody
    public ResponseEntity<AjaxResponse> vcatnStatsUpdtAjax(
            final VcatnStatsTotalDto vcatnStatsTotal,
            final LogActvtyParam logParam
    ) throws Exception {

        final AjaxResponse ajaxResponse = new AjaxResponse();

        final boolean isSuccess = vcatnStatsService.regStatsTotal(vcatnStatsTotal);
        final String rsltMsg = MessageUtils.getMessage(isSuccess ? MessageUtils.RSLT_SUCCESS : MessageUtils.RSLT_FAILURE);

        // 응답 결과 세팅
        ajaxResponse.setAjaxResult(isSuccess, rsltMsg);
        // 로그 관련 세팅
        logParam.setResult(isSuccess, rsltMsg);

        return ResponseEntity.ok(ajaxResponse);
    }

    /**
     * 휴가관리 > 년도별 휴가관리 > 목록 엑셀 다운로드
     * (관리자MNGR만 접근 가능.)
     */
    // @GetMapping(Url.VCATN_STATS_YY_XLSX_DOWNLOAD)
    // @Secured(Constant.ROLE_MNGR)
    // public void vcatnStatsXlsxDownload(
    //         final LogActvtyParam logParam,
    //         final @RequestParam("statsYy") String yyStr
    // ) throws Exception {
//
    //     final boolean isSuccess = false;
    //     final String rsltMsg = "";
    //     try {
    //         VcatnStatsYyDto statsYy = StringUtils.isNotEmpty(yyStr) ? vcatnStatsYyService.getVcatnYyDtDto(yyStr) : vcatnStatsYyService.getCurrVcatnYyDt();
    //         // 해당년도에 근무이력이 있는(중도퇴사 포함) 모든 직원(재직+프리랜서) 전원에 대하여 산정
    //         List<Object> statsObjList = vcatnStatsService.getVcatnStatsListXlsx(statsYy);
    //         xlsxUtils.listXlxsDownload(Constant.VCATN_STATS, statsObjList);
    //         isSuccess = true;
    //         rsltMsg = MessageUtils.getMessage(MessageUtils.RSLT_SUCCESS);
    //     } catch (final Exception e) {
    //         isSuccess = false;
    //         rsltMsg = MessageUtils.getExceptionMsg(e);
    //         logParam.setExceptionInfo(e);
    //         MessageUtils.alertMessage(rsltMsg, Url.VCATN_STATS_YY);
    //     } finally {
    //         // 로그 관련 세팅
    //         logParam.setResult(isSuccess, rsltMsg);
    //         publisher.publishEvent(new LogActvtyEvent(this, logParam));
    //     }
    // }
}
