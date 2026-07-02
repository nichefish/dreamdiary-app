package io.nicheblog.dreamdiary.feature.board.group.controller;

import io.nicheblog.dreamdiary.feature.board.group.model.BoardDto;
import io.nicheblog.dreamdiary.feature.board.group.model.BoardParam;
import io.nicheblog.dreamdiary.feature.board.group.model.BoardSearchParam;
import io.nicheblog.dreamdiary.feature.board.group.service.BoardService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * BoardRestController
 * <pre>
 *  게시판 관리(그룹) REST API 컨트롤러.
 *  관리자 화면에서 게시판 그룹의 등록/조회/수정/삭제/사용여부/정렬을 처리한다.
 * </pre>
 *
 * @author nichefish
 */
@RestController
@RequiredArgsConstructor
@Log4j2
public class BoardRestController extends BaseControllerImpl {

    /** 기본 URL */
    @Getter
    private final String baseUrl = Url.BOARD_ADMIN_PAGE;
    /** 활동 카테고리 (로그 적재용) */
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.BOARD;

    /** 게시판 서비스 */
    private final BoardService boardService;

    /**
     * 게시판 그룹 목록 조회.
     *
     * @param searchParam 검색 조건
     * @param page 페이지 번호 (0-based)
     * @param size 페이지 크기
     * @return Spring Page 형태의 목록 응답
     * @throws Exception 처리 중 예외
     */
    @GetMapping(Url.BOARD_GROUPS)
    @Secured({Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> boardListAjax(
            @ModelAttribute final BoardSearchParam searchParam,
            @RequestParam(defaultValue = "0") final int page,
            @RequestParam(defaultValue = "10") final int size
    ) throws Exception {
        final PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "sortOrder"));
        final Page<BoardDto> pageResult = boardService.getPageDto(searchParam, pageRequest);

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(true, MessageUtils.getMessage("common.result.success")).withObj(pageResult));
    }

    /**
     * 게시판 그룹 등록.
     *
     * @param board 등록할 게시판 DTO
     * @return 처리 결과 응답
     * @throws Exception 처리 중 예외
     */
    @PostMapping(Url.BOARD_GROUPS)
    @Secured({Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> boardRegAjax(final @Valid BoardDto board) throws Exception {
        final ServiceResponse result = boardService.regist(board);
        final boolean isSuccess = result.getRslt();
        final String rsltMsg = isSuccess ? MessageUtils.getMessage("common.result.success") : MessageUtils.getMessage("common.result.failure");

        return ResponseEntity.ok(AjaxResponse.fromResponseWithObj(result, rsltMsg));
    }

    /**
     * 게시판 그룹 상세 조회.
     *
     * @param id 게시판 식별자
     * @return 조회 결과 응답
     * @throws Exception 처리 중 예외
     */
    @GetMapping(Url.BOARD_GROUP)
    @Secured({Constant.ROLE_MNGR})
    public ResponseEntity<AjaxResponse> boardDtlAjax(final @PathVariable Integer id) throws Exception {
        final BoardDto board = boardService.getDtlDto(id);
        return ResponseEntity.ok(AjaxResponse.withAjaxResult(true, MessageUtils.getMessage("common.result.success")).withObj(board));
    }

    /**
     * 게시판 그룹 수정.
     *
     * @param board 수정할 게시판 DTO
     * @return 처리 결과 응답
     * @throws Exception 처리 중 예외
     */
    @PostMapping(Url.BOARD_GROUP)
    @Secured({Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> boardMdfItemAjax(final @Valid BoardDto board) throws Exception {
        final ServiceResponse result = boardService.modify(board);
        final boolean isSuccess = result.getRslt();
        final String rsltMsg = isSuccess ? MessageUtils.getMessage("common.result.success") : MessageUtils.getMessage("common.result.failure");

        return ResponseEntity.ok(AjaxResponse.fromResponseWithObj(result, rsltMsg));
    }

    /**
     * 게시판 그룹 삭제.
     *
     * @param id 삭제할 게시판 식별자
     * @return 처리 결과 응답
     * @throws Exception 처리 중 예외
     */
    @DeleteMapping(Url.BOARD_GROUP)
    @Secured({Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> boardDelAjax(final @PathVariable Integer id) throws Exception {
        final ServiceResponse result = boardService.delete(id);
        return ResponseEntity.ok(AjaxResponse.withAjaxResult(result.getRslt(), MessageUtils.getMessage("common.result.success")));
    }

    /**
     * 게시판 그룹 사용 처리.
     *
     * @param id 사용 처리할 게시판 식별자
     * @return 처리 결과 응답
     * @throws Exception 처리 중 예외
     */
    @PostMapping(Url.BOARD_GROUP_USE)
    @Secured({Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> boardUseAjax(final @PathVariable Integer id) throws Exception {
        final ServiceResponse result = boardService.setUse(id, "Y");
        return ResponseEntity.ok(AjaxResponse.withAjaxResult(result.getRslt(), MessageUtils.getMessage("common.result.success")));
    }

    /**
     * 게시판 그룹 미사용 처리.
     *
     * @param id 미사용 처리할 게시판 식별자
     * @return 처리 결과 응답
     * @throws Exception 처리 중 예외
     */
    @PostMapping(Url.BOARD_GROUP_UNUSE)
    @Secured({Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> boardUnuseAjax(final @PathVariable Integer id) throws Exception {
        final ServiceResponse result = boardService.setUse(id, "N");
        return ResponseEntity.ok(AjaxResponse.withAjaxResult(result.getRslt(), MessageUtils.getMessage("common.result.success")));
    }

    /**
     * 게시판 그룹 정렬 순서 저장.
     *
     * @param boardParam 정렬 순서 목록을 포함한 요청 객체
     * @return 처리 결과 응답
     * @throws Exception 처리 중 예외
     */
    @PutMapping(Url.BOARD_GROUPS_SORT_ORDERS)
    @Secured({Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> boardSortOrderAjax(final @RequestBody BoardParam boardParam) throws Exception {
        final ServiceResponse result = boardService.sortOrder(boardParam.getSortOrders());
        return ResponseEntity.ok(AjaxResponse.withAjaxResult(result.getRslt(), MessageUtils.getMessage("common.result.success")));
    }
}
