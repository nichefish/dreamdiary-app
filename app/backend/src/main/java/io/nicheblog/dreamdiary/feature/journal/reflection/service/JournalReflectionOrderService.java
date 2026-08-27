package io.nicheblog.dreamdiary.feature.journal.reflection.service;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.journal.reflection.entity.JournalReflectionEntity;
import io.nicheblog.dreamdiary.feature.journal.reflection.repository.jpa.JournalReflectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 같은 대상(About-A) 아래 Reflection 형제 순번을 부여·재배치한다.
 *
 * <p>순번 범위는 {@code (refId, refContentType)} 그룹이며 1부터 연속이다.
 * 챕터·일자 소속 순번과 분리한다.</p>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
public class JournalReflectionOrderService {

    private final JournalReflectionRepository journalReflectionRepository;

    /**
     * 같은 대상 아래 다음 정렬 순번을 계산한다.
     *
     * @param refId 대상 엔티티 번호
     * @param refContentType 대상 콘텐츠 타입
     * @return 다음 정렬 순번
     */
    @Transactional(readOnly = true)
    public Integer getNextSortOrder(final Integer refId, final ContentType refContentType) {
        if (refId == null || refContentType == null) return 1;
        final Integer lastSortOrder = journalReflectionRepository
                .findFirstByRefIdAndRefContentTypeOrderBySortOrderDesc(refId, refContentType)
                .map(JournalReflectionEntity::getSortOrder)
                .orElse(0);
        return lastSortOrder + 1;
    }

    /**
     * 같은 대상 아래 정렬 순번을 1부터 연속값으로 재정렬한다.
     *
     * @param refId 대상 엔티티 번호
     * @param refContentType 대상 콘텐츠 타입
     */
    @Transactional
    public void normalizeSortOrder(final Integer refId, final ContentType refContentType) {
        if (refId == null || refContentType == null) return;

        final List<JournalReflectionEntity> entityList = new ArrayList<>(
                journalReflectionRepository.findAllByRefIdAndRefContentTypeOrderBySortOrderAscIdAsc(refId, refContentType)
        );
        if (entityList.isEmpty()) return;

        int sortOrder = 1;
        for (final JournalReflectionEntity entity : entityList) {
            entity.setSortOrder(sortOrder++);
        }
        journalReflectionRepository.saveAll(entityList);
        journalReflectionRepository.flush();
    }

    /**
     * 대상 리플렉션을 원하는 순번 위치로 삽입하고 전체 순번을 다시 매긴다.
     *
     * @param refId 대상 엔티티 번호
     * @param id Reflection ID
     * @param targetSortOrder 목표 순번
     * @param refContentType 대상 콘텐츠 타입
     */
    @Transactional
    public void insert(
            final Integer refId,
            final Integer id,
            final Integer targetSortOrder,
            final ContentType refContentType
    ) {
        if (refId == null || id == null || refContentType == null) return;

        final List<JournalReflectionEntity> entityList = new ArrayList<>(
                journalReflectionRepository.findAllByRefIdAndRefContentTypeOrderBySortOrderAscIdAsc(refId, refContentType)
        );
        final JournalReflectionEntity targetEntity = journalReflectionRepository.findById(id).orElse(null);
        if (targetEntity == null) return;

        entityList.removeIf(entity -> Objects.equals(entity.getId(), id));

        final int maxIdx = entityList.size() + 1;
        final int normalizedIdx = Math.min(targetSortOrder == null ? maxIdx : targetSortOrder, maxIdx);
        final int pos = Math.max(0, Math.min(normalizedIdx - 1, entityList.size()));
        entityList.add(pos, targetEntity);

        int sortOrder = 1;
        for (final JournalReflectionEntity entity : entityList) {
            entity.setSortOrder(sortOrder++);
        }
        journalReflectionRepository.saveAll(entityList);
        journalReflectionRepository.flush();
    }

    /**
     * 같은 대상 안에서 순번 변경을 수행한다.
     *
     * @param refId 대상 엔티티 번호
     * @param id Reflection ID
     * @param targetSortOrder 목표 순번
     * @param refContentType 대상 콘텐츠 타입
     */
    @Transactional
    public void reorderSortOrder(
            final Integer refId,
            final Integer id,
            final Integer targetSortOrder,
            final ContentType refContentType
    ) {
        normalizeSortOrder(refId, refContentType);
        insert(refId, id, targetSortOrder, refContentType);
    }
}
