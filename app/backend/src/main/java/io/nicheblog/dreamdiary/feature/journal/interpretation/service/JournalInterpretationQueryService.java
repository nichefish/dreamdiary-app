package io.nicheblog.dreamdiary.feature.journal.interpretation.service;

import io.nicheblog.dreamdiary.feature.attachable._shared.entity.BaseAttachableKey;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.lifecycle.service.LifecycleService;
import io.nicheblog.dreamdiary.feature.attachable.state.StateKey;
import io.nicheblog.dreamdiary.feature.journal._shared.state.JournalState;
import io.nicheblog.dreamdiary.feature.journal.interpretation.entity.JournalInterpretationEntity;
import io.nicheblog.dreamdiary.feature.journal.interpretation.mapstruct.JournalInterpretationMapstruct;
import io.nicheblog.dreamdiary.feature.journal.interpretation.model.JournalInterpretationDto;
import io.nicheblog.dreamdiary.feature.journal.interpretation.repository.jpa.JournalInterpretationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JournalInterpretationQueryService {

    private final JournalInterpretationRepository journalInterpretationRepository;
    private final JournalInterpretationMapstruct journalInterpretationMapstruct;
    private final LifecycleService lifecycleService;

    @Transactional(readOnly = true)
    public Map<String, List<JournalInterpretationDto>> getInterpretationMapByRefs(
            final Collection<BaseAttachableKey> refKeyList,
            final String createdBy
    ) throws Exception {
        final List<BaseAttachableKey> normalizedRefKeyList = this.normalizeRefKeyList(refKeyList, createdBy);
        if (normalizedRefKeyList.isEmpty()) return Map.of();

        final Set<String> allowedRefKeySet = normalizedRefKeyList.stream()
                .map(this::toRefKey)
                .collect(Collectors.toSet());
        final List<JournalInterpretationEntity> interpretationEntityList = this.getInterpretationEntitiesByRefs(normalizedRefKeyList, createdBy);

        final Map<String, List<JournalInterpretationDto>> interpretationMap = new LinkedHashMap<>();
        for (final JournalInterpretationEntity interpretationEntity : interpretationEntityList) {
            if (interpretationEntity == null || interpretationEntity.getRefId() == null || interpretationEntity.getRefContentType() == null) {
                continue;
            }

            final String refKey = toRefKey(interpretationEntity.getRefContentType(), interpretationEntity.getRefId());
            if (!allowedRefKeySet.contains(refKey)) continue;

            interpretationMap.computeIfAbsent(refKey, key -> new ArrayList<>())
                    .add(journalInterpretationMapstruct.toDto(interpretationEntity));
        }
        return interpretationMap;
    }

    @Transactional(readOnly = true)
    public Map<Integer, JournalState> getInterpretationStateMapByRefs(
            final Collection<BaseAttachableKey> refKeyList,
            final String createdBy
    ) {
        final List<BaseAttachableKey> normalizedRefKeyList = this.normalizeRefKeyList(refKeyList, createdBy);
        if (normalizedRefKeyList.isEmpty()) return Map.of();

        final List<JournalInterpretationEntity> interpretationEntityList = this.getInterpretationEntitiesByRefs(normalizedRefKeyList, createdBy);
        if (interpretationEntityList.isEmpty()) return Map.of();

        final Map<Integer, JournalState> interpretationStateMap = new LinkedHashMap<>();
        for (final JournalInterpretationEntity interpretationEntity : interpretationEntityList) {
            if (interpretationEntity == null || interpretationEntity.getId() == null) continue;

            interpretationStateMap.put(
                    interpretationEntity.getId(),
                    JournalState.builder()
                            .collapsed(interpretationEntity.state.hasState(StateKey.COLLAPSED))
                            .build()
            );
        }
        return interpretationStateMap;
    }

    /**
     * 저널 일기 ref 목록에 붙은 해석 라이프사이클 맵을 조회한다.
     *
     * <p>호출자는 보통 해석 ID가 아니라 일기 ref를 알고 있다. 그래서 먼저 해당 ref에 매칭되는
     * 해석 row를 찾고, 그 ID로 라이프사이클 값을 일괄 조회한다.</p>
     *
     * @param refKeyList 해석이 붙어 있을 수 있는 저널 일기 ref 목록
     * @param createdBy 해석 조회 범위를 제한할 작성자
     * @return 해석 ID 기준 라이프사이클 키 맵
     */
    @Transactional(readOnly = true)
    public Map<Integer, String> getInterpretationLifecycleMapByRefs(
            final Collection<BaseAttachableKey> refKeyList,
            final String createdBy
    ) {
        final List<BaseAttachableKey> normalizedRefKeyList = this.normalizeRefKeyList(refKeyList, createdBy);
        if (normalizedRefKeyList.isEmpty()) return Map.of();

        final List<JournalInterpretationEntity> interpretationEntityList = this.getInterpretationEntitiesByRefs(normalizedRefKeyList, createdBy);
        if (interpretationEntityList.isEmpty()) return Map.of();

        final List<Integer> interpretationIdList = interpretationEntityList.stream()
                .filter(Objects::nonNull)
                .map(JournalInterpretationEntity::getId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        return lifecycleService.getLifecycleMap(ContentType.JOURNAL_INTERPRETATION, interpretationIdList);
    }

    private String toRefKey(final BaseAttachableKey refKey) {
        return toRefKey(refKey.getContentTypeEnum(), refKey.getId());
    }

    private String toRefKey(final ContentType refContentType, final Integer refId) {
        return String.format("%s:%d", refContentType.key, refId);
    }

    private List<BaseAttachableKey> normalizeRefKeyList(
            final Collection<BaseAttachableKey> refKeyList,
            final String createdBy
    ) {
        if (refKeyList == null || refKeyList.isEmpty() || createdBy == null || createdBy.isBlank()) {
            return List.of();
        }

        return refKeyList.stream()
                .filter(Objects::nonNull)
                .filter(refKey -> refKey.getId() != null && refKey.getContentTypeEnum() != null)
                .distinct()
                .toList();
    }

    private List<JournalInterpretationEntity> getInterpretationEntitiesByRefs(
            final List<BaseAttachableKey> normalizedRefKeyList,
            final String createdBy
    ) {
        final List<Integer> refIdList = normalizedRefKeyList.stream()
                .map(BaseAttachableKey::getId)
                .distinct()
                .toList();
        final List<ContentType> refContentTypeList = normalizedRefKeyList.stream()
                .map(BaseAttachableKey::getContentTypeEnum)
                .distinct()
                .toList();

        return journalInterpretationRepository.findAllByCreatedByAndRefIdInAndRefContentTypeInOrderByRefContentTypeAscRefIdAscSortOrderAsc(
                createdBy,
                refIdList,
                refContentTypeList
        );
    }
}
