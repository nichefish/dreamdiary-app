package io.nicheblog.dreamdiary.domain.jrnl.day.controller;

import io.nicheblog.dreamdiary.domain.jrnl.day.model.JrnlDayDto;
import io.nicheblog.dreamdiary.domain.jrnl.day.model.JrnlDaySearchParam;
import io.nicheblog.dreamdiary.domain.jrnl.day.service.JrnlDayService;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global._common._clsf.ContentType;
import io.nicheblog.dreamdiary.global._common._clsf.tag.event.TagProcEvent;
import io.nicheblog.dreamdiary.global._common.log.actvty.ActvtyCtgr;
import io.nicheblog.dreamdiary.global._common.log.actvty.model.LogActvtyParam;
import io.nicheblog.dreamdiary.global.aspect.log.LogActvtyRestControllerAspect;
import io.nicheblog.dreamdiary.global.intrfc.controller.impl.BaseControllerImpl;
import io.nicheblog.dreamdiary.global.intrfc.entity.BaseClsfKey;
import io.nicheblog.dreamdiary.global.model.AjaxResponse;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.swagger.v3.oas.annotations.Operation;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.validation.Valid;
import java.util.List;

/**
 * JrnlDayApiController
 * <pre>
 *  저널 일자 API Controller.
 * </pre>
 *
 * @see LogActvtyRestControllerAspect
 * @author nichefish
 */
@RestController
@RequiredArgsConstructor
public class JrnlDayApiController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.JRNL_DAY_PAGE;             // 기본 URL
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.JRNL;        // 작업 카테고리 (로그 적재용)

    private final JrnlDayService jrnlDayService;

    /**
     * 저널 일자 목록 조회 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param searchParam 검색 조건을 담은 파라미터 객체
     * @param logParam 로그 기록을 위한 파라미터 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @GetMapping(value = {Url.JRNL_DAY_LIST_AJAX})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> jrnlDayListAjax(
            final JrnlDaySearchParam searchParam,
            final LogActvtyParam logParam
    ) throws Exception {

        final AjaxResponse ajaxResponse = new AjaxResponse();

        final List<JrnlDayDto> jrnlDayList = jrnlDayService.getListDtoWithCache(searchParam);

        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.getMessage(MessageUtils.RSLT_SUCCESS);

        // 응답 결과 세팅
        ajaxResponse.setRsltList(jrnlDayList);
        ajaxResponse.setAjaxResult(isSuccess, rsltMsg);
        // 로그 관련 세팅
        logParam.setResult(isSuccess, rsltMsg);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ajaxResponse);
    }

    /**
     * 저널 일자 등록/수정 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param jrnlDay 등록/수정 처리할 객체
     * @param logParam 로그 기록을 위한 파라미터 객체
     * @param request - Multipart 요청
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @Operation(
            summary = "저널 일자 등록/수정",
            description = "저널 일자 정보를 등록/수정한다."
    )
    @PostMapping(value = {Url.JRNL_DAY_REG_AJAX, Url.JRNL_DAY_MDF_AJAX})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> jrnlDayRegAjax(
            final @Valid JrnlDayDto jrnlDay,
            final LogActvtyParam logParam,
            final MultipartHttpServletRequest request
    ) throws Exception {

        final AjaxResponse ajaxResponse = new AjaxResponse();

        final Integer key = jrnlDay.getKey();
        boolean isReg = key == null;
        if (isReg) {
            boolean isDup = jrnlDayService.dupChck(jrnlDay);
            if (isDup) {
                jrnlDay.setPostNo(jrnlDayService.getDupKey(jrnlDay));
                isReg = false;      // 등록 대신 기존 데이터 수정
            }
        }
        final JrnlDayDto result = isReg ? jrnlDayService.regist(jrnlDay, request) : jrnlDayService.modify(jrnlDay, request);

        final boolean isSuccess = (result.getPostNo() != null);
        final String rsltMsg = MessageUtils.getMessage(isSuccess ? MessageUtils.RSLT_SUCCESS : MessageUtils.RSLT_FAILURE);

        // TODO: AOP로 분리
        if (isSuccess) {
            // 태그 처리 :: 메인 로직과 분리
            publisher.publishEvent(new TagProcEvent(this, result.getClsfKey(), jrnlDay.tag));
        }

        // 응답 결과 세팅
        ajaxResponse.setRsltObj(result);
        ajaxResponse.setAjaxResult(isSuccess, rsltMsg);
        // 로그 관련 세팅
        logParam.setResult(isSuccess, rsltMsg);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ajaxResponse);
    }

    /**
     * 저널 일자 상세 조회 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param key 식별자
     * @param logParam 로그 기록을 위한 파라미터 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @GetMapping(value = {Url.JRNL_DAY_DTL_AJAX})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> jrnlDayDtlAjax(
            final @RequestParam("postNo") Integer key,
            final LogActvtyParam logParam
    ) throws Exception {

        final AjaxResponse ajaxResponse = new AjaxResponse();

        final JrnlDayDto retrievedDto = jrnlDayService.getDtlDtoWithCache(key);

        final boolean isSuccess = (retrievedDto.getPostNo() != null);
        final String rsltMsg = MessageUtils.getMessage(MessageUtils.RSLT_SUCCESS);

        // 응답 결과 세팅
        ajaxResponse.setRsltObj(retrievedDto);
        ajaxResponse.setAjaxResult(isSuccess, rsltMsg);
        // 로그 관련 세팅
        logParam.setResult(isSuccess, rsltMsg);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ajaxResponse);
    }

    /**
     * 저널 일자 삭제 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param postNo 식별자
     * @param logParam 로그 기록을 위한 파라미터 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @PostMapping(value = {Url.JRNL_DAY_DEL_AJAX})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> jrnlDayDelAjax(
            final @RequestBody Integer postNo,
            final LogActvtyParam logParam
    ) throws Exception {

        final AjaxResponse ajaxResponse = new AjaxResponse();

        final boolean isSuccess = jrnlDayService.delete(postNo);
        final String rsltMsg = MessageUtils.getMessage(MessageUtils.RSLT_SUCCESS);

        // TODO: AOP로 분리
        if (isSuccess) {
            // 태그 처리 :: 메인 로직과 분리
            publisher.publishEvent(new TagProcEvent(this, new BaseClsfKey(postNo, ContentType.JRNL_DAY)));
        }

        // 응답 결과 세팅
        ajaxResponse.setAjaxResult(isSuccess, rsltMsg);
        // 로그 관련 세팅
        logParam.setResult(isSuccess, rsltMsg);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ajaxResponse);
    }
}
