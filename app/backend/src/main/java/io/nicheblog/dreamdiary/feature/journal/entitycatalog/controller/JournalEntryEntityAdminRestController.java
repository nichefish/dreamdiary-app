package io.nicheblog.dreamdiary.feature.journal.entitycatalog.controller;

import io.nicheblog.dreamdiary.feature.journal.entitycatalog.model.JournalEntryEntityQueueStatsDto;
import io.nicheblog.dreamdiary.feature.journal.entitycatalog.model.JournalEntryEntityQueueSyncResultDto;
import io.nicheblog.dreamdiary.feature.journal.entitycatalog.service.JournalEntryEntityQueueService;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.infrastructure.web.model.AjaxResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin REST endpoints for asynchronous journal entity sync.
 */
@RestController
@RequiredArgsConstructor
@Log4j2
public class JournalEntryEntityAdminRestController {

    private final JournalEntryEntityQueueService journalEntryEntityQueueService;

    /**
     * Return current queue stats for entity ref / role sync.
     *
     * @return queue stats
     */
    @GetMapping(Url.ADMIN_JOURNAL_ENTRY_ENTITY_STATS)
    @Secured(Constant.ROLE_MNGR)
    @ResponseBody
    public ResponseEntity<AjaxResponse> getStats() {
        final JournalEntryEntityQueueStatsDto stats = journalEntryEntityQueueService.getStats();
        return ResponseEntity.ok(AjaxResponse.withAjaxResult(true, MessageUtils.getMessage("common.result.success")).withObj(stats));
    }

    /**
     * Requeue all current journal entries into the entity sync queue.
     *
     * @return enqueue/sync result
     * @throws Exception when queue sync fails
     */
    @PostMapping(Url.ADMIN_JOURNAL_ENTRY_ENTITY_SYNC)
    @Secured(Constant.ROLE_MNGR)
    @ResponseBody
    public ResponseEntity<AjaxResponse> sync() throws Exception {
        final JournalEntryEntityQueueSyncResultDto result = journalEntryEntityQueueService.syncWithJournalEntries();
        return ResponseEntity.ok(AjaxResponse.withAjaxResult(true, MessageUtils.getMessage("common.result.success")).withObj(result));
    }

    /**
     * Requeue failed entity sync rows for retry.
     *
     * @return requeued row count
     */
    @PostMapping(Url.ADMIN_JOURNAL_ENTRY_ENTITY_REQUEUE_FAILED)
    @Secured(Constant.ROLE_MNGR)
    @ResponseBody
    public ResponseEntity<AjaxResponse> requeueFailed() {
        final long requeued = journalEntryEntityQueueService.requeueFailed();
        return ResponseEntity.ok(AjaxResponse.withAjaxResult(true, MessageUtils.getMessage("common.result.success")).withObj(requeued));
    }
}
