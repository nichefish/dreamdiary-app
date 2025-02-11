package io.nicheblog.dreamdiary.domain.vcatn.papr.controller;

import io.nicheblog.dreamdiary.domain.vcatn.papr.model.VcatnSchdulDto;
import io.nicheblog.dreamdiary.domain.vcatn.papr.service.VcatnSchdulService;
import io.nicheblog.dreamdiary.extension.log.actvty.ActvtyCtgr;
import io.nicheblog.dreamdiary.extension.log.actvty.aspect.LogActvtyRestControllerAspect;
import io.nicheblog.dreamdiary.extension.log.actvty.model.LogActvtyParam;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.intrfc.controller.impl.BaseControllerImpl;
import io.nicheblog.dreamdiary.global.model.AjaxResponse;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * VcatnSchdulRestController
 * <pre>
 *  휴가관리 > 휴가사용일자 API 컨트롤러.
 * </pre>
 *
 * @author nichefish
 * @see LogActvtyRestControllerAspect
 */
@RestController
@RequiredArgsConstructor
@Log4j2
public class VcatnSchdulRestController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.VCATN_SCHDUL_LIST;
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.VCATN_SCHDUL;      // 작업 카테고리 (로그 적재용)

    private final VcatnSchdulService vcatnSchdulService;

    /**
     * 휴가관리 > 휴가사용일자 > 휴가사용일자 등록/수정 (Ajax)
     * (관리자MNGR만 접근 가능.)
     *
     * @param vcatnSchdul 등록/수정할 객체
     * @param logParam 로그 기록을 위한 파라미터 객체
     * @return {@link String} -- 뷰 이름을 나타내는 문자열
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @PostMapping(value = {Url.VCATN_SCHDUL_REG_AJAX, Url.VCATN_SCHDUL_MDF_AJAX})
    @Secured(Constant.ROLE_MNGR)
    @ResponseBody
    public ResponseEntity<AjaxResponse> vcatnSchdulRegAjax(
            final @Valid VcatnSchdulDto vcatnSchdul,
            final LogActvtyParam logParam
    ) throws Exception {

        final AjaxResponse ajaxResponse = new AjaxResponse();

        final Integer key = vcatnSchdul.getKey();
        final boolean isReg = (key == null);
        final VcatnSchdulDto result = isReg ? vcatnSchdulService.regist(vcatnSchdul) : vcatnSchdulService.modify(vcatnSchdul);

        final boolean isSuccess = (result.getVcatnSchdulNo() != null);
        final String rsltMsg = MessageUtils.getMessage(isSuccess ? MessageUtils.RSLT_SUCCESS : MessageUtils.RSLT_FAILURE);

        // 응답 결과 세팅
        ajaxResponse.setRsltObj(result);
        ajaxResponse.setAjaxResult(isSuccess, rsltMsg);
        // 로그 관련 세팅
        logParam.setResult(isSuccess, rsltMsg);

        return ResponseEntity.ok(ajaxResponse);
    }

    /**
     * 휴가관리 > 휴가사용일자 > 휴가사용일자 단일 조회 (Ajax)
     * (관리자MNGR만 접근 가능.)
     *
     * @param key 식별자
     * @param logParam 로그 기록을 위한 파라미터 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @GetMapping(value = Url.VCATN_SCHDUL_DTL_AJAX)
    @Secured(Constant.ROLE_MNGR)
    @ResponseBody
    public ResponseEntity<AjaxResponse> vcatnSchdulDtlAjax(
            final @RequestParam("vcatnSchdulNo") Integer key,
            final LogActvtyParam logParam
    ) throws Exception {

        final AjaxResponse ajaxResponse = new AjaxResponse();

        final VcatnSchdulDto rsDto = vcatnSchdulService.getDtlDto(key);

        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.getMessage(MessageUtils.RSLT_SUCCESS);

        // 응답 결과 세팅
        ajaxResponse.setRsltObj(rsDto);
        ajaxResponse.setAjaxResult(isSuccess, rsltMsg);
        // 로그 관련 세팅
        logParam.setResult(isSuccess, rsltMsg);

        return ResponseEntity.ok(ajaxResponse);
    }

    /**
     * 휴가관리 > 휴가사용일자 > 휴가사용일자 삭제 (Ajax)
     * (관리자MNGR만 접근 가능.)
     *
     * @param vcatnSchdulNo 식별자
     * @param logParam 로그 기록을 위한 파라미터 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @PostMapping(Url.VCATN_SCHDUL_DEL_AJAX)
    @Secured(Constant.ROLE_MNGR)
    @ResponseBody
    public ResponseEntity<AjaxResponse> vcatnSchdulDelAjax(
            final @RequestParam("vcatnSchdulNo") Integer vcatnSchdulNo,
            final LogActvtyParam logParam
    ) throws Exception {

        final AjaxResponse ajaxResponse = new AjaxResponse();

        final boolean isSuccess = vcatnSchdulService.delete(vcatnSchdulNo);;
        final String rsltMsg = MessageUtils.getMessage(MessageUtils.RSLT_SUCCESS);

        // 응답 결과 세팅
        ajaxResponse.setAjaxResult(isSuccess, rsltMsg);
        // 로그 관련 세팅
        logParam.setResult(isSuccess, rsltMsg);

        return ResponseEntity.ok(ajaxResponse);
    }
}