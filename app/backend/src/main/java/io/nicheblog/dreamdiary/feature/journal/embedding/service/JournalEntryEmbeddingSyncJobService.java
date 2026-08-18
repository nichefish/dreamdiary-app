package io.nicheblog.dreamdiary.feature.journal.embedding.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.nicheblog.dreamdiary.feature.journal.embedding.entity.JournalEntryEmbeddingSyncJobEntity;
import io.nicheblog.dreamdiary.feature.journal.embedding.model.JournalEntryEmbeddingSyncJobStatusDto;
import io.nicheblog.dreamdiary.feature.journal.embedding.model.JournalEntryEmbeddingSyncResultDto;
import io.nicheblog.dreamdiary.feature.journal.embedding.repository.jpa.JournalEntryEmbeddingSyncJobRepository;
import io.nicheblog.dreamdiary.feature.journal.setting.service.JournalSettingService;
import io.nicheblog.dreamdiary.infrastructure.log.p6spy.P6SpySqlLogQuietScope;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.net.InetAddress;
import java.time.Duration;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

@Service
@RequiredArgsConstructor
@Log4j2
public class JournalEntryEmbeddingSyncJobService {

    private static final int ERROR_MESSAGE_LIMIT = 4000;
    private static final String JOB_KEY = "JOURNAL_ENTRY_EMBEDDING_SYNC";
    private static final String STATUS_IDLE = "IDLE";
    private static final String STATUS_RUNNING = "RUNNING";
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String STATUS_FAILED = "FAILED";
    private static final String PHASE_IDLE = "IDLE";
    private static final String PHASE_RUNNING = "RUNNING";
    private static final String PHASE_COMPLETED = "COMPLETED";
    private static final String PHASE_FAILED = "FAILED";
    private static final Duration STALE_RUNNING_AGE = Duration.ofMinutes(30);

    private final JournalEntryEmbeddingQueueService queueService;
    private final JournalSettingService journalSettingService;
    private final JournalEntryEmbeddingSyncJobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final ObjectMapper objectMapper;

    @Qualifier("taskExecutor")
    private final Executor taskExecutor;

    public JournalEntryEmbeddingSyncJobStatusDto startSync() {
        if (!journalSettingService.isEmbeddingEnabled()) {
            log.info("Journal entry embedding sync skipped. reason=embeddingDisabled");
            return getStatus();
        }

        final StartDecision decision = claimStart();
        if (!decision.started) {
            return decision.status;
        }

        try {
            taskExecutor.execute(this::runSync);
        } catch (final RejectedExecutionException e) {
            markFailed(e);
            log.warn("Journal entry embedding sync executor rejected the job.", e);
        }
        return getStatus();
    }

    public JournalEntryEmbeddingSyncJobStatusDto getStatus() {
        return new TransactionTemplate(transactionManager).execute(status ->
                jobRepository.findFirstByJobKey(JOB_KEY)
                        .map(job -> toStatusDto(markStaleIfNeeded(job)))
                        .orElseGet(this::defaultStatus)
        );
    }

    private StartDecision claimStart() {
        return new TransactionTemplate(transactionManager).execute(status -> {
            final JournalEntryEmbeddingSyncJobEntity job = getOrCreateJobRow(true);
            if (STATUS_RUNNING.equals(job.getStatus()) && !isStale(job)) {
                return new StartDecision(false, toStatusDto(job));
            }

            final long syncTotal = queueService.countJournalEntriesForSync();
            final LocalDateTime now = LocalDateTime.now();
            job.setStatus(STATUS_RUNNING);
            job.setPhase(PHASE_RUNNING);
            job.setStartedAt(now);
            job.setFinishedAt(null);
            job.setHeartbeatAt(now);
            job.setProcessedCount(0L);
            job.setTotalCount(syncTotal);
            job.setResultJson(null);
            job.setErrorMessage(null);
            job.setLockedBy(resolveNodeName());
            jobRepository.saveAndFlush(job);
            return new StartDecision(true, toStatusDto(job));
        });
    }

    /**
     * 전수 sync를 실행한다. 이 스레드의 p6spy statement SQL은 DEBUG로 남긴다.
     */
    private void runSync() {
        P6SpySqlLogQuietScope.run(() -> {
            try {
                if (!journalSettingService.isEmbeddingEnabled()) {
                    log.info("Journal entry embedding sync aborted. reason=embeddingDisabled");
                    markFailed(new IllegalStateException("Journal embedding is disabled."));
                    return;
                }
                final JournalEntryEmbeddingSyncResultDto syncResult =
                        queueService.syncWithJournalEntries(this::markProgress);
                markCompleted(syncResult);
                log.info(
                        "Journal entry embedding sync completed. entries={}, created={}, requeued={}, unchanged={}, skipped={}, removed={}",
                        syncResult.getActiveEntryCount(),
                        syncResult.getCreated(),
                        syncResult.getRequeued(),
                        syncResult.getUnchanged(),
                        syncResult.getSkipped(),
                        syncResult.getRemoved()
                );
            } catch (final Exception e) {
                markFailed(e);
                log.warn("Journal entry embedding sync failed.", e);
            }
        });
    }

