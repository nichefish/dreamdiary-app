package io.nicheblog.dreamdiary.feature.journal.entitycatalog.service;

import io.nicheblog.dreamdiary.feature.journal.entry.entity.JournalEntryEntity;
import io.nicheblog.dreamdiary.feature.journal.entry.repository.jpa.JournalEntryRepository;
import io.nicheblog.dreamdiary.feature.journal.entitycatalog.entity.JournalEntryEntityJobEntity;
import io.nicheblog.dreamdiary.feature.journal.entitycatalog.model.JournalEntryEntityQueueStatsDto;
import io.nicheblog.dreamdiary.feature.journal.entitycatalog.model.JournalEntryEntityQueueSyncResultDto;
import io.nicheblog.dreamdiary.feature.journal.entitycatalog.repository.jpa.JournalEntryEntityJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Queue service for asynchronous entity-ref and role sync.
 *
 * <p>The current implementation keeps delete cleanup immediate, but create/update flows
 * enqueue work so entry save latency stays decoupled from mention extraction growth.</p>
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class JournalEntryEntityQueueService {

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_PROCESSING = "PROCESSING";
    public static final String STATUS_SYNCED = "SYNCED";
    public static final String STATUS_FAILED = "FAILED";
    public static final String STATUS_SKIPPED = "SKIPPED";

    private static final int MIN_BATCH_SIZE = 1;
    private static final int MAX_BATCH_SIZE = 100;
    private static final int DEFAULT_BATCH_SIZE = 20;
    private static final int ERROR_MESSAGE_LIMIT = 4000;

    private final JournalEntryEntityJobRepository repository;
    private final JournalEntryRepository journalEntryRepository;
    private final JournalEntryEntityRefSyncService journalEntryEntityRefSyncService;

    /**
     * Queue one journal entry for entity sync when its extracted source text changed.
     *
     * @param journalEntryId source journal entry ID
     * @throws Exception when the source row cannot be prepared
     */
    @Transactional
    public void queueForEntryId(final Integer journalEntryId) throws Exception {
        if (journalEntryId == null) return;

        final JournalEntryEntity entry = journalEntryRepository.findById(journalEntryId).orElse(null);
        if (entry == null) {
            removeByJournalEntryId(journalEntryId);
            return;
        }

        queueForEntry(entry);
    }

    /**
     * Remove the queue row for one journal entry. Delete cleanup of ref/role rows remains immediate.
     *
     * @param journalEntryId source journal entry ID
     */
    @Transactional
    public void removeByJournalEntryId(final Integer journalEntryId) {
        if (journalEntryId == null) return;

        repository.findFirstByJournalEntryId(journalEntryId).ifPresent(repository::delete);
        log.info("Journal entry entity queue row removed. entryId={}", journalEntryId);
    }

    /**
     * Claim pending queue rows and mark them as processing.
     *
     * @param batchSize requested batch size
     * @return claimed queue rows
     */
    @Transactional
    public List<JournalEntryEntityJobEntity> claimPendingBatch(final Integer batchSize) {
        final int normalizedBatchSize = normalizeBatchSize(batchSize);
        final List<JournalEntryEntityJobEntity> jobList = repository.findAndLockPendingBatch(normalizedBatchSize);
        final String nodeName = resolveNodeName();

        jobList.forEach(job -> {
            job.setJobStatus(STATUS_PROCESSING);
            job.setLockedBy(nodeName);
            job.setErrorMessage(null);
        });
        repository.saveAll(jobList);
        repository.flush();
        return jobList;
    }

    /**
     * Mark one queue row as successfully synced.
     *
     * @param jobId queue row ID
     * @param contentHash latest synchronized content hash
     */
    @Transactional
    public void markSynced(final Integer jobId, final String contentHash) {
        if (jobId == null) return;
        repository.findById(jobId).ifPresent(job -> {
            job.setJobStatus(STATUS_SYNCED);
            job.setContentHash(contentHash);
            job.setProcessedAt(new Date());
            job.setLockedBy(null);
            job.setErrorMessage(null);
            repository.save(job);
        });
    }

    /**
     * Mark one queue row as skipped because the source text no longer needs a fresh sync.
     *
     * @param jobId queue row ID
     * @param contentHash latest synchronized content hash
     */
    @Transactional
    public void markSkipped(final Integer jobId, final String contentHash) {
        if (jobId == null) return;
        repository.findById(jobId).ifPresent(job -> {
            job.setJobStatus(STATUS_SKIPPED);
            job.setContentHash(contentHash);
            job.setProcessedAt(new Date());
            job.setLockedBy(null);
            job.setErrorMessage(null);
            repository.save(job);
        });
    }

    /**
     * Mark one queue row as failed.
     *
     * @param jobId queue row ID
     * @param exception failure reason
     */
    @Transactional
    public void markFailed(final Integer jobId, final Exception exception) {
        if (jobId == null) return;
        repository.findById(jobId).ifPresent(job -> {
            job.setJobStatus(STATUS_FAILED);
            job.setLockedBy(null);
            job.setErrorMessage(toErrorMessage(exception));
            repository.save(job);
        });
    }

    /**
     * Requeue stale processing rows older than the given cutoff.
     *
     * @param updatedAt processing timeout cutoff
     * @param batchSize limit
     */
    @Transactional
    public void requeueStaleProcessing(final Date updatedAt, final Integer batchSize) {
        final List<JournalEntryEntityJobEntity> staleJobList = repository
                .findAllByJobStatusAndUpdatedAtBeforeOrderByUpdatedAtAscIdAsc(
                        STATUS_PROCESSING,
                        updatedAt,
                        PageRequest.of(0, normalizeBatchSize(batchSize))
                );
        if (staleJobList.isEmpty()) return;

        staleJobList.forEach(job -> {
            job.setJobStatus(STATUS_PENDING);
            job.setLockedBy(null);
            job.setErrorMessage("Stale PROCESSING row was requeued.");
        });
        repository.saveAll(staleJobList);
        repository.flush();
        log.warn("Requeued stale journal entity jobs. count={}", staleJobList.size());
    }

    /**
     * Return the current pending queue size.
     *
     * @return pending row count
     */
    @Transactional(readOnly = true)
    public long countPending() {
        return repository.countByJobStatus(STATUS_PENDING);
    }

    /**
     * Return current queue stats for admin/operations visibility.
     *
     * @return queue stats
     */
    @Transactional(readOnly = true)
    public JournalEntryEntityQueueStatsDto getStats() {
        final long activeEntryCount = journalEntryRepository.count();
        final long queueRows = repository.count();
        final long unqueuedEntries = Math.max(0L, activeEntryCount - queueRows);
        final long pending = repository.countByJobStatus(STATUS_PENDING);
        final long processing = repository.countByJobStatus(STATUS_PROCESSING);
        final long synced = repository.countByJobStatus(STATUS_SYNCED);
        final long failed = repository.countByJobStatus(STATUS_FAILED);
        final long skipped = repository.countByJobStatus(STATUS_SKIPPED);
        final long remaining = unqueuedEntries + pending + processing + failed;
        final long completed = synced + skipped + failed;

        return JournalEntryEntityQueueStatsDto.builder()
                .total(activeEntryCount)
                .queueRows(queueRows)
                .unqueuedEntries(unqueuedEntries)
                .pending(pending)
                .processing(processing)
                .synced(synced)
                .failed(failed)
                .skipped(skipped)
                .remaining(remaining)
                .completed(completed)
                .completionRate(toPercent(synced, activeEntryCount))
                .queueCompletionRate(toPercent(completed, queueRows))
                .build();
    }

    /**
     * Rebuild queue rows for all current journal entries while removing stale queue rows.
     *
     * @return queue sync result
     * @throws Exception when queueing one source row fails
     */
    @Transactional
    public JournalEntryEntityQueueSyncResultDto syncWithJournalEntries() throws Exception {
        final List<JournalEntryEntity> entryList = journalEntryRepository.findAll();
        final List<JournalEntryEntityJobEntity> queueListBefore = repository.findAll();
        final Set<Integer> activeEntryIdSet = entryList.stream()
                .map(JournalEntryEntity::getId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        long removed = 0L;
        for (final JournalEntryEntityJobEntity queueRow : queueListBefore) {
            if (queueRow.getJournalEntryId() == null) continue;
            if (!activeEntryIdSet.contains(queueRow.getJournalEntryId())) {
                repository.delete(queueRow);
                removed++;
            }
        }
        repository.flush();

        long created = 0L;
        long requeued = 0L;
        long unchanged = 0L;
        for (final JournalEntryEntity entry : entryList) {
            final QueueAction action = queueForEntry(entry);
            switch (action) {
                case CREATED -> created++;
                case REQUEUED -> requeued++;
                case UNCHANGED -> unchanged++;
                default -> unchanged++;
            }
        }

        return JournalEntryEntityQueueSyncResultDto.builder()
                .activeEntryCount(entryList.size())
                .queueCountBefore(queueListBefore.size())
                .created(created)
                .requeued(requeued)
                .unchanged(unchanged)
                .removed(removed)
                .queueCountAfter(repository.count())
                .build();
    }

    /**
     * Requeue all failed rows for retry.
     *
     * @return number of requeued rows
     */
    @Transactional
    public long requeueFailed() {
        final List<JournalEntryEntityJobEntity> failedJobList = repository.findAllByJobStatus(STATUS_FAILED);
        if (failedJobList.isEmpty()) return 0L;

        failedJobList.forEach(job -> {
            job.setJobStatus(STATUS_PENDING);
            job.setLockedBy(null);
            job.setErrorMessage(null);
        });
        repository.saveAll(failedJobList);
        repository.flush();
        log.info("Requeued failed journal entity jobs. count={}", failedJobList.size());
        return failedJobList.size();
    }

    /**
     * Process one job row by regenerating both entity refs and entity roles.
     *
     * @param job queue row
     * @throws Exception when sync fails
     */
    @Transactional
    public void processJob(final JournalEntryEntityJobEntity job) throws Exception {
        if (job == null || job.getId() == null || job.getJournalEntryId() == null) return;

        final JournalEntryEntity entry = journalEntryRepository.findById(job.getJournalEntryId()).orElse(null);
        if (entry == null) {
            journalEntryEntityRefSyncService.removeByJournalEntryId(job.getJournalEntryId());
            repository.deleteById(job.getId());
            log.info("Journal entry entity queue job removed because source entry disappeared. jobId={}, entryId={}",
                    job.getId(), job.getJournalEntryId());
            return;
        }

        final String contentHash = buildContentHash(entry);
        journalEntryEntityRefSyncService.syncForEntryId(entry.getId());
        markSynced(job.getId(), contentHash);
        log.info("Journal entry entity queue synced entry. jobId={}, entryId={}", job.getId(), entry.getId());
    }

    /**
     * Queue or requeue one loaded journal entry.
     */
    private QueueAction queueForEntry(final JournalEntryEntity entry) {
        if (entry == null || entry.getId() == null) return QueueAction.UNCHANGED;

        final String contentHash = buildContentHash(entry);
        final Optional<JournalEntryEntityJobEntity> existingOpt = repository.findFirstByJournalEntryId(entry.getId());
        final JournalEntryEntityJobEntity job = existingOpt.orElseGet(() -> JournalEntryEntityJobEntity.builder()
                .journalEntryId(entry.getId())
                .build());

        final boolean unchangedSynced = StringUtils.equals(job.getContentHash(), contentHash)
                && (STATUS_SYNCED.equals(job.getJobStatus()) || STATUS_SKIPPED.equals(job.getJobStatus()));
        if (unchangedSynced) {
            log.info("Journal entry entity queue unchanged. entryId={}, status={}", entry.getId(), job.getJobStatus());
            return QueueAction.UNCHANGED;
        }

        final boolean existing = existingOpt.isPresent();
        job.setJournalEntryId(entry.getId());
        job.setJobStatus(STATUS_PENDING);
        job.setLockedBy(null);
        job.setErrorMessage(null);
        repository.saveAndFlush(job);
        log.info("Journal entry entity queue enqueued. entryId={}, existingJob={}, previousStatus={}",
                entry.getId(), existingOpt.isPresent(), existingOpt.map(JournalEntryEntityJobEntity::getJobStatus).orElse(null));
        return existing ? QueueAction.REQUEUED : QueueAction.CREATED;
    }

    /**
     * Build a stable sync hash from the entry fields that feed PERSON/entity extraction.
     */
    private String buildContentHash(final JournalEntryEntity entry) {
        final String raw = String.join("\n",
                StringUtils.defaultString(entry.getContentType()),
                StringUtils.defaultString(entry.getTitle()),
                StringUtils.defaultString(entry.getContent()),
                StringUtils.defaultString(entry.getElseDreamerNm())
        );
        return sha256Hex(raw);
    }

    /**
     * Hash one string to SHA-256 hex.
     */
    private String sha256Hex(final String raw) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            final byte[] bytes = digest.digest(StringUtils.defaultString(raw).getBytes(StandardCharsets.UTF_8));
            final StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (final byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (final Exception e) {
            throw new IllegalStateException("Failed to hash journal entry entity sync source.", e);
        }
    }

    /**
     * Normalize a requested queue batch size.
     */
    private int normalizeBatchSize(final Integer batchSize) {
        if (batchSize == null) return DEFAULT_BATCH_SIZE;
        return Math.max(MIN_BATCH_SIZE, Math.min(MAX_BATCH_SIZE, batchSize));
    }

    /**
     * Convert a partial count into a percentage with two decimal places.
     */
    private double toPercent(final long count, final long total) {
        if (total <= 0) return 0.0D;
        return Math.round(((double) count * 10000.0D) / (double) total) / 100.0D;
    }

    /**
     * Convert an exception into a bounded queue error string.
     */
    private String toErrorMessage(final Exception exception) {
        if (exception == null) return null;
        final String message = exception.getMessage() == null ? exception.toString() : exception.getMessage();
        return StringUtils.abbreviate(message, ERROR_MESSAGE_LIMIT);
    }

    /**
     * Resolve a worker node label for debugging multi-worker queue claims.
     */
    private String resolveNodeName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (final Exception e) {
            return "unknown";
        }
    }

    private enum QueueAction {
        CREATED,
        REQUEUED,
        UNCHANGED
    }
}
