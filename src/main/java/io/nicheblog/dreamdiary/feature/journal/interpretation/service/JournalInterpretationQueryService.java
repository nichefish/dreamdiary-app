package io.nicheblog.dreamdiary.feature.journal.interpretation.service;

import io.nicheblog.dreamdiary.feature.attachable._shared.entity.BaseAttachableKey;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
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
                            .resolved(interpretationEntity.state.hasState(StateKey.RESOLVED))
                            .collapsed(interpretationEntity.state.hasState(StateKey.COLLAPSED))
                            .build()
            );
        }
        return interpretationStateMap;
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