    private void markProgress(final int processedCount) {
        if (!journalSettingService.isEmbeddingEnabled()) {
            throw new IllegalStateException("Journal embedding is disabled.");
        }
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            final JournalEntryEmbeddingSyncJobEntity job = getOrCreateJobRow(false);
            if (!STATUS_RUNNING.equals(job.getStatus())) return;
            job.setProcessedCount((long) processedCount);
            if (job.getTotalCount() == null || processedCount > job.getTotalCount()) {
                job.setTotalCount((long) processedCount);
            }
            job.setHeartbeatAt(LocalDateTime.now());
            jobRepository.save(job);
        });
    }

    private void markCompleted(final JournalEntryEmbeddingSyncResultDto syncResult) {
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            final JournalEntryEmbeddingSyncJobEntity job = getOrCreateJobRow(false);
            final LocalDateTime now = LocalDateTime.now();
            job.setStatus(STATUS_COMPLETED);
            job.setPhase(PHASE_COMPLETED);
            job.setProcessedCount(syncResult.getActiveEntryCount());
            job.setTotalCount(syncResult.getActiveEntryCount());
            job.setFinishedAt(now);
            job.setHeartbeatAt(now);
            job.setResultJson(toResultJson(syncResult));
            job.setErrorMessage(null);
            job.setLockedBy(null);
            jobRepository.save(job);
        });
    }

    private void markFailed(final Exception e) {
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            final JournalEntryEmbeddingSyncJobEntity job = getOrCreateJobRow(false);
            final LocalDateTime now = LocalDateTime.now();
            job.setStatus(STATUS_FAILED);
            job.setPhase(PHASE_FAILED);
            job.setFinishedAt(now);
            job.setHeartbeatAt(now);
            job.setErrorMessage(toErrorMessage(e));
            job.setLockedBy(null);
            jobRepository.save(job);
        });
    }

    private JournalEntryEmbeddingSyncJobEntity getOrCreateJobRow(final boolean forUpdate) {
        return (forUpdate
                ? jobRepository.findFirstByJobKeyForUpdate(JOB_KEY)
                : jobRepository.findFirstByJobKey(JOB_KEY)
        ).orElseGet(() -> jobRepository.saveAndFlush(JournalEntryEmbeddingSyncJobEntity.builder()
                .jobKey(JOB_KEY)
                .status(STATUS_IDLE)
                .phase(PHASE_IDLE)
                .processedCount(0L)
                .totalCount(0L)
                .build()));
    }

    private JournalEntryEmbeddingSyncJobStatusDto toStatusDto(final JournalEntryEmbeddingSyncJobEntity job) {
        return JournalEntryEmbeddingSyncJobStatusDto.builder()
                .running(STATUS_RUNNING.equals(job.getStatus()) && !isStale(job))
                .phase(StringUtils.defaultIfBlank(job.getPhase(), PHASE_IDLE))
                .startedAt(job.getStartedAt())
                .finishedAt(job.getFinishedAt())
                .processed(job.getProcessedCount() == null ? 0L : job.getProcessedCount())
                .total(job.getTotalCount() == null ? 0L : job.getTotalCount())
                .result(readResult(job.getResultJson()))
                .errorMessage(job.getErrorMessage())
                .build();
    }

    private JournalEntryEmbeddingSyncJobEntity markStaleIfNeeded(final JournalEntryEmbeddingSyncJobEntity job) {
        if (!isStale(job)) return job;

        final LocalDateTime now = LocalDateTime.now();
        job.setStatus(STATUS_FAILED);
        job.setPhase(PHASE_FAILED);
        job.setFinishedAt(now);
        job.setHeartbeatAt(now);
        job.setLockedBy(null);
        job.setErrorMessage("Stale RUNNING sync job was marked as failed.");
        return jobRepository.save(job);
    }

    private JournalEntryEmbeddingSyncJobStatusDto defaultStatus() {
        return JournalEntryEmbeddingSyncJobStatusDto.builder()
                .running(false)
                .phase(PHASE_IDLE)
                .processed(0L)
                .total(0L)
                .build();
    }

    private boolean isStale(final JournalEntryEmbeddingSyncJobEntity job) {
        if (!STATUS_RUNNING.equals(job.getStatus()) || job.getHeartbeatAt() == null) return false;
        return job.getHeartbeatAt().isBefore(LocalDateTime.now().minus(STALE_RUNNING_AGE));
    }

    private JournalEntryEmbeddingSyncResultDto readResult(final String resultJson) {
        if (StringUtils.isBlank(resultJson)) return null;
        try {
            return objectMapper.readValue(resultJson, JournalEntryEmbeddingSyncResultDto.class);
        } catch (final Exception e) {
            log.warn("Failed to read embedding sync result JSON.", e);
            return null;
        }
    }

    private String toResultJson(final JournalEntryEmbeddingSyncResultDto result) {
        try {
            return objectMapper.writeValueAsString(result);
        } catch (final Exception e) {
            log.warn("Failed to serialize embedding sync result.", e);
            return null;
        }
    }

    private String toErrorMessage(final Exception exception) {
        if (exception == null) return null;
        final String message = exception.getMessage() == null ? exception.toString() : exception.getMessage();
        return StringUtils.abbreviate(message, ERROR_MESSAGE_LIMIT);
    }

    private String resolveNodeName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (final Exception e) {
            return "unknown";
        }
    }

    private record StartDecision(boolean started, JournalEntryEmbeddingSyncJobStatusDto status) {}
}
