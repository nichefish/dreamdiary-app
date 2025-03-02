package io.nicheblog.dreamdiary.domain.vcatn.papr.controller;

import io.nicheblog.dreamdiary.domain.vcatn.papr.model.VcatnPaprDto;
import io.nicheblog.dreamdiary.domain.vcatn.papr.service.VcatnPaprService;
import io.nicheblog.dreamdiary.extension.clsf.viewer.handler.ViewerEventListener;
import io.nicheblog.dreamdiary.extension.log.actvty.ActvtyCtgr;
import io.nicheblog.dreamdiary.extension.log.actvty.aspect.LogActvtyRestControllerAspect;
import io.nicheblog.dreamdiary.extension.log.actvty.model.LogActvtyParam;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.intrfc.controller.impl.BaseControllerImpl;
import io.nicheblog.dreamdiary.global.model.AjaxResponse;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.validation.Valid;

/**
 * VcatnPaprRestController
 * <pre>
 *  휴가계획서 API 컨트롤러.
 * </pre>
 *
 * @author nichefish
 * @see LogActvtyRestControllerAspect
 */
@RestController
@RequiredArgsConstructor
@Log4j2
public class VcatnPaprRestController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.VCATN_PAPR_LIST;
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.VCATN_PAPR;      // 작업 카테고리 (로그 적재용)

    private final VcatnPaprService vcatnPaprService;

    /**
     * 일정  > 휴가 계획서 > 휴가 계획서 등록/수정 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param vcatnPapr 등록/수정 처리할 객체
     * @param logParam 로그 기록을 위한 파라미터 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     * @throws Exception 처리 중 발생할 수 있는 예외
     * @see ViewerEventListener
     */
    @PostMapping(value = {Url.VCATN_PAPR_REG_AJAX, Url.VCATN_PAPR_MDF_AJAX})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> vcatnPaprRegAjax(
            final @Valid VcatnPaprDto.DTL vcatnPapr,
            final LogActvtyParam logParam,
            final MultipartHttpServletRequest request
    ) throws Exception {

        final boolean isReg = vcatnPapr.getKey() == null;
        final ServiceResponse result = isReg ? vcatnPaprService.regist(vcatnPapr, request) : vcatnPaprService.modify(vcatnPapr, request);
        final boolean isSuccess = result.getRslt();
        final String rsltMsg = isSuccess ? MessageUtils.RSLT_SUCCESS : MessageUtils.RSLT_FAILURE;

        // 로그 관련 세팅
        logParam.setResult(isSuccess, rsltMsg);

        return ResponseEntity.ok(AjaxResponse.fromResponseWithObj(result, rsltMsg));
    }

    /**
     * 일정  > 휴가 계획서 > 휴가 계획서 확인 처리 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param postNo 식별자
     * @param logParam 로그 기록을 위한 파라미터 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @PostMapping(Url.VCATN_PAPR_CF_AJAX)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> vcatnPaprCfAjax(
            final @RequestParam("postNo") Integer postNo,
            final LogActvtyParam logParam
    ) throws Exception {

        final ServiceResponse result = vcatnPaprService.cf(postNo);
        final boolean isSuccess = result.getRslt();
        final String rsltMsg = isSuccess ? MessageUtils.RSLT_SUCCESS : MessageUtils.RSLT_FAILURE;

        // 로그 관련 세팅
        logParam.setResult(isSuccess, rsltMsg);

        return ResponseEntity.ok(AjaxResponse.fromResponse(result, rsltMsg));
    }

    /**
     * 일정  > 휴가 계획서 > 상세 조회 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param key 식별자
     * @param logParam 로그 기록을 위한 파라미터 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     * @throws Exception 처리 중 발생할 수 있는 예외
     * @see ViewerEventListener
     */
    @GetMapping(Url.VCATN_PAPR_DTL_AJAX)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> vcatnPaprDtlAjax(
            final @RequestParam("postNo") Integer key,
            final LogActvtyParam logParam
    ) throws Exception {

        final VcatnPaprDto rsDto = vcatnPaprService.viewDtlPage(key);
        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        // 로그 관련 세팅
        logParam.setResult(isSuccess, rsltMsg);

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg).withObj(rsDto));
    }

    /**
     * 일정 > 휴가계획서 > 휴가계획서 삭제 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param postNo 식별자
     * @param logParam 로그 기록을 위한 파라미터 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @PostMapping(Url.VCATN_PAPR_DEL_AJAX)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> vcatnPaprDelAjax(
            final @RequestParam("postNo") Integer postNo,
            final LogActvtyParam logParam
    ) throws Exception {

        final ServiceResponse result = vcatnPaprService.delete(postNo);
        final boolean isSuccess = result.getRslt();
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        // 로그 관련 세팅
        logParam.setResult(isSuccess, rsltMsg);

        return ResponseEntity.ok(AjaxResponse.fromResponseWithObj(result, rsltMsg));
    }
}
