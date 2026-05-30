package io.nicheblog.dreamdiary.feature.journal.entry.service;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.journal.entry.entity.JournalEntryEntity;
import io.nicheblog.dreamdiary.feature.journal.entry.repository.jpa.JournalEntryRepository;
import io.nicheblog.dreamdiary.infrastructure.cache.util.EhCacheUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class JournalEntryOrderService {

    private final JournalEntryRepository journalEntryRepository;

    /**
     * 챕터 내 다음 정렬 순번을 계산한다.
     *
     * @param journalChapterId 챕터 ID
     * @param contentType 콘텐츠 타입
     * @return 다음 정렬 순번
     */
    @Transactional(readOnly = true)
    public Integer getNextSortOrder(final Integer journalChapterId, final ContentType contentType) {
        if (journalChapterId == null || contentType == null) return 1;
        final Integer lastSortOrder = journalEntryRepository
                .findFirstByJournalChapterIdAndContentTypeOrderBySortOrderDesc(journalChapterId, contentType.key)
                .map(JournalEntryEntity::getSortOrder)
                .orElse(0);
        return lastSortOrder + 1;
    }

    /**
     * 챕터의 정렬 순번을 1부터 연속값으로 재정렬한다.
     *
     * @param journalChapterId 챕터 ID
     * @param contentType 콘텐츠 타입
     * @param detailCacheName 상세 캐시명
     */
    @Transactional
    public void normalizeSortOrder(final Integer journalChapterId, final ContentType contentType, final String detailCacheName) {
        if (journalChapterId == null || contentType == null) return;

        final List<JournalEntryEntity> entityList = new ArrayList<>(
                journalEntryRepository.findAllByJournalChapterIdAndContentTypeOrderBySortOrderAsc(journalChapterId, contentType.key)
        );
        if (entityList.isEmpty()) return;

        int sortOrder = 1;
        for (final JournalEntryEntity entity : entityList) {
            entity.setSortOrder(sortOrder++);
            evictDetailCache(detailCacheName, entity);
        }

        journalEntryRepository.saveAll(entityList);
        journalEntryRepository.flush();
    }

    /**
     * 대상 엔트리를 원하는 순번 위치로 삽입하고 전체 순번을 다시 매긴다.
     *
     * @param journalChapterId 챕터 ID
     * @param id 엔트리 ID
     * @param targetSortOrder 목표 순번
     * @param contentType 콘텐츠 타입
     * @param detailCacheName 상세 캐시명
     */
    @Transactional
    public void insert(
            final Integer journalChapterId,
            final Integer id,
            final Integer targetSortOrder,
            final ContentType contentType,
            final String detailCacheName
    ) {
        if (journalChapterId == null || id == null || contentType == null) return;

        final List<JournalEntryEntity> entityList = new ArrayList<>(
                journalEntryRepository.findAllByJournalChapterIdAndContentTypeOrderBySortOrderAsc(journalChapterId, contentType.key)
        );
        final JournalEntryEntity targetEntity = journalEntryRepository.findByIdAndContentType(id, contentType.key).orElse(null);
        if (targetEntity == null) return;

        entityList.removeIf(entity -> Objects.equals(entity.getId(), id));
        targetEntity.setJournalChapterId(journalChapterId);

        final int maxIdx = entityList.size() + 1;
        final int normalizedIdx = Math.min(targetSortOrder == null ? maxIdx : targetSortOrder, maxIdx);
        final int pos = Math.min(normalizedIdx - 1, entityList.size());
        entityList.add(pos, targetEntity);

        int sortOrder = 1;
        for (final JournalEntryEntity entity : entityList) {
            entity.setSortOrder(sortOrder++);
            evictDetailCache(detailCacheName, entity);
        }

        journalEntryRepository.saveAll(entityList);
        journalEntryRepository.flush();
    }

    /**
     * 같은 챕터 안에서 순번 변경을 수행한다.
     *
     * @param journalChapterId 챕터 ID
     * @param id 엔트리 ID
     * @param targetSortOrder 목표 순번
     * @param contentType 콘텐츠 타입
     * @param detailCacheName 상세 캐시명
     */
    @Transactional
    public void reorderSortOrder(
            final Integer journalChapterId,
            final Integer id,
            final Integer targetSortOrder,
            final ContentType contentType,
            final String detailCacheName
    ) {
        normalizeSortOrder(journalChapterId, contentType, detailCacheName);
        insert(journalChapterId, id, targetSortOrder, contentType, detailCacheName);
    }

    /**
     * 챕터 이동 시 이전/신규 챕터 순번을 각각 정리한다.
     *
     * @param prevJournalChapterId 기존 챕터 ID
     * @param journalChapterId 변경 챕터 ID
     * @param id 엔트리 ID
     * @param targetSortOrder 목표 순번
     * @param contentType 콘텐츠 타입
     * @param detailCacheName 상세 캐시명
     */
    @Transactional
    public void reorderWhenChapterChanged(
            final Integer prevJournalChapterId,
            final Integer journalChapterId,
            final Integer id,
            final Integer targetSortOrder,
            final ContentType contentType,
            final String detailCacheName
    ) {
        normalizeSortOrder(prevJournalChapterId, contentType, detailCacheName);
        insert(journalChapterId, id, targetSortOrder, contentType, detailCacheName);
    }

    /**
     * 상세 캐시 키 규칙에 맞춰 단건 캐시를 제거한다.
     *
     * @param detailCacheName 상세 캐시명
     * @param entity 엔트리 엔티티
     */
    private void evictDetailCache(final String detailCacheName, final JournalEntryEntity entity) {
        if (detailCacheName == null || entity == null || entity.getId() == null || entity.getCreatedBy() == null) return;
        EhCacheUtils.evictUserCacheByKey(detailCacheName, entity.getCreatedBy(), entity.getContentType() + "_" + entity.getId());
    }
}
