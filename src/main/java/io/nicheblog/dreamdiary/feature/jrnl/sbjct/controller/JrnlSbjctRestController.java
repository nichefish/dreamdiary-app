package io.nicheblog.dreamdiary.feature.jrnl.sbjct.controller;

import io.nicheblog.dreamdiary.feature.clsf.viewer.handler.ViewerEventListener;
import io.nicheblog.dreamdiary.feature.jrnl.sbjct.model.JrnlSbjctDto;
import io.nicheblog.dreamdiary.feature.jrnl.sbjct.service.JrnlSbjctService;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.infrastructure.log.actvty.ActvtyCtgr;
import io.nicheblog.dreamdiary.infrastructure.web.controller.impl.BaseControllerImpl;
import io.nicheblog.dreamdiary.infrastructure.web.model.AjaxResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.validation.Valid;

/**
 * JrnlSbjctRestController
 * <pre>
 *  저널 주제 API 컨트롤러.
 * </pre>
 *
 * @author nichefish
 */
@RestController
@RequiredArgsConstructor
@Log4j2
public class JrnlSbjctRestController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.JRNL_SBJCT_LIST;             // 기본 URL
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.NOTICE;      // 작업 카테고리 (로그 적재용)

    private final JrnlSbjctService jrnlSbjctService;

    /**
     * 저널 주제 등록/수정 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param jrnlSbjct 등록/수정 처리할 객체
     * @param request - Multipart 요청
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @PostMapping(value = {Url.JRNL_SBJCT_REG_AJAX, Url.JRNL_SBJCT_MDF_AJAX})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> jrnlSbjctRegAjax(
            final @Valid JrnlSbjctDto jrnlSbjct,
            final MultipartHttpServletRequest request
    ) throws Exception {

        final boolean isReg = (jrnlSbjct.getKey() == null);
        final ServiceResponse result = isReg ? jrnlSbjctService.regist(jrnlSbjct, request) : jrnlSbjctService.modify(jrnlSbjct, request);
        final boolean isSuccess = result.getRslt();
        final String rsltMsg = isSuccess ? MessageUtils.RSLT_SUCCESS : MessageUtils.RSLT_FAILURE;

        return ResponseEntity.ok(AjaxResponse.fromResponseWithObj(result, rsltMsg));
    }

    /**
     * 저널 주제 상세 조회 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param key 식별자
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     * @see ViewerEventListener
     */
    @GetMapping(Url.JRNL_SBJCT_DTL_AJAX)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> jrnlSbjctDtlAjax(
            final @RequestParam("postNo") Integer key
    ) throws Exception {

        final JrnlSbjctDto retrievedDto = jrnlSbjctService.viewDtlPage(key);
        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg).withObj(retrievedDto));
    }

    /**
     * 저널 주제 삭제 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param postNo 식별자
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @PostMapping(Url.JRNL_SBJCT_DEL_AJAX)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> jrnlSbjctDelAjax(
            final @RequestParam("postNo") Integer postNo
    ) throws Exception {

        final ServiceResponse result = jrnlSbjctService.delete(postNo);
        final boolean isSuccess = result.getRslt();
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        return ResponseEntity.ok(AjaxResponse.fromResponseWithObj(result, rsltMsg));
    }
}
