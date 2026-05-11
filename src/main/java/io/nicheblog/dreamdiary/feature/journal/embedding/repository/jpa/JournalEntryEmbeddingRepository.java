package io.nicheblog.dreamdiary.feature.journal.embedding.repository.jpa;

import io.nicheblog.dreamdiary.feature.journal.embedding.entity.JournalEntryEmbeddingEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * 저널 엔트리 임베딩 작업 큐를 조회하고 저장하는 JPA Repository 인터페이스입니다.
 */
@Repository
public interface JournalEntryEmbeddingRepository
        extends BaseStreamRepository<JournalEntryEmbeddingEntity, Integer> {

    /**
     * 원본 저널 엔트리 ID에 해당하는 활성 임베딩 작업을 조회한다.
     *
     * @param journalEntryId 원본 저널 엔트리 ID
     * @return 활성 임베딩 작업 엔티티
     */
    Optional<JournalEntryEmbeddingEntity> findFirstByJournalEntryId(Integer journalEntryId);

    /**
     * 지정한 처리 상태의 임베딩 작업을 생성 순서대로 조회한다.
     *
     * @param embeddingStatus 조회할 임베딩 처리 상태
     * @param pageable 조회 개수와 페이지 조건
     * @return 조건에 맞는 임베딩 작업 엔티티 목록
     */
    List<JournalEntryEmbeddingEntity> findAllByEmbeddingStatusOrderByCreatedAtAscIdAsc(String embeddingStatus, Pageable pageable);

    /**
     * 지정 시각 이전부터 특정 상태에 머문 임베딩 작업을 조회한다.
     *
     * @param embeddingStatus 조회할 임베딩 처리 상태
     * @param updatedAt 오래된 처리 중 상태로 판단할 기준 시각
     * @param pageable 조회 개수와 페이지 조건
     * @return 조건에 맞는 임베딩 작업 엔티티 목록
     */
    List<JournalEntryEmbeddingEntity> findAllByEmbeddingStatusAndUpdatedAtBeforeOrderByUpdatedAtAscIdAsc(
            String embeddingStatus,
            Date updatedAt,
            Pageable pageable
    );

    /**
     * 지정한 처리 상태의 임베딩 작업 전체를 조회한다. 벡터 캐시 초기화에 사용한다.
     *
     * @param embeddingStatus 조회할 임베딩 처리 상태
     * @return 해당 상태의 임베딩 작업 엔티티 전체 목록
     */
    List<JournalEntryEmbeddingEntity> findAllByEmbeddingStatus(String embeddingStatus);

    /**
     * 지정한 처리 상태의 임베딩 작업 건수를 조회한다.
     *
     * @param embeddingStatus 집계할 임베딩 처리 상태
     * @return 해당 상태의 작업 건수
     */
    long countByEmbeddingStatus(String embeddingStatus);

    /**
     * PENDING 상태의 임베딩 작업을 배치 크기만큼 선점하고 행 잠금을 건다.
     *
     * <p>다중 인스턴스 환경에서 중복 처리를 방지하기 위해
     * {@code FOR UPDATE SKIP LOCKED} 를 사용한다.</p>
     *
     * @param batchSize 선점할 최대 작업 개수
     * @return 잠금이 걸린 임베딩 작업 엔티티 목록
     */
    @Query(
        value = "SELECT * FROM journal_entry_embedding" +
                " WHERE embedding_status = 'PENDING' AND deleted_at IS NULL" +
                " ORDER BY created_at ASC, id ASC" +
                " LIMIT :batchSize FOR UPDATE SKIP LOCKED",
        nativeQuery = true
    )
    List<JournalEntryEmbeddingEntity> findAndLockPendingBatch(@Param("batchSize") int batchSize);
}
