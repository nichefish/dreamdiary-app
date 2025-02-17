package io.nicheblog.dreamdiary.domain.jrnl.sbjct.controller;

import io.nicheblog.dreamdiary.domain.jrnl.sbjct.model.JrnlSbjctDto;
import io.nicheblog.dreamdiary.domain.jrnl.sbjct.service.JrnlSbjctService;
import io.nicheblog.dreamdiary.extension.clsf.ContentType;
import io.nicheblog.dreamdiary.extension.clsf.managt.event.ManagtrAddEvent;
import io.nicheblog.dreamdiary.extension.clsf.managt.handler.ManagtrEventListener;
import io.nicheblog.dreamdiary.extension.clsf.tag.event.TagProcEvent;
import io.nicheblog.dreamdiary.extension.clsf.tag.handler.TagProcEventListener;
import io.nicheblog.dreamdiary.extension.clsf.viewer.event.ViewerAddEvent;
import io.nicheblog.dreamdiary.extension.clsf.viewer.handler.ViewerEventListener;
import io.nicheblog.dreamdiary.extension.log.actvty.ActvtyCtgr;
import io.nicheblog.dreamdiary.extension.log.actvty.aspect.LogActvtyRestControllerAspect;
import io.nicheblog.dreamdiary.extension.log.actvty.model.LogActvtyParam;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.intrfc.controller.impl.BaseControllerImpl;
import io.nicheblog.dreamdiary.global.intrfc.entity.BaseClsfKey;
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
 * JrnlSbjctRestController
 * <pre>
 *  저널 주제 API 컨트롤러.
 * </pre>
 *
 * @author nichefish
 * @see LogActvtyRestControllerAspect
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
     * @param logParam 로그 기록을 위한 파라미터 객체
     * @param request - Multipart 요청
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     * @throws Exception 처리 중 발생할 수 있는 예외
     * @see TagProcEventListener ,ManagtrEventListener
     */
    @PostMapping(value = {Url.JRNL_SBJCT_REG_AJAX, Url.JRNL_SBJCT_MDF_AJAX})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> jrnlSbjctRegAjax(
            final @Valid JrnlSbjctDto.DTL jrnlSbjct,
            final LogActvtyParam logParam,
            final MultipartHttpServletRequest request
    ) throws Exception {

        final boolean isReg = (jrnlSbjct.getKey() == null);
        final ServiceResponse result = isReg ? jrnlSbjctService.regist(jrnlSbjct, request) : jrnlSbjctService.modify(jrnlSbjct, request);
        final boolean isSuccess = result.getRslt();
        final String rsltMsg = isSuccess ? MessageUtils.RSLT_SUCCESS : MessageUtils.RSLT_FAILURE;

        // TODO: AOP로 분리하기
        if (isSuccess) {
            final JrnlSbjctDto rsltObj = (JrnlSbjctDto) result.getRsltObj();
            // 조치자 추가 :: 메인 로직과 분리
            publisher.publishAsyncEvent(new ManagtrAddEvent(this, rsltObj.getClsfKey()));
            // 태그 처리 :: 메인 로직과 분리
            publisher.publishAsyncEventAndWait(new TagProcEvent(this, rsltObj.getClsfKey(), jrnlSbjct.tag));
            // 잔디 메세지 발송 :: 메인 로직과 분리
            // if ("Y".equals(jandiYn)) {
            //     String jandiRsltMsg = notifyService.notifyJrnlSbjctReg(trgetTopic, result, logParam);
            //     rsltMsg = rsltMsg + "\n" + jandiRsltMsg;
            // }
        }

        // 로그 관련 세팅
        logParam.setResult(isSuccess, rsltMsg);

        return ResponseEntity.ok(AjaxResponse.fromResponseWithObj(result, rsltMsg));
    }

    /**
     * 저널 주제 상세 조회 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param key 식별자
     * @param logParam 로그 기록을 위한 파라미터 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     * @throws Exception 처리 중 발생할 수 있는 예외
     * @see ViewerEventListener
     */
    @GetMapping(Url.JRNL_SBJCT_DTL_AJAX)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> jrnlSbjctDtlAjax(
            final @RequestParam("postNo") Integer key,
            final LogActvtyParam logParam
    ) throws Exception {

        final JrnlSbjctDto retrievedDto = jrnlSbjctService.getDtlDto(key);
        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        // 조회수 카운트 추가
        // TODO: AOP로 분리
        jrnlSbjctService.hitCntUp(key);
        // 열람자 추가 :: 메인 로직과 분리
        // TODO: AOP로 분리
        publisher.publishAsyncEvent(new ViewerAddEvent(this, retrievedDto.getClsfKey()));

        // 로그 관련 세팅
        logParam.setResult(isSuccess, rsltMsg);

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg).withObj(retrievedDto));
    }

    /**
     * 저널 주제 삭제 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param postNo 식별자
     * @param logParam 로그 기록을 위한 파라미터 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     * @throws Exception 처리 중 발생할 수 있는 예외
     * @see TagProcEventListener
     */
    @PostMapping(Url.JRNL_SBJCT_DEL_AJAX)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> jrnlSbjctDelAjax(
            final @RequestParam("postNo") Integer postNo,
            final LogActvtyParam logParam
    ) throws Exception {

        final ServiceResponse result = jrnlSbjctService.delete(postNo);
        final boolean isSuccess = result.getRslt();
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        // TODO: AOP로 분리
        if (isSuccess) {
            // 태그 처리 :: 메인 로직과 분리
            publisher.publishAsyncEventAndWait(new TagProcEvent(this, new BaseClsfKey(postNo, ContentType.JRNL_SBJCT)));
        }

        // 로그 관련 세팅
        logParam.setResult(isSuccess, rsltMsg);

        return ResponseEntity.ok(AjaxResponse.fromResponseWithObj(result, rsltMsg));
    }
}