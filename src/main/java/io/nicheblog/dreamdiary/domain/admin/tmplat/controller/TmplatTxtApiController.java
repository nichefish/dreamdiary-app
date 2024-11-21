package io.nicheblog.dreamdiary.domain.admin.tmplat.controller;

import io.nicheblog.dreamdiary.domain.admin.tmplat.model.TmplatDefDto;
import io.nicheblog.dreamdiary.domain.admin.tmplat.service.TmplatDefService;
import io.nicheblog.dreamdiary.global.Constant;
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
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Nullable;
import javax.validation.Valid;

/**
 * TmplatTxtApiController
 * <pre>
 *  템플릿 문구 API 컨트롤러.
 * </pre>
 *
 * @see LogActvtyRestControllerAspect
 * @author nichefish
 */
@RestController
@RequiredArgsConstructor
@Log4j2
public class TmplatTxtApiController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.TMPLAT_DEF_LIST;             // 기본 URL
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.TMPLAT;        // 작업 카테고리 (로그 적재용)

    private final TmplatDefService tmplatDefService;

    /**
     * 템플릿 문구 등록/수정 (Ajax)
     * (관리자MNGR만 접근 가능.)
     *
     * @param tmplatDto 등록/수정 처리할 객체
     * @param key 식별자
     * @param logParam 로그 기록을 위한 파라미터 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @PostMapping(value = {Url.TMPLAT_TXT_REG_AJAX, Url.TMPLAT_TXT_MDF_AJAX})
    @Secured(Constant.ROLE_MNGR)
    @ResponseBody
    public ResponseEntity<AjaxResponse> tmplatTxtRegAjax(
            final @Valid TmplatDefDto tmplatDto,
            final @RequestParam("tmplatDefNo") @Nullable Integer key,
            final LogActvtyParam logParam
    ) throws Exception {

        final AjaxResponse ajaxResponse = new AjaxResponse();

        final boolean isReg = key == null;
        final TmplatDefDto result = isReg ? tmplatDefService.regist(tmplatDto) : tmplatDefService.modify(tmplatDto);

        final boolean isSuccess = (result.getTmplatDefNo() != null);
        final String rsltMsg = MessageUtils.getMessage(isSuccess ? MessageUtils.RSLT_SUCCESS : MessageUtils.RSLT_FAILURE);

        // 응답 결과 세팅
        ajaxResponse.setRsltObj(result);
        ajaxResponse.setAjaxResult(isSuccess, rsltMsg);
        // 로그 관련 세팅
        logParam.setResult(isSuccess, rsltMsg);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ajaxResponse);
    }
}
