package io.nicheblog.dreamdiary.feature.clsf.related.service;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.clsf._shared.entity.BaseClsfKey;
import io.nicheblog.dreamdiary.feature.clsf._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.clsf.related.entity.RelatedContentEntity;
import io.nicheblog.dreamdiary.feature.clsf.related.mapstruct.RelatedContentMapstruct;
import io.nicheblog.dreamdiary.feature.clsf.related.model.RelatedContentDto;
import io.nicheblog.dreamdiary.feature.clsf.related.repository.jpa.RelatedContentRepository;
import io.nicheblog.dreamdiary.feature.jrnl.diary.repository.jpa.JrnlDiaryRepository;
import io.nicheblog.dreamdiary.feature.jrnl.dream.repository.jpa.JrnlDreamRepository;
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
    private final JrnlDiaryRepository jrnlDiaryRepository;
    private final JrnlDreamRepository jrnlDreamRepository;

    @Transactional(readOnly = true)
    public Map<String, List<RelatedContentDto>> getRelatedContentMapByRefs(
            final Collection<BaseClsfKey> refKeyList,
            final String username
    ) throws Exception {
        final String resolvedUsername = AuthUtils.requireUsername(username);
        final Map<String, BaseClsfKey> refKeyMap = this.toRefKeyMap(refKeyList);
        if (refKeyMap.isEmpty()) return Map.of();

        final Set<Integer> postNoSet = new LinkedHashSet<>();
        refKeyMap.values().forEach(refKey -> postNoSet.add(refKey.getPostNo()));
        if (postNoSet.isEmpty()) return Map.of();

        final List<RelatedContentEntity> entityList = relatedContentRepository.findAllByAnyRefPostNoIn(postNoSet, resolvedUsername);
        if (entityList.isEmpty()) return Map.of();

        final Map<String, String> titleMap = this.resolveTitleMap(entityList);
        final Map<String, List<RelatedContentDto>> relatedMap = new LinkedHashMap<>();

        for (final RelatedContentEntity entity : entityList) {
            this.appendRelatedDto(relatedMap, refKeyMap, titleMap, entity.getLeftPostNo(), entity.getLeftContentType(), entity);
            this.appendRelatedDto(relatedMap, refKeyMap, titleMap, entity.getRightPostNo(), entity.getRightContentType(), entity);
        }

        return relatedMap;
    }

    private Map<String, BaseClsfKey> toRefKeyMap(final Collection<BaseClsfKey> refKeyList) {
        final Map<String, BaseClsfKey> refKeyMap = new LinkedHashMap<>();
        if (refKeyList == null) return refKeyMap;

        for (final BaseClsfKey refKey : refKeyList) {
            if (!this.isSupported(refKey)) continue;
            refKeyMap.putIfAbsent(this.toKey(refKey.getContentType(), refKey.getPostNo()), refKey);
        }

        return refKeyMap;
    }

    private void appendRelatedDto(
            final Map<String, List<RelatedContentDto>> relatedMap,
            final Map<String, BaseClsfKey> refKeyMap,
            final Map<String, String> titleMap,
            final Integer refPostNo,
            final String refContentType,
            final RelatedContentEntity entity
    ) throws Exception {
        final String refKey = this.toKey(refContentType, refPostNo);
        final BaseClsfKey matchedRefKey = refKeyMap.get(refKey);
        if (matchedRefKey == null) return;

        final RelatedContentDto dto = relatedContentMapstruct.toDto(entity);
        final BaseClsfKey targetKey = this.resolveTargetKey(entity, matchedRefKey);
        dto.setTargetPostNo(targetKey.getPostNo());
        dto.setTargetContentType(targetKey.getContentType());
        dto.setTargetTitle(titleMap.get(this.toKey(targetKey.getContentType(), targetKey.getPostNo())));

        relatedMap.computeIfAbsent(refKey, key -> new ArrayList<>()).add(dto);
    }

    private BaseClsfKey resolveTargetKey(final RelatedContentEntity entity, final BaseClsfKey refKey) {
        final boolean isLeft = Objects.equals(entity.getLeftPostNo(), refKey.getPostNo())
                && Objects.equals(entity.getLeftContentType(), refKey.getContentType());

        if (isLeft) return new BaseClsfKey(entity.getRightPostNo(), entity.getRightContentType());
        return new BaseClsfKey(entity.getLeftPostNo(), entity.getLeftContentType());
    }

    private Map<String, String> resolveTitleMap(final Collection<RelatedContentEntity> entityList) {
        final Set<Integer> diaryPostNoSet = new LinkedHashSet<>();
        final Set<Integer> dreamPostNoSet = new LinkedHashSet<>();

        for (final RelatedContentEntity entity : entityList) {
            this.collectTitleTarget(diaryPostNoSet, dreamPostNoSet, entity.getLeftPostNo(), entity.getLeftContentType());
            this.collectTitleTarget(diaryPostNoSet, dreamPostNoSet, entity.getRightPostNo(), entity.getRightContentType());
        }

        final Map<String, String> titleMap = new LinkedHashMap<>();
        jrnlDiaryRepository.findAllById(diaryPostNoSet).forEach(entity ->
                titleMap.put(this.toKey(ContentType.JRNL_DIARY.key, entity.getPostNo()), entity.getTitle())
        );
        jrnlDreamRepository.findAllById(dreamPostNoSet).forEach(entity ->
                titleMap.put(this.toKey(ContentType.JRNL_DREAM.key, entity.getPostNo()), entity.getTitle())
        );

        return titleMap;
    }

    private void collectTitleTarget(
            final Set<Integer> diaryPostNoSet,
            final Set<Integer> dreamPostNoSet,
            final Integer postNo,
            final String contentType
    ) {
        if (postNo == null || StringUtils.isBlank(contentType)) return;

        if (Objects.equals(contentType, ContentType.JRNL_DIARY.key)) {
            diaryPostNoSet.add(postNo);
            return;
        }

        if (Objects.equals(contentType, ContentType.JRNL_DREAM.key)) {
            dreamPostNoSet.add(postNo);
        }
    }

    private boolean isSupported(final BaseClsfKey refKey) {
        if (refKey == null || refKey.getPostNo() == null || StringUtils.isBlank(refKey.getContentType())) return false;
        return Objects.equals(refKey.getContentType(), ContentType.JRNL_DIARY.key)
                || Objects.equals(refKey.getContentType(), ContentType.JRNL_DREAM.key);
    }

    private String toKey(final String contentType, final Integer postNo) {
        return String.format("%s:%d", StringUtils.defaultString(contentType), postNo);
    }
}
