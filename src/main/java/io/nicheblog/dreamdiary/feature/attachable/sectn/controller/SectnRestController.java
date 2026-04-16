package io.nicheblog.dreamdiary.feature.attachable.sectn.controller;

import io.nicheblog.dreamdiary.feature.attachable.sectn.model.SectnDto;
import io.nicheblog.dreamdiary.feature.attachable.sectn.model.SectnSearchParam;
import io.nicheblog.dreamdiary.feature.attachable.sectn.service.SectnService;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.infrastructure.log.actvty.ActvtyCtgr;
import io.nicheblog.dreamdiary.infrastructure.web.controller.impl.BaseControllerImpl;
import io.nicheblog.dreamdiary.infrastructure.web.model.AjaxResponse;
import io.nicheblog.dreamdiary.infrastructure.web.util.ParamUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @GetMapping(Url.SECTNS)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> sectnListAjax(
            @ModelAttribute("searchParam") SectnSearchParam searchParam
    ) throws Exception {

        final Sort sort = Sort.by(Sort.Direction.ASC, "sortOrder");
        final PageRequest pageRequest = ParamUtils.getPageRequest(searchParam, sort);
        final Page<SectnDto> sectnList = sectnService.getPageDto(searchParam, pageRequest);
        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg).withList(sectnList.getContent()));
    }

    /**
     * 단락 등록/수정 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param sectn 등록/수정 처리할 객체
     * @param request - Multipart 요청
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @PostMapping(value = {Url.SECTNS, Url.SECTN})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> sectnRegAjax(
            final @PathVariable(value = "id", required = false) Integer id,
            final @Valid SectnDto sectn,
            final MultipartHttpServletRequest request
    ) throws Exception {

        final boolean isMdf = (id != null);
        if (isMdf) sectn.setId(id);
        final ServiceResponse result = isMdf ? sectnService.modify(sectn, request) : sectnService.regist(sectn, request);
        final boolean isSuccess = result.getRslt();
        final String rsltMsg = isSuccess ? MessageUtils.RSLT_SUCCESS : MessageUtils.RSLT_FAILURE;

        return ResponseEntity.ok(AjaxResponse.fromResponseWithObj(result, rsltMsg));
    }

    /**
     * 단락 상세 조회 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param id 식별자
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @GetMapping(Url.SECTN)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> sectnDtlAjax(
            final @PathVariable("id") Integer id
    ) throws Exception {

        final SectnDto rsDto = sectnService.getDtlDto(id);
        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg).withObj(rsDto));
    }
    
    /**
     * 단락 삭제 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param id 식별자
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @DeleteMapping(Url.SECTN)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> sectnDelAjax(
            final @PathVariable("id") Integer id
    ) throws Exception {

        final ServiceResponse result = sectnService.delete(id);;
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        return ResponseEntity.ok(AjaxResponse.fromResponseWithObj(result, rsltMsg));
    }

    /**
     * 관리자 > 메뉴 관리 > 정렬 순서 저장 (드래그앤드랍 결과 반영) (Ajax)
     *
     * @param sectnParam 키+정렬 순서 목록을 담은 파라미터
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
/*    @PostMapping(Url.SECTN_SORT_ORDR)
    @ResponseBody
    public ResponseEntity<AjaxResponse> sectnSortOrdrAjax(
            final @RequestBody SectnParam sectnParam
    ) throws Exception {

        final ServiceResponse result = sectnService.idx(sectnParam.getSortOrdr());
        final boolean isSuccess = result.getRslt();
        String rsltMsg = isSuccess ? MessageUtils.RSLT_SUCCESS : MessageUtils.RSLT_FAILURE;

        return ResponseEntity.ok(AjaxResponse.fromResponseWithObj(result, rsltMsg));
    }
    */
}
