package io.nicheblog.dreamdiary.feature.journal.embedding.controller;

import io.nicheblog.dreamdiary.feature.chat.client.OllamaClient;
import io.nicheblog.dreamdiary.feature.journal.embedding.model.JournalEntryEmbeddingStatsDto;
import io.nicheblog.dreamdiary.feature.journal.embedding.model.JournalEntryEmbeddingSyncJobStatusDto;
import io.nicheblog.dreamdiary.feature.journal.embedding.service.JournalEntryEmbeddingQualityEvalService;
import io.nicheblog.dreamdiary.feature.journal.embedding.service.JournalEntryEmbeddingQueueService;
import io.nicheblog.dreamdiary.feature.journal.embedding.service.JournalEntryEmbeddingSyncJobService;
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
    private final JournalEntryEmbeddingSyncJobService journalEntryEmbeddingSyncJobService;
    private final JournalEntryEmbeddingQualityEvalService journalEntryEmbeddingQualityEvalService;
    private final OllamaClient ollamaClient;

    /**
     * 임베딩 큐의 전체/대기/처리/완료 건수와 진행률을 조회한다.
     *
     * @return 임베딩 작업 통계 DTO를 담은 Ajax 응답
     */
    @GetMapping(Url.ADMIN_JOURNAL_ENTRY_EMBEDDING_STATS)
    @Secured(Constant.ROLE_MNGR)
    @ResponseBody
    public ResponseEntity<AjaxResponse> getStats() {
        final JournalEntryEmbeddingStatsDto stats = journalEntryEmbeddingQueueService.getStats()
                .withSyncStatus(journalEntryEmbeddingSyncJobService.getStatus());
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
        final JournalEntryEmbeddingSyncJobStatusDto status = journalEntryEmbeddingSyncJobService.startSync();
        return ResponseEntity.ok(AjaxResponse.withAjaxResult(true, MessageUtils.RSLT_SUCCESS).withObj(status));
    }

    /**
     * FAILED 상태 임베딩 작업 row를 다시 PENDING으로 되돌립니다.
     *
     * @return 재대기 처리한 row 수
     */
    @PostMapping(Url.ADMIN_JOURNAL_ENTRY_EMBEDDING_REQUEUE_FAILED)
    @Secured(Constant.ROLE_MNGR)
    @ResponseBody
    public ResponseEntity<AjaxResponse> requeueFailed() {
        final long requeued = journalEntryEmbeddingQueueService.requeueFailed();
        return ResponseEntity.ok(AjaxResponse.withAjaxResult(true, MessageUtils.RSLT_SUCCESS).withObj(requeued));
    }

    /**
     * 로컬 Ollama 런타임 가용성을 점검합니다. 자동 기동은 하지 않습니다.
     *
     * @return Ollama health DTO
     */
    @GetMapping(Url.ADMIN_OLLAMA_HEALTH)
    @Secured(Constant.ROLE_MNGR)
    @ResponseBody
    public ResponseEntity<AjaxResponse> getOllamaHealth() {
        return ResponseEntity.ok(AjaxResponse.withAjaxResult(true, MessageUtils.RSLT_SUCCESS)
                .withObj(ollamaClient.checkHealth()));
    }

    /**
     * 현재 임베딩 모델의 한국어 의미 유사도를 고정 시드·코퍼스 샘플로 실측합니다.
     *
     * <p>Ollama({@code nomic-embed-text} 등)가 실행 중이어야 합니다.</p>
     *
     * @return 품질 실측 리포트
     */
    @GetMapping(Url.ADMIN_JOURNAL_ENTRY_EMBEDDING_QUALITY_EVAL)
    @Secured(Constant.ROLE_MNGR)
    @ResponseBody
    public ResponseEntity<AjaxResponse> runQualityEval() {
        return ResponseEntity.ok(AjaxResponse.withAjaxResult(true, MessageUtils.RSLT_SUCCESS)
                .withObj(journalEntryEmbeddingQualityEvalService.runEval()));
    }
}
