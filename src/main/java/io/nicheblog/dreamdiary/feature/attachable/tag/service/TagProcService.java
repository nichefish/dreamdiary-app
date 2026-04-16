package io.nicheblog.dreamdiary.feature.attachable.tag.service;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.attachable._shared.entity.BaseAttachableKey;
import io.nicheblog.dreamdiary.feature.attachable.tag.entity.TagContentEntity;
import io.nicheblog.dreamdiary.feature.attachable.tag.entity.TagEntity;
import io.nicheblog.dreamdiary.feature.attachable.tag.handler.JournalTagCacheUpdtWorker;
import io.nicheblog.dreamdiary.feature.attachable.tag.model.TagDto;
import io.nicheblog.dreamdiary.feature.attachable.tag.model.cmpstn.TagCmpstn;
import io.nicheblog.dreamdiary.global.util.TransactionHookUtils;
import io.nicheblog.dreamdiary.infrastructure.cache.util.EhCacheUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * TagProcService
 * <pre>
 *  태그 처리 서비스.
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class TagProcService {

    private final TagService tagService;
    private final TagContentService tagContentService;
    private final JournalTagCacheUpdtWorker journalTagCacheUpdtWorker;

    /**
     * 태그 처리.
     *
     * @param attachableKey 분류 키
     * @param tagCmpstn 태그 조합
     * @param yy 저널 연도
     * @param mnth 저널 월
     */
    @Transactional
    public void process(
            final BaseAttachableKey attachableKey,
            final TagCmpstn tagCmpstn,
            final Integer yy,
            final Integer mnth
    ) throws Exception {
        if (attachableKey == null) return;

        final boolean isJournal = yy != null || mnth != null;
        if (isJournal && (yy == null || mnth == null)) {
            throw new IllegalStateException("yy/mnth must both be set for journal tag process.");
        }

        if (tagCmpstn == null) {
            delExistingTagContents(attachableKey, yy, mnth);
        } else {
            procTags(attachableKey, tagCmpstn, yy, mnth);
        }

        // 비저널 컨텐츠만 일반 분류 캐시 evict
        if (!isJournal) {
            EhCacheUtils.evictCacheByKey("tagContentEntityListByRef", attachableKey.getId() + "_" + attachableKey.getContentType());
        }

        // 고아 태그 정리
        tagService.deleteNoRefTags();
    }

    /**
     * 특정 게시물에 대해 기존 콘텐츠 태그를 모두 삭제합니다.
     *
     * @param attachableKey 컨텐츠 복합키
     * @param yy 저널 년도
     * @param mnth 저널 월
     */
    @Transactional
    public void delExistingTagContents(final BaseAttachableKey attachableKey, final Integer yy, final Integer mnth) throws Exception {
        if (attachableKey == null) return;

        final Map<String, Object> searchParamMap = new HashMap<>();
        searchParamMap.put("refId", attachableKey.getId());
        searchParamMap.put("refContentType", attachableKey.getContentType());

        final List<TagContentEntity> entityList = tagContentService.getListEntity(searchParamMap);
        tagContentService.deleteAll(entityList);

        if (yy != null && mnth != null) {
            final Map<Integer, Integer> tagCntChangeMap = entityList.stream()
                    .collect(Collectors.toMap(TagContentEntity::getTagId, tagId -> -1));
            publishTagCacheUpdateAfterCommit(attachableKey, yy, mnth, tagCntChangeMap);
        }
    }

    /**
     * 태그-컨텐츠 처리.
     * 새로운 태그를 추가하고, 더 이상 필요하지 않은 태그를 삭제합니다.
     *
     * @param attachableKey 컨텐츠 복합키
     * @param tagCmpstn 태그 조합
     * @param yy 저널 년도
     * @param mnth 저널 월
     */
    @Transactional
    public void procTags(
            final BaseAttachableKey attachableKey,
            final TagCmpstn tagCmpstn,
            final Integer yy,
            final Integer mnth
    ) throws Exception {
        if (attachableKey == null || tagCmpstn == null) return;

        final List<TagDto> existingTagList = tagContentService.getTagStrListByAttachableKey(attachableKey);
        final List<TagDto> newTagList = tagCmpstn.getParsedTagList();
        final boolean isSame = newTagList.size() == existingTagList.size() && new HashSet<>(newTagList).containsAll(existingTagList);
        if (isSame) return;

        // 1. 새 마스터 태그 추가
        final Set<TagDto> newTagSet = new HashSet<>(newTagList);
        existingTagList.forEach(newTagSet::remove);
        final List<TagEntity> createdTagList = CollectionUtils.isNotEmpty(newTagSet)
                ? tagService.addMasterTag(new ArrayList<>(newTagSet))
                : new ArrayList<>();

        final Map<Integer, Integer> tagCntChangeMap = new HashMap<>();

        // 2. 사용하지 않는 tag-content 제거
        final Set<TagDto> obsoleteTagSet = new HashSet<>(existingTagList);
        newTagList.forEach(obsoleteTagSet::remove);
        if (CollectionUtils.isNotEmpty(obsoleteTagSet)) {
            tagContentService.delObsoleteTagContents(attachableKey, new ArrayList<>(obsoleteTagSet));
            for (final TagDto tag : obsoleteTagSet) {
                tagCntChangeMap.put(tag.getId(), -1);
            }
        }

        // 3. 새 tag-content 추가
        if (CollectionUtils.isNotEmpty(createdTagList)) {
            final List<TagContentEntity> registeredList = tagContentService.addTagContents(attachableKey, createdTagList);
            for (final TagContentEntity tag : registeredList) {
                tagCntChangeMap.put(tag.getTagId(), 1);
            }
        }

        if (!tagCntChangeMap.isEmpty() && yy != null && mnth != null) {
            publishTagCacheUpdateAfterCommit(attachableKey, yy, mnth, tagCntChangeMap);
        }
    }

    /**
     * 트랜잭션 commit 이후 태그 캐시 갱신 이벤트를 발행한다.
     */
    private void publishTagCacheUpdateAfterCommit(
            final BaseAttachableKey attachableKey,
            final Integer yy,
            final Integer mnth,
            final Map<Integer, Integer> tagCntChangeMap
    ) throws Exception {
        final Object cacheKey = new SimpleKey(AuthUtils.getLgnUsername(), yy, mnth);
        final String contentType = attachableKey.getContentType();
        final Map<Integer, Integer> safeChangeMap = new HashMap<>(tagCntChangeMap);
        TransactionHookUtils.runAfterCommitOrNow(
                () -> journalTagCacheUpdtWorker.handle(contentType, cacheKey, safeChangeMap),
                e -> log.error("Tag cache update failed [{}:{}:{}]: {}", contentType, yy, mnth, e.getMessage(), e)
        );
    }
}

