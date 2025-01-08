package io.nicheblog.dreamdiary.global._common._clsf.sectn.controller;

import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global._common._clsf.sectn.model.SectnDto;
import io.nicheblog.dreamdiary.global._common._clsf.sectn.model.SectnParam;
import io.nicheblog.dreamdiary.global._common._clsf.sectn.model.SectnSearchParam;
import io.nicheblog.dreamdiary.global._common._clsf.sectn.service.SectnService;
import io.nicheblog.dreamdiary.global._common.log.actvty.ActvtyCtgr;
import io.nicheblog.dreamdiary.global._common.log.actvty.model.LogActvtyParam;
import io.nicheblog.dreamdiary.global.aspect.log.LogActvtyRestControllerAspect;
import io.nicheblog.dreamdiary.global.intrfc.controller.impl.BaseControllerImpl;
import io.nicheblog.dreamdiary.global.model.AjaxResponse;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.global.util.cmm.CmmUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.validation.Valid;

/**
 * SectnRestController
 * <pre>
 *  단락 API 컨트롤러.
 * </pre>
 *
 * @see LogActvtyRestControllerAspect
 * @author nichefish
 */
@RestController
@RequiredArgsConstructor
@Log4j2
public class SectnRestController
        extends BaseControllerImpl {

    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.DEFAULT;      // 작업 카테고리 (로그 적재용)

    private final SectnService sectnService;

    /**
     * 단락 목록 조회 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param searchParam 검색 조건을 담은 파라미터 객체
     * @param logParam 로그 기록을 위한 파라미터 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @GetMapping(Url.SECTN_LIST_AJAX)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> sectnListAjax(
            @ModelAttribute("searchParam") SectnSearchParam searchParam,
            final LogActvtyParam logParam
    ) throws Exception {

        final AjaxResponse ajaxResponse = new AjaxResponse();

        final Sort sort = Sort.by(Sort.Direction.ASC, "state.sortOrdr");
        final PageRequest pageRequest = CmmUtils.Param.getPageRequest(searchParam, sort);
        final Page<SectnDto> sectnList = sectnService.getPageDto(searchParam, pageRequest);
        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.getMessage(MessageUtils.RSLT_SUCCESS);

        // 응답 결과 세팅
        ajaxResponse.setRsltList(sectnList.getContent());
        ajaxResponse.setAjaxResult(isSuccess, rsltMsg);
        // 로그 관련 세팅
        logParam.setResult(isSuccess, rsltMsg);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ajaxResponse);
    }

    /**
     * 단락 등록/수정 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param sectn 등록/수정 처리할 객체
     * @param logParam 로그 기록을 위한 파라미터 객체
     * @param request - Multipart 요청
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @PostMapping(value = {Url.SECTN_REG_AJAX, Url.SECTN_MDF_AJAX})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> sectnRegAjax(
            final @Valid SectnDto sectn,
            final MultipartHttpServletRequest request,
            final LogActvtyParam logParam
    ) throws Exception {

        final AjaxResponse ajaxResponse = new AjaxResponse();

        final Integer key = sectn.getKey();
        final boolean isReg = (key == null);
        final SectnDto result = isReg ? sectnService.regist(sectn, request) : sectnService.modify(sectn, request);

        final boolean isSuccess = (result.getPostNo() != null);;
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


    /**
     * 단락 상세 조회 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param key 식별자
     * @param logParam 로그 기록을 위한 파라미터 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @GetMapping(Url.SECTN_DTL_AJAX)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> sectnDtlAjax(
            final @RequestParam("postNo") Integer key,
            final SectnParam param,
            final LogActvtyParam logParam
    ) throws Exception {

        final AjaxResponse ajaxResponse = new AjaxResponse();

        final SectnDto rsDto = sectnService.getDtlDto(key);
        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.getMessage(MessageUtils.RSLT_SUCCESS);

        // 응답 결과 세팅
        ajaxResponse.setRsltObj(rsDto);
        ajaxResponse.setAjaxResult(isSuccess, rsltMsg);
        // 로그 관련 세팅
        logParam.setResult(isSuccess, rsltMsg);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ajaxResponse);
    }
    
    /**
     * 단락 삭제 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param postNo 식별자
     * @param logParam 로그 기록을 위한 파라미터 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @PostMapping(Url.SECTN_DEL_AJAX)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> sectnDelAjax(
            final @RequestParam("postNo") Integer postNo,
            final LogActvtyParam logParam
    ) throws Exception {

        final AjaxResponse ajaxResponse = new AjaxResponse();

        final boolean isSuccess = sectnService.delete(postNo);;
        final String rsltMsg = MessageUtils.getMessage(MessageUtils.RSLT_SUCCESS);

        // 응답 결과 세팅
        ajaxResponse.setAjaxResult(isSuccess, rsltMsg);
        // 로그 관련 세팅
        logParam.setResult(isSuccess, rsltMsg);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ajaxResponse);
    }

    /**
     * 관리자 > 메뉴 관리 > 정렬 순서 저장 (드래그앤드랍 결과 반영) (Ajax)
     *
     * @param sectnParam 키+정렬 순서 목록을 담은 파라미터
     * @param logParam 로그 기록을 위한 파라미터 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @PostMapping(Url.SECTN_SORT_ORDR_AJAX)
    @ResponseBody
    public ResponseEntity<AjaxResponse> sectnSortOrdrAjax(
            final @RequestBody SectnParam sectnParam,
            final LogActvtyParam logParam
    ) throws Exception {

        final AjaxResponse ajaxResponse = new AjaxResponse();

        final boolean isSuccess = sectnService.sortOrdr(sectnParam.getSortOrdr());
        String rsltMsg =  MessageUtils.getMessage(isSuccess ? MessageUtils.RSLT_SUCCESS : MessageUtils.RSLT_FAILURE);

        // 응답 결과 세팅
        ajaxResponse.setAjaxResult(isSuccess, rsltMsg);
        // logParam.setCn("key: " + menuNo);
        logParam.setResult(isSuccess, rsltMsg, actvtyCtgr);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ajaxResponse);
    }
}
