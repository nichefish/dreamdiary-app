package io.nicheblog.dreamdiary.feature.clsf.tag.service;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.clsf._shared.entity.BaseClsfKey;
import io.nicheblog.dreamdiary.feature.clsf.tag.entity.TagContentEntity;
import io.nicheblog.dreamdiary.feature.clsf.tag.entity.TagEntity;
import io.nicheblog.dreamdiary.feature.clsf.tag.handler.JrnlTagCacheUpdtWorker;
import io.nicheblog.dreamdiary.feature.clsf.tag.model.TagDto;
import io.nicheblog.dreamdiary.feature.clsf.tag.model.cmpstn.TagCmpstn;
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
    private final JrnlTagCacheUpdtWorker jrnlTagCacheUpdtWorker;

    /**
     * 태그 처리.
     *
     * @param clsfKey 분류 키
     * @param tagCmpstn 태그 조합
     * @param yy 저널 연도
     * @param mnth 저널 월
     */
    @Transactional
    public void process(
            final BaseClsfKey clsfKey,
            final TagCmpstn tagCmpstn,
            final Integer yy,
            final Integer mnth
    ) throws Exception {
        if (clsfKey == null) return;

        final boolean isJrnl = yy != null || mnth != null;
        if (isJrnl && (yy == null || mnth == null)) {
            throw new IllegalStateException("yy/mnth must both be set for jrnl tag process.");
        }

        if (tagCmpstn == null) {
            delExistingTagContents(clsfKey, yy, mnth);
        } else {
            procTags(clsfKey, tagCmpstn, yy, mnth);
        }

        // 비저널 컨텐츠만 일반 분류 캐시 evict
        if (!isJrnl) {
            EhCacheUtils.evictCacheByKey("tagContentEntityListByRef", clsfKey.getPostNo() + "_" + clsfKey.getContentType());
        }

        // 고아 태그 정리
        tagService.deleteNoRefTags();
    }

    /**
     * 특정 게시물에 대해 기존 콘텐츠 태그를 모두 삭제합니다.
     *
     * @param clsfKey 컨텐츠 복합키
     * @param yy 저널 년도
     * @param mnth 저널 월
     */
    @Transactional
    public void delExistingTagContents(final BaseClsfKey clsfKey, final Integer yy, final Integer mnth) throws Exception {
        if (clsfKey == null) return;

        final Map<String, Object> searchParamMap = new HashMap<>();
        searchParamMap.put("refPostNo", clsfKey.getPostNo());
        searchParamMap.put("refContentType", clsfKey.getContentType());

        final List<TagContentEntity> entityList = tagContentService.getListEntity(searchParamMap);
        tagContentService.deleteAll(entityList);

        if (yy != null && mnth != null) {
            final Map<Integer, Integer> tagCntChangeMap = entityList.stream()
                    .collect(Collectors.toMap(TagContentEntity::getTagId, tagId -> -1));
            publishTagCacheUpdateAfterCommit(clsfKey, yy, mnth, tagCntChangeMap);
        }
    }

    /**
     * 태그-컨텐츠 처리.
     * 새로운 태그를 추가하고, 더 이상 필요하지 않은 태그를 삭제합니다.
     *
     * @param clsfKey 컨텐츠 복합키
     * @param tagCmpstn 태그 조합
     * @param yy 저널 년도
     * @param mnth 저널 월
     */
    @Transactional
    public void procTags(
            final BaseClsfKey clsfKey,
            final TagCmpstn tagCmpstn,
            final Integer yy,
            final Integer mnth
    ) throws Exception {
        if (clsfKey == null || tagCmpstn == null) return;

        final List<TagDto> existingTagList = tagContentService.getTagStrListByClsfKey(clsfKey);
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
            tagContentService.delObsoleteTagContents(clsfKey, new ArrayList<>(obsoleteTagSet));
            for (final TagDto tag : obsoleteTagSet) {
                tagCntChangeMap.put(tag.getId(), -1);
            }
        }

        // 3. 새 tag-content 추가
        if (CollectionUtils.isNotEmpty(createdTagList)) {
            final List<TagContentEntity> registeredList = tagContentService.addTagContents(clsfKey, createdTagList);
            for (final TagContentEntity tag : registeredList) {
                tagCntChangeMap.put(tag.getTagId(), 1);
            }
        }

        if (!tagCntChangeMap.isEmpty() && yy != null && mnth != null) {
            publishTagCacheUpdateAfterCommit(clsfKey, yy, mnth, tagCntChangeMap);
        }
    }

    /**
     * 트랜잭션 commit 이후 태그 캐시 갱신 이벤트를 발행한다.
     */
    private void publishTagCacheUpdateAfterCommit(
            final BaseClsfKey clsfKey,
            final Integer yy,
            final Integer mnth,
            final Map<Integer, Integer> tagCntChangeMap
    ) throws Exception {
        final Object cacheKey = new SimpleKey(AuthUtils.getLgnUserId(), yy, mnth);
        final String contentType = clsfKey.getContentType();
        final Map<Integer, Integer> safeChangeMap = new HashMap<>(tagCntChangeMap);
        TransactionHookUtils.runAfterCommitOrNow(
                () -> jrnlTagCacheUpdtWorker.handle(contentType, cacheKey, safeChangeMap),
                e -> log.error("Tag cache update failed [{}:{}:{}]: {}", contentType, yy, mnth, e.getMessage(), e)
        );
    }
}
