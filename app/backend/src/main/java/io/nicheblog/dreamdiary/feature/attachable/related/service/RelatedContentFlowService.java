package io.nicheblog.dreamdiary.feature.attachable.related.service;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.attachable._shared.entity.BaseAttachableKey;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.related.entity.RelatedContentEntity;
import io.nicheblog.dreamdiary.feature.attachable.related.mapstruct.RelatedContentMapstruct;
import io.nicheblog.dreamdiary.feature.attachable.related.model.RelatedContentDto;
import io.nicheblog.dreamdiary.feature.attachable.related.model.RelatedContentFlowDto;
import io.nicheblog.dreamdiary.feature.attachable.related.model.RelatedContentFlowEntryDto;
import io.nicheblog.dreamdiary.feature.attachable.related.model.RelatedContentFlowSummaryDto;
import io.nicheblog.dreamdiary.feature.attachable.related.repository.jpa.RelatedContentRepository;
import io.nicheblog.dreamdiary.feature.attachable.related.type.RelationType;
import io.nicheblog.dreamdiary.feature.journal.entry.entity.JournalEntryEntity;
import io.nicheblog.dreamdiary.feature.journal.entry.mapstruct.JournalEntryMapstruct;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryDto;
import io.nicheblog.dreamdiary.feature.journal.entry.repository.jpa.JournalEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * RelatedContentFlowService
 * <pre>
 *  무방향 FLOW 관계를 따라 앵커 엔트리의 연결 컴포넌트를 조회한다.
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class RelatedContentFlowService {

    private final RelatedContentRepository relatedContentRepository;
    private final RelatedContentMapstruct relatedContentMapstruct;
    private final RelatedContentService relatedContentService;
    private final JournalEntryRepository journalEntryRepository;
    private final JournalEntryMapstruct journalEntryMapstruct;

    /**
     * 앵커 엔트리에서 FLOW 간선만 탐색해 연결 컴포넌트를 시간순으로 반환한다.
     *
     * @param contentType 앵커 콘텐츠 타입
     * @param id 앵커 엔트리 ID
     * @return FLOW 연결 컴포넌트
     * @throws Exception DTO 변환 중 예외
     */
    @Transactional(readOnly = true)
    public RelatedContentFlowDto getFlow(final ContentType contentType, final Integer id) throws Exception {
        final BaseAttachableKey anchorKey = new BaseAttachableKey(id, contentType);
        final String createdBy = relatedContentService.requireOwnedContent(anchorKey);
        final String requiredCreatedBy = AuthUtils.requireUsername(createdBy);

        final List<RelatedContentEntity> flowRelations = relatedContentRepository
                .findAllByRelationTypeAndCreatedByOrderByCreatedAtAsc(RelationType.FLOW.key, requiredCreatedBy);
        final Set<BaseAttachableKey> componentKeys = this.traverse(anchorKey, flowRelations);
        final Map<BaseAttachableKey, JournalEntryEntity> entryMap = this.resolveOwnedEntries(componentKeys, requiredCreatedBy);
        final List<RelatedContentFlowEntryDto> entryList = this.toSortedEntryList(anchorKey, entryMap);
        final List<RelatedContentDto> relationList = this.toComponentRelationList(entryMap.keySet(), flowRelations);

        log.info(
                "FLOW traversal completed. anchorType={}, anchorId={}, entryCount={}, relationCount={}",
                contentType.key,
                id,
                entryList.size(),
                relationList.size()
        );

        return RelatedContentFlowDto.builder()
                .anchorId(id)
                .anchorContentType(contentType.key)
                .entryList(entryList)
                .relationList(relationList)
                .build();
    }

    /**
     * 목록에 포함된 앵커들의 전체 FLOW 연결 컴포넌트 요약을 일괄 계산한다.
     * 엔트리별 FLOW 조회를 반복하지 않고 사용자 FLOW 그래프와 관련 엔트리를 각각 배치 조회한다.
     *
     * @param refKeyList 요약이 필요한 엔트리 키
     * @param createdBy 등록자 아이디
     * @return 앵커 키별 FLOW 연결 컴포넌트 요약
     * @throws Exception 엔트리 DTO 변환 중 예외
     */
    @Transactional(readOnly = true)
    public Map<BaseAttachableKey, RelatedContentFlowSummaryDto> getFlowSummaryMap(
            final Collection<BaseAttachableKey> refKeyList,
            final String createdBy
    ) throws Exception {
        final String requiredCreatedBy = AuthUtils.requireUsername(createdBy);
        final Set<BaseAttachableKey> anchorKeySet = new LinkedHashSet<>();
        if (refKeyList != null) {
            refKeyList.stream()
                    .filter(this::isJournalEntryKey)
                    .forEach(anchorKeySet::add);
        }
        if (anchorKeySet.isEmpty()) return Map.of();

        final List<RelatedContentEntity> flowRelations = relatedContentRepository
                .findAllByRelationTypeAndCreatedByOrderByCreatedAtAsc(RelationType.FLOW.key, requiredCreatedBy);
        if (flowRelations.isEmpty()) return Map.of();

        final Map<BaseAttachableKey, Set<BaseAttachableKey>> adjacencyMap = this.toAdjacencyMap(flowRelations);
        final Map<BaseAttachableKey, Set<BaseAttachableKey>> componentByKey = new LinkedHashMap<>();
        final List<Set<BaseAttachableKey>> componentList = new ArrayList<>();
        for (final BaseAttachableKey anchorKey : anchorKeySet) {
            if (!adjacencyMap.containsKey(anchorKey) || componentByKey.containsKey(anchorKey)) continue;
            final Set<BaseAttachableKey> componentKeys = this.traverse(anchorKey, adjacencyMap);
            componentList.add(componentKeys);
            componentKeys.forEach(key -> componentByKey.put(key, componentKeys));
        }
        if (componentList.isEmpty()) return Map.of();

        final Set<BaseAttachableKey> relevantKeySet = new LinkedHashSet<>();
        componentList.forEach(relevantKeySet::addAll);
        final Map<BaseAttachableKey, JournalEntryEntity> entryMap =
                this.resolveOwnedEntries(relevantKeySet, requiredCreatedBy);
        final Map<BaseAttachableKey, RelatedContentFlowSummaryDto> summaryMap = new LinkedHashMap<>();

        for (final Set<BaseAttachableKey> componentKeys : componentList) {
            final Set<BaseAttachableKey> resolvedKeySet = componentKeys.stream()
                    .filter(entryMap::containsKey)
                    .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
            final int relationCount = (int) flowRelations.stream()
                    .filter(relation -> resolvedKeySet.contains(this.leftKey(relation)))
                    .filter(relation -> resolvedKeySet.contains(this.rightKey(relation)))
                    .count();
            if (resolvedKeySet.size() < 2 || relationCount == 0) continue;

            final List<String> dateList = new ArrayList<>();
            for (final BaseAttachableKey key : resolvedKeySet) {
                final JournalEntryDto dto = journalEntryMapstruct.toDto(entryMap.get(key));
                if (dto.getStdrdDt() != null && !dto.getStdrdDt().isBlank()) dateList.add(dto.getStdrdDt());
            }
            dateList.sort(String::compareTo);
            final RelatedContentFlowSummaryDto summary = RelatedContentFlowSummaryDto.builder()
                    .entryCount(resolvedKeySet.size())
                    .relationCount(relationCount)
                    .startStdrdDt(dateList.isEmpty() ? null : dateList.get(0))
                    .endStdrdDt(dateList.isEmpty() ? null : dateList.get(dateList.size() - 1))
                    .build();
            resolvedKeySet.stream()
                    .filter(anchorKeySet::contains)
                    .forEach(key -> summaryMap.put(key, summary));
        }

        log.info(
                "FLOW summary batch completed. requestedCount={}, componentCount={}, summarizedCount={}",
                anchorKeySet.size(),
                componentList.size(),
                summaryMap.size()
        );
        return summaryMap;
    }

    private Set<BaseAttachableKey> traverse(
            final BaseAttachableKey anchorKey,
            final List<RelatedContentEntity> relationList
    ) {
        return this.traverse(anchorKey, this.toAdjacencyMap(relationList));
    }

    private Map<BaseAttachableKey, Set<BaseAttachableKey>> toAdjacencyMap(
            final List<RelatedContentEntity> relationList
    ) {
        final Map<BaseAttachableKey, Set<BaseAttachableKey>> adjacencyMap = new LinkedHashMap<>();
        for (final RelatedContentEntity relation : relationList) {
            final BaseAttachableKey leftKey = this.leftKey(relation);
            final BaseAttachableKey rightKey = this.rightKey(relation);
            adjacencyMap.computeIfAbsent(leftKey, key -> new LinkedHashSet<>()).add(rightKey);
            adjacencyMap.computeIfAbsent(rightKey, key -> new LinkedHashSet<>()).add(leftKey);
        }
        return adjacencyMap;
    }

    private Set<BaseAttachableKey> traverse(
            final BaseAttachableKey anchorKey,
            final Map<BaseAttachableKey, Set<BaseAttachableKey>> adjacencyMap
    ) {
        final Set<BaseAttachableKey> visited = new LinkedHashSet<>();
        final Deque<BaseAttachableKey> queue = new ArrayDeque<>();
        queue.add(anchorKey);

        while (!queue.isEmpty()) {
            final BaseAttachableKey current = queue.removeFirst();
            if (!visited.add(current)) continue;
            adjacencyMap.getOrDefault(current, Set.of()).stream()
                    .filter(key -> !visited.contains(key))
                    .forEach(queue::addLast);
        }
        return visited;
    }

    private boolean isJournalEntryKey(final BaseAttachableKey key) {
        if (key == null || key.getId() == null) return false;
        return Objects.equals(key.getContentType(), ContentType.JOURNAL_DIARY.key)
                || Objects.equals(key.getContentType(), ContentType.JOURNAL_DREAM.key);
    }

    private Map<BaseAttachableKey, JournalEntryEntity> resolveOwnedEntries(
            final Set<BaseAttachableKey> componentKeys,
            final String createdBy
    ) {
        final Set<Integer> idSet = new LinkedHashSet<>();
        final Set<String> contentTypeSet = new LinkedHashSet<>();
        componentKeys.forEach(key -> {
            idSet.add(key.getId());
            contentTypeSet.add(key.getContentType());
        });

        final Map<BaseAttachableKey, JournalEntryEntity> entryMap = new LinkedHashMap<>();
        journalEntryRepository.findAllByIdInAndContentTypeIn(idSet, contentTypeSet).forEach(entity -> {
            final BaseAttachableKey key = new BaseAttachableKey(entity.getId(), entity.getContentType());
            if (!componentKeys.contains(key)) return;
            if (!Objects.equals(entity.getCreatedBy(), createdBy)) {
                log.warn(
                        "FLOW endpoint owner mismatch skipped. contentType={}, id={}",
                        entity.getContentType(),
                        entity.getId()
                );
                return;
            }
            entryMap.put(key, entity);
        });

        if (entryMap.size() != componentKeys.size()) {
            log.warn(
                    "FLOW component contains missing endpoints. requestedCount={}, resolvedCount={}",
                    componentKeys.size(),
                    entryMap.size()
            );
        }
        return entryMap;
    }

    private List<RelatedContentFlowEntryDto> toSortedEntryList(
            final BaseAttachableKey anchorKey,
            final Map<BaseAttachableKey, JournalEntryEntity> entryMap
    ) throws Exception {
        final List<RelatedContentFlowEntryDto> entryList = new ArrayList<>();
        for (final Map.Entry<BaseAttachableKey, JournalEntryEntity> entry : entryMap.entrySet()) {
            final JournalEntryEntity entity = entry.getValue();
            final JournalEntryDto dto = journalEntryMapstruct.toDto(entity);
            entryList.add(RelatedContentFlowEntryDto.builder()
                    .id(dto.getId())
                    .contentType(dto.getContentType())
                    .title(dto.getTitle())
                    .content(dto.getContent())
                    .markdownContent(dto.getMarkdownContent())
                    .stdrdDt(dto.getStdrdDt())
                    .journalDayId(dto.getJournalDayId())
                    .journalChapterId(dto.getJournalChapterId())
                    .chapterSortOrder(entity.getJournalChapter() != null ? entity.getJournalChapter().getSortOrder() : null)
                    .sortOrder(dto.getSortOrder())
                    .anchor(Objects.equals(entry.getKey(), anchorKey))
                    .build());
        }
        entryList.sort(this.flowEntryComparator());
        return entryList;
    }

    private List<RelatedContentDto> toComponentRelationList(
            final Set<BaseAttachableKey> resolvedKeys,
            final List<RelatedContentEntity> flowRelations
    ) throws Exception {
        final List<RelatedContentDto> relationList = new ArrayList<>();
        for (final RelatedContentEntity relation : flowRelations) {
            if (!resolvedKeys.contains(this.leftKey(relation)) || !resolvedKeys.contains(this.rightKey(relation))) continue;
            relationList.add(relatedContentMapstruct.toDto(relation));
        }
        return relationList;
    }

    private Comparator<RelatedContentFlowEntryDto> flowEntryComparator() {
        return Comparator
                .comparing(RelatedContentFlowEntryDto::getStdrdDt, Comparator.nullsLast(String::compareTo))
                .thenComparing(RelatedContentFlowEntryDto::getChapterSortOrder, Comparator.nullsLast(Integer::compareTo))
                .thenComparingInt(entry -> this.contentTypeOrder(entry.getContentType()))
                .thenComparing(RelatedContentFlowEntryDto::getSortOrder, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(RelatedContentFlowEntryDto::getId, Comparator.nullsLast(Integer::compareTo));
    }

    private int contentTypeOrder(final String contentType) {
        if (Objects.equals(contentType, ContentType.JOURNAL_DIARY.key)) return 0;
        if (Objects.equals(contentType, ContentType.JOURNAL_DREAM.key)) return 1;
        return 2;
    }

    private BaseAttachableKey leftKey(final RelatedContentEntity relation) {
        return new BaseAttachableKey(relation.getLeftId(), relation.getLeftContentType());
    }

    private BaseAttachableKey rightKey(final RelatedContentEntity relation) {
        return new BaseAttachableKey(relation.getRightId(), relation.getRightContentType());
    }
}
