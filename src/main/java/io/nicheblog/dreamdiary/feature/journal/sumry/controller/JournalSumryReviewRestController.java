package io.nicheblog.dreamdiary.feature.journal.sumry.controller;

import io.nicheblog.dreamdiary.feature.journal.sumry.model.JournalSumryReviewDto;
import io.nicheblog.dreamdiary.feature.journal.sumry.service.JournalSumryReviewService;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.infrastructure.log.actvty.ActvtyCtgr;
import io.nicheblog.dreamdiary.infrastructure.web.controller.impl.BaseControllerImpl;
import io.nicheblog.dreamdiary.infrastructure.web.model.AjaxResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.validation.Valid;

/**
 * JournalSumryReviewRestController
 * <pre>
 *  저널 결산 리뷰 API Controller.
 * </pre>
 *
 * @author nichefish
 */
@RestController
@RequiredArgsConstructor
public class JournalSumryReviewRestController
        extends BaseControllerImpl {

    private final JournalSumryReviewService journalSumryReviewService;

    @Getter
    private final String baseUrl = Url.JOURNAL_SUMRY_LIST;             // 기본 URL
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.JOURNAL;        // 작업 카테고리 (로그 적재용)

    /**
     * 저널 결산 상세 조회 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param id 식별자
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @GetMapping(value = {Url.JOURNAL_SUMRY_REVIEW})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> journalSumryDtlAjax(
            final @PathVariable("id") Integer id
    ) throws Exception {

        // 객체 조회 및 모델에 추가
        final JournalSumryReviewDto retrievedDto = journalSumryReviewService.getDtlDto(id);
        final boolean isSuccess = (retrievedDto.getId() != null);
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg).withObj(retrievedDto));
    }

    /**
     * 저널 결산 리뷰 등록/수정 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param journalSumryReview 등록/수정 처리할 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @PostMapping(value = {Url.JOURNAL_SUMRY_REVIEWS, Url.JOURNAL_SUMRY_REVIEW})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> journalSumryReviewRegAjax(
            final @PathVariable(value = "id", required = false) Integer id,
            final @Valid JournalSumryReviewDto journalSumryReview,
            final MultipartHttpServletRequest request
    ) throws Exception {

        final boolean isMdf = id != null;
        if (isMdf) journalSumryReview.setId(id);
        final ServiceResponse result = isMdf ? journalSumryReviewService.modify(journalSumryReview, request) : journalSumryReviewService.regist(journalSumryReview, request);
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        return ResponseEntity.ok(AjaxResponse.fromResponseWithObj(result, rsltMsg));
    }

    /**
     * 저널 결산 리뷰 삭제 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param id 식별자
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @DeleteMapping(value = {Url.JOURNAL_SUMRY_REVIEW})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> journalSumryReviewAjax(
            final @PathVariable("id") Integer id
    ) throws Exception {

        final ServiceResponse result = journalSumryReviewService.delete(id);
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        return ResponseEntity.ok(AjaxResponse.fromResponseWithObj(result, rsltMsg));
    }
}

