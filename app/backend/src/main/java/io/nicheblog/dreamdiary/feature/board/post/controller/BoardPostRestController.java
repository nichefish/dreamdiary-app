package io.nicheblog.dreamdiary.feature.board.post.controller;

import io.nicheblog.dreamdiary.feature.attachable.viewer.handler.ViewerEventListener;
import io.nicheblog.dreamdiary.feature.board.post.model.BoardPostDto;
import io.nicheblog.dreamdiary.feature.board.post.model.BoardPostSearchParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import io.nicheblog.dreamdiary.feature.board.group.model.BoardDto;
import io.nicheblog.dreamdiary.feature.board.group.service.BoardService;
import io.nicheblog.dreamdiary.feature.board.post.service.BoardPostService;
import io.nicheblog.dreamdiary.feature.admin.code.model.CodeItemDto;
import io.nicheblog.dreamdiary.feature.admin.code.service.CodeItemService;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.model.ServiceResponse;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.infrastructure.log.type.ActvtyCtgr;
import io.nicheblog.dreamdiary.infrastructure.web.controller.impl.BaseControllerImpl;
import io.nicheblog.dreamdiary.infrastructure.web.model.AjaxResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.validation.Valid;
import java.util.Collections;
import java.util.List;

/**
 * BoardPostRestController
 * <pre>
 *  게시판 게시물 API 컨트롤러.
 *  화면단에선 board, 어플리케이션 단에선 contentType으로 사용
 * </pre>
 *
 * @author nichefish
 */
@RestController
@RequiredArgsConstructor
@Log4j2
public class BoardPostRestController
        extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.BOARD_POST_LIST;             // 기본 URL
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.BOARD_POST;        // 작업 카테고리 (로그 적재용)

    private final BoardPostService boardPostService;
    private final BoardService boardService;
    private final CodeItemService codeItemService;

    /**
     * 게시판별 분류 코드 목록 조회 (Ajax).
     * 관리 화면 API(코드 관리)를 우회하지 않고 사용자 화면이 읽을 수 있는 전용 읽기 계약을 제공한다.
     * <p>
     * 분류 코드 그룹은 게시판마다 다르므로({@code board.category_group_code}) boardKey 로 받아
     * 해당 게시판의 그룹 코드를 찾은 뒤 코드 목록을 반환한다.
     * 게시판에 분류 그룹이 지정돼 있지 않으면 빈 목록을 반환하며, 화면은 분류 select 를 숨긴다.
     *
     * @param boardKey 게시판 키
     * @return {@link ResponseEntity} -- 현재 locale 이 적용된 분류 코드 목록 (없으면 빈 목록)
     */
    @GetMapping(Url.BOARD_CATEGORIES)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> boardCategoryListAjax(
            final @PathVariable("boardKey") String boardKey
    ) throws Exception {

        final BoardDto board = boardService.getDtlDtoByBoardKey(boardKey);
        final String groupCode = board != null ? StringUtils.trimToNull(board.getCategoryGroupCode()) : null;
        final List<CodeItemDto> categoryList = groupCode != null
                ? codeItemService.getCdDtoListByGroupCode(groupCode)
                : null;
        if (groupCode == null) {
            log.debug("[boardCategoryListAjax] 분류 그룹 미지정 게시판. boardKey={}", boardKey);
        }

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(true, MessageUtils.getMessage("common.result.success"))
                .withList(categoryList != null ? categoryList : Collections.emptyList()));
    }

    /**
     * 게시판 게시물 목록 조회 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     * 추가(board-1): Vue SPA 목록 조회용 REST 엔드포인트.
     *
     * @param searchParam 검색 조건 (contentType, categoryCode, searchKeyword 등)
     * @param page 페이지 번호 (0-based, 기본값: 0)
     * @param size 페이지 크기 (기본값: 10)
     * @return {@link ResponseEntity} -- Spring Page 직렬화 (content, totalElements, totalPages, number)
     */
    @GetMapping(Url.BOARD_POSTS)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> boardPostListAjax(
            @ModelAttribute final BoardPostSearchParam searchParam,
            @RequestParam(defaultValue = "0") final int page,
            @RequestParam(defaultValue = "10") final int size
    ) throws Exception {

        final PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        final Page<BoardPostDto> pageResult = boardPostService.getPageDto(searchParam, pageRequest);
        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.getMessage("common.result.success");

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg).withObj(pageResult));
    }

    /**
     * 게시판 게시물 등록/수정 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param boardPost 등록/수정 처리할 게시물
     * @param request - Multipart 요청
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @PostMapping(value = {Url.BOARD_POSTS, Url.BOARD_POST})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> boardPostRegistAjax(
            final @Valid BoardPostDto boardPost,
            final MultipartHttpServletRequest request
    ) throws Exception {

        final boolean isReg = (boardPost.getKey() == null);
        final ServiceResponse result = isReg ? boardPostService.regist(boardPost, request) : boardPostService.modify(boardPost, request);
        final boolean isSuccess = result.getRslt();
        final String rsltMsg = isSuccess ? MessageUtils.getMessage("common.result.success") : MessageUtils.getMessage("common.result.failure");

        return ResponseEntity.ok(AjaxResponse.fromResponseWithObj(result, rsltMsg));
    }

    /**
     * 게시판 게시물 상세 조회 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param id 식별자
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     * @see ViewerEventListener
     */
    @GetMapping(Url.BOARD_POST)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> boardPostDetailAjax(
            final @RequestParam("id") Integer id
    ) throws Exception {

        final BoardPostDto retrievedDto = boardPostService.viewDtlPage(id);
        final boolean isSuccess = true;
        final String rsltMsg = MessageUtils.getMessage("common.result.success");

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(isSuccess, rsltMsg).withObj(retrievedDto));
    }

    /**
     * 게시판 게시물 삭제 (Ajax)
     * (사용자USER, 관리자MNGR만 접근 가능.)
     *
     * @param id 복합키 식별자
     * @return {@link ResponseEntity} -- 처리 결과와 메시지
     */
    @DeleteMapping(Url.BOARD_POST)
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> boardPostDelAjax(
            final @RequestParam("id") Integer id
    ) throws Exception {

        final ServiceResponse result = boardPostService.delete(id);
        final boolean isSuccess = result.getRslt();
        final String rsltMsg = MessageUtils.getMessage("common.result.success");

        return ResponseEntity.ok(AjaxResponse.fromResponseWithObj(result, rsltMsg));
    }
}
