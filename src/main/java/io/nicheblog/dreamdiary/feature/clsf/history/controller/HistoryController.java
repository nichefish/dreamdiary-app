package io.nicheblog.dreamdiary.feature.clsf.history.controller;

import io.nicheblog.dreamdiary.feature.clsf.ContentType;
import io.nicheblog.dreamdiary.feature.clsf._shared.model.BaseClsfDto;
import io.nicheblog.dreamdiary.feature.clsf.history.service.my.MyHistoryFacade;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.infrastructure.web.model.AjaxResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class HistoryController {

    private final MyHistoryFacade myHistoryFacade;

    @GetMapping(value = {Url.HISTORIES})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> clsfHistoryListAjax(
            final @PathVariable("contentType") String contentType,
            final @PathVariable("postNo") Integer postNo
    ) throws Exception {
        final ContentType resolvedContentType = ContentType.get(contentType);
        final BaseClsfDto retrievedDto = myHistoryFacade.getMyHistoryTarget(resolvedContentType, postNo);
        return ResponseEntity.ok(AjaxResponse.withAjaxResult(true, MessageUtils.RSLT_SUCCESS).withObj(retrievedDto));
    }

    @PostMapping(value = {Url.HISTORY_RESTORE})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> clsfHistoryRestoreAjax(
            final @PathVariable("contentType") String contentType,
            final @PathVariable("postNo") Integer postNo,
            final @PathVariable("historyNo") Integer historyNo
    ) throws Exception {
        final ContentType resolvedContentType = ContentType.get(contentType);
        final BaseClsfDto restoredDto = myHistoryFacade.restoreMyHistory(resolvedContentType, postNo, historyNo);
        return ResponseEntity.ok(AjaxResponse.withAjaxResult(true, MessageUtils.RSLT_SUCCESS).withObj(restoredDto));
    }

    @DeleteMapping(value = {Url.HISTORY})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> clsfHistoryDeleteAjax(
            final @PathVariable("contentType") String contentType,
            final @PathVariable("postNo") Integer postNo,
            final @PathVariable("historyNo") Integer historyNo
    ) throws Exception {
        final ContentType resolvedContentType = ContentType.get(contentType);
        final boolean deleted = myHistoryFacade.deleteMyHistory(resolvedContentType, postNo, historyNo);
        return ResponseEntity.ok(AjaxResponse.withAjaxResult(deleted, deleted ? MessageUtils.RSLT_SUCCESS : MessageUtils.RSLT_FAILURE));
    }

    @DeleteMapping(value = {Url.HISTORY_CLEAR})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> clsfHistoryClearAjax(
            final @PathVariable("contentType") String contentType,
            final @PathVariable("postNo") Integer postNo
    ) throws Exception {
        final ContentType resolvedContentType = ContentType.get(contentType);
        final boolean deleted = myHistoryFacade.deleteAllMyHistory(resolvedContentType, postNo);
        return ResponseEntity.ok(AjaxResponse.withAjaxResult(deleted, deleted ? MessageUtils.RSLT_SUCCESS : MessageUtils.RSLT_FAILURE));
    }
}
