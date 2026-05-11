package io.nicheblog.dreamdiary.feature.journal.embedding.service;

import io.nicheblog.dreamdiary.feature.journal.embedding.entity.JournalEntryEmbeddingEntity;
import io.nicheblog.dreamdiary.feature.journal.embedding.model.JournalEntryEmbeddingStatsDto;
import io.nicheblog.dreamdiary.feature.journal.embedding.repository.jpa.JournalEntryEmbeddingRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 저널 엔트리 임베딩 작업 큐의 상태 전이를 담당하는 서비스입니다.
 *
 * <p>대기 작업 선점, 오래된 처리 중 작업 재대기, 성공/실패/스킵 마킹,
 * 관리자 진행률 집계를 한곳에서 처리한다.</p>
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class JournalEntryEmbeddingQueueService {

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_PROCESSING = "PROCESSING";
    public static final String STATUS_EMBEDDED = "EMBEDDED";
    public static final String STATUS_FAILED = "FAILED";
    public static final String STATUS_SKIPPED = "SKIPPED";

    private static final int MIN_BATCH_SIZE = 1;
    private static final int MAX_BATCH_SIZE = 100;
    private static final int ERROR_MESSAGE_LIMIT = 4000;

    @Getter
    private final JournalEntryEmbeddingRepository repository;

    /**
     * 대기 중인 작업을 배치 크기만큼 선점하고 처리 중 상태로 변경한다.
     *
     * @param batchSize 선점할 최대 작업 개수
     * @return 처리 중 상태로 변경된 임베딩 작업 목록
     */
    @Transactional
    public List<JournalEntryEmbeddingEntity> claimPendingBatch(final Integer batchSize) {
        final int normalizedBatchSize = normalizeBatchSize(batchSize);
        final List<JournalEntryEmbeddingEntity> entityList = repository.findAllByEmbeddingStatusOrderByCreatedAtAscIdAsc(
                STATUS_PENDING,
                PageRequest.of(0, normalizedBatchSize)
        );

        entityList.forEach(entity -> {
            entity.setEmbeddingStatus(STATUS_PROCESSING);
            entity.setErrorMessage(null);
        });
        repository.saveAll(entityList);
        repository.flush();

        return entityList;
    }

    /**
     * 일정 시간 이상 처리 중 상태에 머문 작업을 다시 대기 상태로 되돌린다.
     *
     * @param staleBefore 오래된 처리 중 작업으로 판단할 기준 시각
     * @param batchSize 한 번에 재대기시킬 최대 작업 개수
     * @return 재대기 처리한 작업 건수
     */
    @Transactional
    public int requeueStaleProcessing(final Date staleBefore, final Integer batchSize) {
        final int normalizedBatchSize = normalizeBatchSize(batchSize);
        final List<JournalEntryEmbeddingEntity> entityList =
                repository.findAllByEmbeddingStatusAndUpdatedAtBeforeOrderByUpdatedAtAscIdAsc(
                        STATUS_PROCESSING,
                        staleBefore,
                        PageRequest.of(0, normalizedBatchSize)
                );

        entityList.forEach(entity -> {
            entity.setEmbeddingStatus(STATUS_PENDING);
            entity.setErrorMessage("Requeued stale PROCESSING row.");
        });
        repository.saveAll(entityList);

        if (!entityList.isEmpty()) {
            log.info("Requeued {} stale journal entry embedding rows.", entityList.size());
        }

        return entityList.size();
    }

    /**
     * 임베딩 벡터 생성에 성공한 작업을 완료 상태로 마킹한다.
     *
     * @param id 임베딩 작업 ID
     * @param embeddingModel 벡터 생성에 사용한 모델명
     * @param embeddingVectorJson 생성된 벡터 JSON 배열 문자열
     */
    @Transactional
    public void markEmbedded(final Integer id, final String embeddingModel, final String embeddingVectorJson) {
        repository.findById(id).ifPresent(entity -> {
            entity.setEmbeddingStatus(STATUS_EMBEDDED);
            entity.setEmbeddingModel(embeddingModel);
            entity.setEmbeddingVectorJson(embeddingVectorJson);
            entity.setEmbeddedAt(new Date());
            entity.setErrorMessage(null);
        });
    }

    /**
     * 처리 중 예외가 발생한 임베딩 작업을 실패 상태로 마킹한다.
     *
     * @param id 임베딩 작업 ID
     * @param exception 실패 원인이 된 예외
     */
    @Transactional
    public void markFailed(final Integer id, final Exception exception) {
        repository.findById(id).ifPresent(entity -> {
            entity.setEmbeddingStatus(STATUS_FAILED);
            entity.setErrorMessage(toErrorMessage(exception));
        });
    }

    /**
     * 지정한 작업들을 다시 대기 상태로 되돌린다.
     *
     * @param idList 재대기시킬 임베딩 작업 ID 목록
     * @param reason 재대기 사유
     */
    @Transactional
    public void requeueByIds(final List<Integer> idList, final String reason) {
        if (idList == null || idList.isEmpty()) return;

        final List<JournalEntryEmbeddingEntity> entityList = repository.findAllById(idList);
        entityList.forEach(entity -> {
            entity.setEmbeddingStatus(STATUS_PENDING);
            entity.setErrorMessage(StringUtils.abbreviate(reason, ERROR_MESSAGE_LIMIT));
        });
    }

    /**
     * 임베딩할 수 없는 작업을 스킵 상태로 마킹한다.
     *
     * @param id 임베딩 작업 ID
     * @param reason 스킵 사유
     */
    @Transactional
    public void markSkipped(final Integer id, final String reason) {
        repository.findById(id).ifPresent(entity -> {
            entity.setEmbeddingStatus(STATUS_SKIPPED);
            entity.setErrorMessage(StringUtils.abbreviate(reason, ERROR_MESSAGE_LIMIT));
        });
    }

    /**
     * 대기 중인 임베딩 작업 건수를 조회한다.
     *
     * @return 대기 중인 작업 건수
     */
    @Transactional(readOnly = true)
    public long countPending() {
        return repository.countByEmbeddingStatus(STATUS_PENDING);
    }

    /**
     * 관리자 화면에 표시할 임베딩 작업 통계를 집계한다.
     *
     * @return 임베딩 작업 상태별 건수와 진행률 DTO
     */
    @Transactional(readOnly = true)
    public JournalEntryEmbeddingStatsDto getStats() {
        final long total = repository.count();
        final long pending = repository.countByEmbeddingStatus(STATUS_PENDING);
        final long processing = repository.countByEmbeddingStatus(STATUS_PROCESSING);
        final long embedded = repository.countByEmbeddingStatus(STATUS_EMBEDDED);
        final long failed = repository.countByEmbeddingStatus(STATUS_FAILED);
        final long skipped = repository.countByEmbeddingStatus(STATUS_SKIPPED);
        final long remaining = pending + processing;
        final long completed = embedded + failed + skipped;

        return JournalEntryEmbeddingStatsDto.builder()
                .total(total)
                .pending(pending)
                .processing(processing)
                .embedded(embedded)
                .failed(failed)
                .skipped(skipped)
                .remaining(remaining)
                .completed(completed)
                .completionRate(toPercent(completed, total))
                .vectorizedRate(toPercent(embedded, total))
                .build();
    }

    /**
     * 배치 크기를 허용 범위 안으로 보정한다.
     *
     * @param batchSize 요청된 배치 크기
     * @return 보정된 배치 크기
     */
    private int normalizeBatchSize(final Integer batchSize) {
        if (batchSize == null) return 20;
        return Math.max(MIN_BATCH_SIZE, Math.min(MAX_BATCH_SIZE, batchSize));
    }

    /**
     * 예외 메시지를 DB에 저장 가능한 길이로 축약한다.
     *
     * @param exception 실패 원인이 된 예외
     * @return 축약된 예외 메시지
     */
    private String toErrorMessage(final Exception exception) {
        if (exception == null) return null;
        final String message = exception.getMessage() == null ? exception.toString() : exception.getMessage();
        return StringUtils.abbreviate(message, ERROR_MESSAGE_LIMIT);
    }

    /**
     * 부분 건수를 전체 건수 기준 백분율로 변환한다.
     *
     * @param count 부분 건수
     * @param total 전체 건수
     * @return 소수점 둘째 자리까지 반올림한 백분율
     */
    private double toPercent(final long count, final long total) {
        if (total <= 0) return 0.0D;
        return Math.round(((double) count * 10000.0D) / (double) total) / 100.0D;
    }
}
