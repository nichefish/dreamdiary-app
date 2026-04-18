package io.nicheblog.dreamdiary.feature.board.group.controller;

import io.nicheblog.dreamdiary.feature.board.group.model.BoardDto;
import io.nicheblog.dreamdiary.feature.board.group.model.BoardParam;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@Log4j2
public class BoardRestController extends BaseControllerImpl {

    @Getter
    private final String baseUrl = Url.BOARD_GROUP_LIST;
    @Getter
    private final ActvtyCtgr actvtyCtgr = ActvtyCtgr.BOARD_GROUP;

    private final BoardService boardService;

    @PostMapping(Url.BOARD_GROUP_REG_AJAX)
    @Secured({Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> boardRegAjax(final @Valid BoardDto board) throws Exception {
        final ServiceResponse result = boardService.regist(board);
        final boolean isSuccess = result.getRslt();
        final String rsltMsg = isSuccess ? MessageUtils.RSLT_SUCCESS : MessageUtils.RSLT_FAILURE;

        return ResponseEntity.ok(AjaxResponse.fromResponseWithObj(result, rsltMsg));
    }

    @GetMapping(Url.BOARD_GROUP_DTL_AJAX)
    @Secured({Constant.ROLE_MNGR})
    public ResponseEntity<AjaxResponse> boardDtlAjax(final @RequestParam("id") Integer id) throws Exception {
        final BoardDto board = boardService.getDtlDto(id);
        return ResponseEntity.ok(AjaxResponse.withAjaxResult(true, MessageUtils.RSLT_SUCCESS).withObj(board));
    }

    @PostMapping(Url.BOARD_GROUP_MDF_ITEM_AJAX)
    @Secured({Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> boardMdfItemAjax(final @Valid BoardDto board) throws Exception {
        final ServiceResponse result = boardService.modify(board);
        final boolean isSuccess = result.getRslt();
        final String rsltMsg = isSuccess ? MessageUtils.RSLT_SUCCESS : MessageUtils.RSLT_FAILURE;

        return ResponseEntity.ok(AjaxResponse.fromResponseWithObj(result, rsltMsg));
    }

    @PostMapping(Url.BOARD_GROUP_DEL_AJAX)
    @Secured({Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> boardDelAjax(final @RequestParam("id") Integer id) throws Exception {
        final ServiceResponse result = boardService.delete(id);
        return ResponseEntity.ok(AjaxResponse.withAjaxResult(result.getRslt(), MessageUtils.RSLT_SUCCESS));
    }

    @PostMapping(Url.BOARD_GROUP_USE_AJAX)
    @Secured({Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> boardUseAjax(final @RequestParam("id") Integer id) throws Exception {
        final ServiceResponse result = boardService.setUse(id, "Y");
        return ResponseEntity.ok(AjaxResponse.withAjaxResult(result.getRslt(), MessageUtils.RSLT_SUCCESS));
    }

    @PostMapping(Url.BOARD_GROUP_UNUSE_AJAX)
    @Secured({Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> boardUnuseAjax(final @RequestParam("id") Integer id) throws Exception {
        final ServiceResponse result = boardService.setUse(id, "N");
        return ResponseEntity.ok(AjaxResponse.withAjaxResult(result.getRslt(), MessageUtils.RSLT_SUCCESS));
    }

    @PutMapping(Url.BOARD_GROUP_SORT_ORDR_AJAX)
    @Secured({Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> boardSortOrderAjax(final @RequestBody BoardParam boardParam) throws Exception {
        final ServiceResponse result = boardService.sortOrder(boardParam.getSortOrders());
        return ResponseEntity.ok(AjaxResponse.withAjaxResult(result.getRslt(), MessageUtils.RSLT_SUCCESS));
    }
}
