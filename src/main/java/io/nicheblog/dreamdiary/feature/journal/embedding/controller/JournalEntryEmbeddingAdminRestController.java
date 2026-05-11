package io.nicheblog.dreamdiary.feature.journal.embedding.controller;

import io.nicheblog.dreamdiary.feature.journal.embedding.model.JournalEntryEmbeddingStatsDto;
import io.nicheblog.dreamdiary.feature.journal.embedding.model.JournalEntryEmbeddingSyncResultDto;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 저널 엔트리 임베딩 작업 현황을 관리자 화면에 제공하는 REST 컨트롤러입니다.
 */
@RestController
@RequiredArgsConstructor
@Log4j2
public class JournalEntryEmbeddingAdminRestController {

    private final JournalEntryEmbeddingQueueService journalEntryEmbeddingQueueService;

    /**
     * 임베딩 큐의 전체/대기/처리/완료 건수와 진행률을 조회한다.
     *
     * @return 임베딩 작업 통계 DTO를 담은 Ajax 응답
     */
    @GetMapping(Url.ADMIN_JOURNAL_ENTRY_EMBEDDING_STATS)
    @Secured(Constant.ROLE_MNGR)
    @ResponseBody
    public ResponseEntity<AjaxResponse> getStats() {
        final JournalEntryEmbeddingStatsDto stats = journalEntryEmbeddingQueueService.getStats();
        return ResponseEntity.ok(AjaxResponse.withAjaxResult(true, MessageUtils.RSLT_SUCCESS).withObj(stats));
    }

    /**
     * 현재 저널 엔트리 기준으로 임베딩 작업 테이블을 수동 재동기화한다.
     *
     * @return 동기화 결과 DTO를 담은 Ajax 응답
     * @throws Exception 동기화 중 예외가 발생한 경우
     */
    @PostMapping(Url.ADMIN_JOURNAL_ENTRY_EMBEDDING_SYNC)
    @Secured(Constant.ROLE_MNGR)
    @ResponseBody
    public ResponseEntity<AjaxResponse> sync() throws Exception {
        final JournalEntryEmbeddingSyncResultDto result = journalEntryEmbeddingQueueService.syncWithJournalEntries();
        return ResponseEntity.ok(AjaxResponse.withAjaxResult(true, MessageUtils.RSLT_SUCCESS).withObj(result));
    }
}
