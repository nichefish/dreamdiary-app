package io.nicheblog.dreamdiary.feature.clsf.comment.controller;

import io.nicheblog.dreamdiary.feature.clsf.comment.model.CommentDto;
import io.nicheblog.dreamdiary.feature.clsf.comment.model.CommentSearchParam;
import io.nicheblog.dreamdiary.feature.clsf.comment.service.CommentService;
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
 * CommentRestController
 * <pre>
 *  댓글 API 컨트롤러.
 * </pre>
 *
 * @author nichefish
 */
@RestController
@RequiredArgsConstructor
@Log4j2
public class CommentRestController
        extends BaseControllerImpl {

    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.DEFAULT;      // 작업 카테고리 (로그 적재용)

    private final CommentService commentService;

    /**
     * 댓글 목록 조회 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     * 
     * @param searchParam 검색 조건을 담은 파라미터 객체
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @GetMapping(Url.COMMENTS)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> commentListAjax(
            final @ModelAttribute("searchParam") CommentSearchParam searchParam
    ) throws Exception {

        final Sort sort = Sort.by(Sort.Direction.ASC, "regDt");
        final PageRequest pageRequest = ParamUtils.getPageRequest(searchParam, sort);
        final Page<CommentDto> commentList = commentService.getPageDto(searchParam, pageRequest);
        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg).withList(commentList.getContent()));
    }

    /**
     * 댓글 등록/수정 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     * 
     * @param comment 등록/수정 처리할 객체
     * @param request - Multipart 요청
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @PostMapping(value = {Url.COMMENTS, Url.COMMENT})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> commentRegAjax(
            final @Valid CommentDto comment,
            final MultipartHttpServletRequest request
    ) throws Exception {

        final boolean isReg = (comment.getKey() == null);
        final ServiceResponse result = isReg ? commentService.regist(comment, request) : commentService.modify(comment, request);
        final boolean isSuccess = result.getRslt();
        final String rsltMsg = isSuccess ? MessageUtils.RSLT_SUCCESS : MessageUtils.RSLT_FAILURE;

        return ResponseEntity.ok(AjaxResponse.fromResponseWithObj(result, rsltMsg));
    }

    /**
     * 댓글 상세 조회 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param key 식별자
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @GetMapping(Url.COMMENT)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> commentDtlAjax(
            final @RequestParam("postNo") Integer key
    ) throws Exception {

        final CommentDto rsDto = commentService.getDtlDto(key);
        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg).withObj(rsDto));
    }

    /**
     * 댓글 삭제 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param postNo 식별자
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @DeleteMapping(Url.COMMENT)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> commentDelAjax(
            final @PathVariable("postNo") Integer postNo
    ) throws Exception {

        final ServiceResponse result = commentService.delete(postNo);
        final String rsltMsg = MessageUtils.RSLT_SUCCESS;

        return ResponseEntity.ok(AjaxResponse.fromResponseWithObj(result, rsltMsg));
    }
}
