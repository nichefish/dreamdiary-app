package io.nicheblog.dreamdiary.feature.attachable.history.controller;

import io.nicheblog.dreamdiary.feature.attachable._shared.model.BaseAttachableDto;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.history.service.my.MyHistoryFacade;
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
    public ResponseEntity<AjaxResponse> attachableHistoryListAjax(
            final @PathVariable("contentType") String contentType,
            final @PathVariable("id") Integer id
    ) throws Exception {
        final ContentType resolvedContentType = ContentType.get(contentType);
        final BaseAttachableDto retrievedDto = myHistoryFacade.getMyHistoryTarget(resolvedContentType, id);
        return ResponseEntity.ok(AjaxResponse.withAjaxResult(true, MessageUtils.getMessage("common.result.success")).withObj(retrievedDto));
    }

    @PostMapping(value = {Url.HISTORY_RESTORE})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> attachableHistoryRestoreAjax(
            final @PathVariable("contentType") String contentType,
            final @PathVariable("id") Integer id,
            final @PathVariable("historyId") Integer historyId
    ) throws Exception {
        final ContentType resolvedContentType = ContentType.get(contentType);
        final BaseAttachableDto restoredDto = myHistoryFacade.restoreMyHistory(resolvedContentType, id, historyId);
        return ResponseEntity.ok(AjaxResponse.withAjaxResult(true, MessageUtils.getMessage("common.result.success")).withObj(restoredDto));
    }

    @DeleteMapping(value = {Url.HISTORY})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> attachableHistoryDeleteAjax(
            final @PathVariable("contentType") String contentType,
            final @PathVariable("id") Integer id,
            final @PathVariable("historyId") Integer historyId
    ) throws Exception {
        final ContentType resolvedContentType = ContentType.get(contentType);
        final boolean deleted = myHistoryFacade.deleteMyHistory(resolvedContentType, id, historyId);
        return ResponseEntity.ok(AjaxResponse.withAjaxResult(deleted, deleted ? MessageUtils.getMessage("common.result.success") : MessageUtils.getMessage("common.result.failure")));
    }

    @DeleteMapping(value = {Url.HISTORY_CLEAR})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> attachableHistoryClearAjax(
            final @PathVariable("contentType") String contentType,
            final @PathVariable("id") Integer id
    ) throws Exception {
        final ContentType resolvedContentType = ContentType.get(contentType);
        final boolean deleted = myHistoryFacade.deleteAllMyHistory(resolvedContentType, id);
        return ResponseEntity.ok(AjaxResponse.withAjaxResult(deleted, deleted ? MessageUtils.getMessage("common.result.success") : MessageUtils.getMessage("common.result.failure")));
    }
}
