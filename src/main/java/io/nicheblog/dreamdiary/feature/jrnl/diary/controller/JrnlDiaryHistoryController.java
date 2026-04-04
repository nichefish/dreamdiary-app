package io.nicheblog.dreamdiary.feature.jrnl.diary.controller;

import io.nicheblog.dreamdiary.feature.clsf.history.model.HistoryDto;
import io.nicheblog.dreamdiary.feature.jrnl.diary.model.JrnlDiaryDto;
import io.nicheblog.dreamdiary.feature.jrnl.diary.service.my.MyJrnlDiaryHistoryService;
import io.nicheblog.dreamdiary.feature.jrnl.diary.service.my.MyJrnlDiaryService;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.infrastructure.web.model.AjaxResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class JrnlDiaryHistoryController {

    private final MyJrnlDiaryService myJrnlDiaryService;
    private final MyJrnlDiaryHistoryService myJrnlDiaryHistoryService;

    @GetMapping(value = {Url.JRNL_DIARY_HISTORIES})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> jrnlDiaryHistoryListAjax(
            final @PathVariable("postNo") Integer postNo
    ) throws Exception {

        final JrnlDiaryDto retrievedDto = myJrnlDiaryService.getMyDtlDtoWithCache(postNo);
        final List<HistoryDto> historyList = myJrnlDiaryHistoryService.getMyHistoryList(postNo);
        retrievedDto.setHistoryList(historyList);

        return ResponseEntity.ok(AjaxResponse.withAjaxResult(true, MessageUtils.RSLT_SUCCESS).withObj(retrievedDto));
    }

    @PostMapping(value = {Url.JRNL_DIARY_HISTORY_RESTORE})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> jrnlDiaryHistoryRestoreAjax(
            final @PathVariable("postNo") Integer postNo,
            final @PathVariable("historyNo") Integer historyNo
    ) throws Exception {

        final JrnlDiaryDto restoredDto = myJrnlDiaryHistoryService.restoreMyHistory(postNo, historyNo);
        return ResponseEntity.ok(AjaxResponse.withAjaxResult(true, MessageUtils.RSLT_SUCCESS).withObj(restoredDto));
    }

    @DeleteMapping(value = {Url.JRNL_DIARY_HISTORY})
    @Secured({Constant.ROLE_USER, Constant.ROLE_MNGR})
    @ResponseBody
    public ResponseEntity<AjaxResponse> jrnlDiaryHistoryDeleteAjax(
            final @PathVariable("postNo") Integer postNo,
            final @PathVariable("historyNo") Integer historyNo
    ) throws Exception {

        final boolean deleted = myJrnlDiaryHistoryService.deleteMyHistory(postNo, historyNo);
        return ResponseEntity.ok(AjaxResponse.withAjaxResult(deleted, deleted ? MessageUtils.RSLT_SUCCESS : MessageUtils.RSLT_FAILURE));
    }
}
