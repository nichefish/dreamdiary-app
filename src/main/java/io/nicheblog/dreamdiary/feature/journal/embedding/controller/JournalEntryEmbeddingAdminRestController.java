package io.nicheblog.dreamdiary.feature.journal.embedding.controller;

import io.nicheblog.dreamdiary.feature.journal.embedding.model.JournalEntryEmbeddingStatsDto;
import io.nicheblog.dreamdiary.feature.journal.embedding.service.JournalEntryEmbeddingQueueService;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.Url;
import io.nicheblog.dreamdiary.global.util.MessageUtils;
import io.nicheblog.dreamdiary.infrastructure.web.model.AjaxResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Log4j2
public class JournalEntryEmbeddingAdminRestController {

    private final JournalEntryEmbeddingQueueService journalEntryEmbeddingQueueService;

    @GetMapping(Url.ADMIN_JOURNAL_ENTRY_EMBEDDING_STATS)
    @Secured(Constant.ROLE_MNGR)
    @ResponseBody
    public ResponseEntity<AjaxResponse> getStats() {
        final JournalEntryEmbeddingStatsDto stats = journalEntryEmbeddingQueueService.getStats();
        return ResponseEntity.ok(AjaxResponse.withAjaxResult(true, MessageUtils.RSLT_SUCCESS).withObj(stats));
    }
}
