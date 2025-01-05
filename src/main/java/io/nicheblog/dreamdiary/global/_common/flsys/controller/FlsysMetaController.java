package io.nicheblog.dreamdiary.global._common.flsys.controller;

import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global._common.flsys.model.FlsysMetaDto;
import io.nicheblog.dreamdiary.global._common.flsys.service.FlsysMetaService;
import io.nicheblog.dreamdiary.global._common.log.actvty.ActvtyCtgr;
import io.nicheblog.dreamdiary.global._common.log.actvty.event.LogActvtyEvent;
import io.nicheblog.dreamdiary.global._common.log.actvty.model.LogActvtyParam;
import io.nicheblog.dreamdiary.global.intrfc.controller.impl.BaseControllerImpl;
import io.nicheblog.dreamdiary.global.intrfc.entity.BaseClsfEntity;
import io.nicheblog.dreamdiary.global.intrfc.entity.BaseClsfKey;
import io.nicheblog.dreamdiary.global.model.AjaxResponse;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * FlsysMetaController
 * <pre>
 *  파일시스템 메타 관리 컨트롤러.
 * </pre>
 * TODO: 보완 예정
 *
 * @author nichefish
 */
@Controller
@RequiredArgsConstructor
@Log4j2
public class FlsysMetaController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.FLSYS_HOME;
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.FLSYS;        // 작업 카테고리 (로그 적재용)

    private final FlsysMetaService flsysMetaService;

    /**
     * 파일시스템 메타 정보 등록/수정 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param flsysMeta 등록/수정 처리할 객체
     * @param logParam 로그 기록을 위한 파라미터 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @PostMapping(value = {Url.FLSYS_META_REG_AJAX, Url.FLSYS_META_MDF_AJAX})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> flsysMetaRegAjax(
            final @RequestBody @Valid FlsysMetaDto flsysMeta,
            final LogActvtyParam logParam
    ) {

        final AjaxResponse ajaxResponse = new AjaxResponse();

        boolean isSuccess = false;
        String rsltMsg = "";
        try {
            final BaseClsfKey key = flsysMeta.getClsfKey();
            final boolean isReg = key.getPostNo() == null;
            final FlsysMetaDto result = isReg ? flsysMetaService.regist(flsysMeta) : flsysMetaService.modify(flsysMeta);
            ajaxResponse.setRsltObj(result);

            isSuccess = (result.getPostNo() != null);
            rsltMsg = MessageUtils.getMessage(isSuccess ? MessageUtils.RSLT_SUCCESS : MessageUtils.RSLT_FAILURE);
        } catch (Exception e) {
            isSuccess = false;
            rsltMsg = MessageUtils.getExceptionMsg(e);
            logParam.setExceptionInfo(e);
        } finally {
            ajaxResponse.setAjaxResult(isSuccess, rsltMsg);
            // 로그 관련 세팅
            logParam.setCn(flsysMeta.toString());
            logParam.setResult(isSuccess, rsltMsg, actvtyCtgr);
            publisher.publishEvent(new LogActvtyEvent(this, logParam));
        }

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ajaxResponse);
    }

    /**
     * 파일시스템 메타 정보 상세 조회 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param key 식별자
     * @param logParam 로그 기록을 위한 파라미터 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @GetMapping(Url.FLSYS_META_DTL_AJAX)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> flsysMetaDtlAjax(
            final @RequestParam("postNo") Integer key,
            final LogActvtyParam logParam
    ) {

        final AjaxResponse ajaxResponse = new AjaxResponse();

        boolean isSuccess = false;
        String rsltMsg = "";
        try {
            // 정보 조회 및 응답에 세팅
            final FlsysMetaDto rsDto = flsysMetaService.getDtlDto(key);
            ajaxResponse.setRsltObj(rsDto);

            isSuccess = true;
            rsltMsg = MessageUtils.getMessage(MessageUtils.RSLT_SUCCESS);
        } catch (Exception e) {
            isSuccess = false;
            rsltMsg = MessageUtils.getExceptionMsg(e);
            logParam.setExceptionInfo(e);
        } finally {
            ajaxResponse.setAjaxResult(isSuccess, rsltMsg);
            // 로그 관련 세팅
            logParam.setCn("key: " + key.toString());
            logParam.setResult(isSuccess, rsltMsg, actvtyCtgr);
            publisher.publishEvent(new LogActvtyEvent(this, logParam));
        }

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ajaxResponse);
    }

    /**
     * 파일시스템 메타 정보 삭제 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param postNo 식별자
     * @param logParam 로그 기록을 위한 파라미터 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @PostMapping(Url.FLSYS_META_DEL_AJAX)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> flsysMetaDelAjax(
            final @RequestBody Integer postNo,
            final LogActvtyParam logParam

        ) {

        final AjaxResponse ajaxResponse = new AjaxResponse();

        boolean isSuccess = false;
        String rsltMsg = "";
        try {
            // 게시판 정보 조회
            isSuccess = flsysMetaService.delete(postNo);
            rsltMsg = MessageUtils.getMessage(isSuccess ? MessageUtils.RSLT_SUCCESS : MessageUtils.RSLT_FAILURE);
        } catch (Exception e) {
            isSuccess = false;
            rsltMsg = MessageUtils.getExceptionMsg(e);
            logParam.setExceptionInfo(e);
        } finally {
            ajaxResponse.setAjaxResult(isSuccess, rsltMsg);
            // 로그 관련 세팅
            logParam.setCn("key: " + postNo.toString());
            logParam.setResult(isSuccess, rsltMsg, actvtyCtgr);
            publisher.publishEvent(new LogActvtyEvent(this, logParam));
        }

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ajaxResponse);
    }
}
