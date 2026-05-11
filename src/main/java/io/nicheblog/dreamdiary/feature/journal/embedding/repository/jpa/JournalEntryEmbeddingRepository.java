package io.nicheblog.dreamdiary.feature.journal.embedding.repository.jpa;

import io.nicheblog.dreamdiary.feature.journal.embedding.entity.JournalEntryEmbeddingEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * 저널 엔트리 임베딩 작업 큐를 조회하고 저장하는 JPA Repository 인터페이스입니다.
 */
@Repository
public interface JournalEntryEmbeddingRepository
        extends BaseStreamRepository<JournalEntryEmbeddingEntity, Integer> {

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
     * 지정한 처리 상태의 임베딩 작업 건수를 조회한다.
     *
     * @param embeddingStatus 집계할 임베딩 처리 상태
     * @return 해당 상태의 작업 건수
     */
    long countByEmbeddingStatus(String embeddingStatus);
}
