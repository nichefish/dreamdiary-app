package io.nicheblog.dreamdiary.feature.clsf.related.service;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.clsf._shared.entity.BaseClsfKey;
import io.nicheblog.dreamdiary.feature.clsf._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.clsf.related.entity.RelatedContentEntity;
import io.nicheblog.dreamdiary.feature.clsf.related.mapstruct.RelatedContentMapstruct;
import io.nicheblog.dreamdiary.feature.clsf.related.model.RelatedContentDto;
import io.nicheblog.dreamdiary.feature.clsf.related.repository.jpa.RelatedContentRepository;
import io.nicheblog.dreamdiary.feature.journal.diary.repository.jpa.JournalDiaryRepository;
import io.nicheblog.dreamdiary.feature.journal.dream.repository.jpa.JournalDreamRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * RelatedContentQueryService
 * <pre>
 *  related content batch query service.
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
public class RelatedContentQueryService {

    private final RelatedContentRepository relatedContentRepository;
    private final RelatedContentMapstruct relatedContentMapstruct;
    private final JournalDiaryRepository journalDiaryRepository;
    private final JournalDreamRepository journalDreamRepository;

    @Transactional(readOnly = true)
    public Map<String, List<RelatedContentDto>> getRelatedContentMapByRefs(
            final Collection<BaseClsfKey> refKeyList,
            final String username
    ) throws Exception {
        final String resolvedUsername = AuthUtils.requireUsername(username);
        final Map<String, BaseClsfKey> refKeyMap = this.toRefKeyMap(refKeyList);
        if (refKeyMap.isEmpty()) return Map.of();

        final Set<Integer> idSet = new LinkedHashSet<>();
        refKeyMap.values().forEach(refKey -> idSet.add(refKey.getId()));
        if (idSet.isEmpty()) return Map.of();

        final List<RelatedContentEntity> entityList = relatedContentRepository.findAllByAnyRefIdIn(idSet, resolvedUsername);
        if (entityList.isEmpty()) return Map.of();

        final Map<String, String> titleMap = this.resolveTitleMap(entityList);
        final Map<String, List<RelatedContentDto>> relatedMap = new LinkedHashMap<>();

        for (final RelatedContentEntity entity : entityList) {
            this.appendRelatedDto(relatedMap, refKeyMap, titleMap, entity.getLeftId(), entity.getLeftContentType(), entity);
            this.appendRelatedDto(relatedMap, refKeyMap, titleMap, entity.getRightId(), entity.getRightContentType(), entity);
        }

        return relatedMap;
    }

    private Map<String, BaseClsfKey> toRefKeyMap(final Collection<BaseClsfKey> refKeyList) {
        final Map<String, BaseClsfKey> refKeyMap = new LinkedHashMap<>();
        if (refKeyList == null) return refKeyMap;

        for (final BaseClsfKey refKey : refKeyList) {
            if (!this.isSupported(refKey)) continue;
            refKeyMap.putIfAbsent(this.toKey(refKey.getContentType(), refKey.getId()), refKey);
        }

        return refKeyMap;
    }

    private void appendRelatedDto(
            final Map<String, List<RelatedContentDto>> relatedMap,
            final Map<String, BaseClsfKey> refKeyMap,
            final Map<String, String> titleMap,
            final Integer refId,
            final String refContentType,
            final RelatedContentEntity entity
    ) throws Exception {
        final String refKey = this.toKey(refContentType, refId);
        final BaseClsfKey matchedRefKey = refKeyMap.get(refKey);
        if (matchedRefKey == null) return;

        final RelatedContentDto dto = relatedContentMapstruct.toDto(entity);
        final BaseClsfKey targetKey = this.resolveTargetKey(entity, matchedRefKey);
        dto.setTargetId(targetKey.getId());
        dto.setTargetContentType(targetKey.getContentType());
        dto.setTargetTitle(titleMap.get(this.toKey(targetKey.getContentType(), targetKey.getId())));

        relatedMap.computeIfAbsent(refKey, key -> new ArrayList<>()).add(dto);
    }

    private BaseClsfKey resolveTargetKey(final RelatedContentEntity entity, final BaseClsfKey refKey) {
        final boolean isLeft = Objects.equals(entity.getLeftId(), refKey.getId())
                && Objects.equals(entity.getLeftContentType(), refKey.getContentType());

        if (isLeft) return new BaseClsfKey(entity.getRightId(), entity.getRightContentType());
        return new BaseClsfKey(entity.getLeftId(), entity.getLeftContentType());
    }

    private Map<String, String> resolveTitleMap(final Collection<RelatedContentEntity> entityList) {
        final Set<Integer> diaryIdSet = new LinkedHashSet<>();
        final Set<Integer> dreamIdSet = new LinkedHashSet<>();

        for (final RelatedContentEntity entity : entityList) {
            this.collectTitleTarget(diaryIdSet, dreamIdSet, entity.getLeftId(), entity.getLeftContentType());
            this.collectTitleTarget(diaryIdSet, dreamIdSet, entity.getRightId(), entity.getRightContentType());
        }

        final Map<String, String> titleMap = new LinkedHashMap<>();
        journalDiaryRepository.findAllById(diaryIdSet).forEach(entity ->
                titleMap.put(this.toKey(ContentType.JOURNAL_DIARY.key, entity.getId()), entity.getTitle())
        );
        journalDreamRepository.findAllById(dreamIdSet).forEach(entity ->
                titleMap.put(this.toKey(ContentType.JOURNAL_DREAM.key, entity.getId()), entity.getTitle())
        );

        return titleMap;
    }

    private void collectTitleTarget(
            final Set<Integer> diaryIdSet,
            final Set<Integer> dreamIdSet,
            final Integer id,
            final String contentType
    ) {
        if (id == null || StringUtils.isBlank(contentType)) return;

        if (Objects.equals(contentType, ContentType.JOURNAL_DIARY.key)) {
            diaryIdSet.add(id);
            return;
        }

        if (Objects.equals(contentType, ContentType.JOURNAL_DREAM.key)) {
            dreamIdSet.add(id);
        }
    }

    private boolean isSupported(final BaseClsfKey refKey) {
        if (refKey == null || refKey.getId() == null || StringUtils.isBlank(refKey.getContentType())) return false;
        return Objects.equals(refKey.getContentType(), ContentType.JOURNAL_DIARY.key)
                || Objects.equals(refKey.getContentType(), ContentType.JOURNAL_DREAM.key);
    }

    private String toKey(final String contentType, final Integer id) {
        return String.format("%s:%d", StringUtils.defaultString(contentType), id);
    }
}
