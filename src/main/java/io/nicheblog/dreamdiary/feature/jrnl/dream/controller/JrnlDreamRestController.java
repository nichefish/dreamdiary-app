package io.nicheblog.dreamdiary.feature.jrnl.dream.controller;

import io.nicheblog.dreamdiary.feature.jrnl.dream.model.JrnlDreamDto;
import io.nicheblog.dreamdiary.feature.jrnl.dream.model.JrnlDreamPostDto;
import io.nicheblog.dreamdiary.feature.jrnl.dream.model.JrnlDreamSearchParam;
import io.nicheblog.dreamdiary.feature.jrnl.dream.service.JrnlDreamService;
import io.nicheblog.dreamdiary.feature.clsf.tag.handler.TagProcEventListener;
import io.nicheblog.dreamdiary.infrastructure.log.actvty.ActvtyCtgr;
import io.nicheblog.dreamdiary.infrastructure.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.intrfc.controller.impl.BaseControllerImpl;
import io.nicheblog.dreamdiary.infrastructure.web.model.AjaxResponse;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.swagger.v3.oas.annotations.Operation;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.validation.Valid;
import java.util.List;

/**
 * JrnlDreamRestController
 * <pre>
 *  저널 꿈 API Controller.
 * </pre>
 *
 * @author nichefish
 */
@RestController
@RequiredArgsConstructor
public class JrnlDreamRestController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.JRNL_DAY_MONTHLY;             // 기본 URL
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.JRNL;        // 작업 카테고리 (로그 적재용)

    private final JrnlDreamService jrnlDreamService;

    /**
     * 저널 꿈 목록 조회 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param searchParam 검색 조건을 담은 파라미터 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @GetMapping(value = {Url.JRNL_DREAMS})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> jrnlDreamListAjax(
            JrnlDreamSearchParam searchParam
    ) throws Exception {

        if (searchParam.isEmpty()) throw new IllegalArgumentException("검색 조건 필요");

        final List<JrnlDreamDto> jrnlDreamList = jrnlDreamService.getListDtoWithCache(searchParam);
        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg).withList(jrnlDreamList));
    }

    /**
     * 저널 꿈 등록/수정 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param jrnlDream 등록/수정 처리할 객체
     * @param request - Multipart 요청
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     * @see TagProcEventListener
     */
    @Operation(
            summary = "저널 꿈 등록/수정",
            description = "저널 꿈 정보를 등록/수정한다."
    )
    @PostMapping(value = {Url.JRNL_DREAMS, Url.JRNL_DREAM})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> jrnlDreamRegAjax(
            final @PathVariable(value = "postNo", required = false) Integer postNo,
            final @Valid JrnlDreamPostDto jrnlDream,
            final MultipartHttpServletRequest request
    ) throws Exception {

        final boolean isMdf = postNo != null;
        if (isMdf) jrnlDream.setPostNo(postNo);

        final ServiceResponse result = isMdf ? jrnlDreamService.modify(jrnlDream, request) : jrnlDreamService.regist(jrnlDream, request);
        final boolean isSuccess = result.getRslt();
        final String rsltMsg = isSuccess ? MessageUtils.RSLT_SUCCESS : MessageUtils.RSLT_FAILURE;

        return ResponseEntity.ok(AjaxResponse.fromResponseWithObj(result, rsltMsg));
    }

    /**
     * 저널 꿈 상세 조회 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param key 식별자
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @GetMapping(value = {Url.JRNL_DREAM})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> jrnlDreamDtlAjax(
            final @PathVariable("postNo") Integer key
    ) throws Exception {

        final JrnlDreamDto retrievedDto = jrnlDreamService.getDtlDtoWithCache(key);
        final boolean isSuccess = (retrievedDto.getPostNo() != null);
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg).withObj(retrievedDto));
    }

    /**
     * 저널 꿈 삭제 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param postNo 식별자
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     * @see TagProcEventListener
     */
    @DeleteMapping(value = {Url.JRNL_DREAM})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> jrnlDreamDelAjax(
            final @PathVariable("postNo") Integer postNo
    ) throws Exception {

        final ServiceResponse result = jrnlDreamService.delete(postNo);
        final boolean isSuccess = result.getRslt();
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        return ResponseEntity.ok(AjaxResponse.fromResponseWithObj(result, rsltMsg));
    }
}
