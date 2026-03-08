package io.nicheblog.dreamdiary.domain.jrnl.sumry.controller;

import io.nicheblog.dreamdiary.domain.jrnl.sumry.model.JrnlSumryReviewDto;
import io.nicheblog.dreamdiary.domain.jrnl.sumry.service.JrnlSumryReviewService;
import io.nicheblog.dreamdiary.extension.clsf.tag.handler.TagProcEventListener;
import io.nicheblog.dreamdiary.extension.log.actvty.ActvtyCtgr;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.intrfc.controller.impl.BaseControllerImpl;
import io.nicheblog.dreamdiary.global.model.AjaxResponse;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.validation.Valid;

/**
 * JrnlSumryReviewRestController
 * <pre>
 *  저널 결산 리뷰 API Controller.
 * </pre>
 *
 * @author nichefish
 */
@RestController
@RequiredArgsConstructor
public class JrnlSumryReviewRestController
        extends BaseControllerImpl {

    private final JrnlSumryReviewService jrnlSumryReviewService;

    @Getter
    private final String baseUrl = Url.JRNL_SUMRY_LIST;             // 기본 URL
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.JRNL;        // 작업 카테고리 (로그 적재용)

    /**
     * 저널 결산 상세 조회 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param key 식별자
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @GetMapping(value = {Url.JRNL_SUMRY_REVIEW})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> jrnlSumryDtlAjax(
            final @PathVariable("postNo") Integer key
    ) throws Exception {

        // 객체 조회 및 모델에 추가
        final JrnlSumryReviewDto retrievedDto = jrnlSumryReviewService.getDtlDto(key);
        final boolean isSuccess = (retrievedDto.getPostNo() != null);
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg).withObj(retrievedDto));
    }

    /**
     * 저널 결산 리뷰 등록/수정 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param jrnlSumryReview 등록/수정 처리할 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     * @see TagProcEventListener
     */
    @PostMapping(value = {Url.JRNL_SUMRY_REVIEWS, Url.JRNL_SUMRY_REVIEW})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> jrnlSumryReviewRegAjax(
            final @PathVariable(value = "postNo", required = false) Integer postNo,
            final @Valid JrnlSumryReviewDto jrnlSumryReview,
            final MultipartHttpServletRequest request
    ) throws Exception {

        final boolean isMdf = postNo != null;
        if (isMdf) jrnlSumryReview.setPostNo(postNo);
        final ServiceResponse result = isMdf ? jrnlSumryReviewService.modify(jrnlSumryReview, request) : jrnlSumryReviewService.regist(jrnlSumryReview, request);
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        return ResponseEntity.ok(AjaxResponse.fromResponseWithObj(result, rsltMsg));
    }

    /**
     * 저널 결산 리뷰 삭제 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param postNo 식별자
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     * @see TagProcEventListener
     */
    @DeleteMapping(value = {Url.JRNL_SUMRY_REVIEW})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> jrnlSumryReviewAjax(
            final @PathVariable("postNo") Integer postNo
    ) throws Exception {

        final ServiceResponse result = jrnlSumryReviewService.delete(postNo);
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        return ResponseEntity.ok(AjaxResponse.fromResponseWithObj(result, rsltMsg));
    }
}
