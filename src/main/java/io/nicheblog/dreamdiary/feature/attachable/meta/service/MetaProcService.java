package io.nicheblog.dreamdiary.feature.attachable.meta.service;

import io.nicheblog.dreamdiary.feature.attachable._shared.entity.BaseAttachableKey;
import io.nicheblog.dreamdiary.feature.attachable.meta.entity.MetaContentEntity;
import io.nicheblog.dreamdiary.feature.attachable.meta.entity.MetaEntity;
import io.nicheblog.dreamdiary.feature.attachable.meta.model.MetaDto;
import io.nicheblog.dreamdiary.feature.attachable.meta.model.cmpstn.MetaCmpstn;
import io.nicheblog.dreamdiary.infrastructure.cache.util.EhCacheUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * MetaProcService
 * <pre>
 *  메타 처리 서비스.
 * </pre>
 *
 * @author nichefish
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class MetaProcService {

    private final MetaService metaService;
    private final MetaContentService metaContentService;

    /**
     * 메타 처리.
     *
     * @param attachableKey 분류 키
     * @param metaCmpstn 메타 조합
     * @param yy 저널 연도
     * @param mnth 저널 월
     */
    @Transactional
    public void process(
            final BaseAttachableKey attachableKey,
            final MetaCmpstn metaCmpstn,
            final Integer yy,
            final Integer mnth
    ) throws Exception {
        if (attachableKey == null) return;

        final boolean isJournal = yy != null || mnth != null;
        if (isJournal && (yy == null || mnth == null)) {
            throw new IllegalStateException("yy/mnth must both be set for journal meta process.");
        }

        if (metaCmpstn == null) {
            delExistingMetaContents(attachableKey);
        } else {
            procMetas(attachableKey, metaCmpstn);
        }

        // 비저널 컨텐츠만 일반 분류 캐시 evict
        if (!isJournal) {
            EhCacheUtils.evictCacheByKey("metaContentEntityListByRef", attachableKey.getId() + "_" + attachableKey.getContentType());
        }

        // 고아 메타 정리
        metaService.deleteNoRefMetas();
    }

    /**
     * 특정 게시물의 기존 meta-content를 모두 제거.
     */
    @Transactional
    public void delExistingMetaContents(final BaseAttachableKey attachableKey) throws Exception {
        if (attachableKey == null) return;

        final Map<String, Object> searchParamMap = new HashMap<>();
        searchParamMap.put("refId", attachableKey.getId());
        searchParamMap.put("refContentType", attachableKey.getContentType());

        final List<MetaContentEntity> entityList = metaContentService.getListEntity(searchParamMap);
        metaContentService.deleteAll(entityList);
    }

    /**
     * meta-content 처리.
     * 새 meta를 추가하고, 더 이상 필요 없는 meta는 제거.
     */
    @Transactional
    public void procMetas(final BaseAttachableKey attachableKey, final MetaCmpstn metaCmpstn) throws Exception {
        if (attachableKey == null || metaCmpstn == null) return;

        final List<MetaDto> existingMetaList = metaContentService.getMetaStrListByAttachableKey(attachableKey);
        final List<MetaDto> newMetaList = metaCmpstn.getParsedMetaList();
        final boolean isSame = newMetaList.size() == existingMetaList.size() && new HashSet<>(newMetaList).containsAll(existingMetaList);
        if (isSame) return;

        // 1. 새 마스터 메타 추가
        final Set<MetaDto> newMetaSet = new HashSet<>(newMetaList);
        existingMetaList.forEach(newMetaSet::remove);
        final List<MetaEntity> createdMetaList = CollectionUtils.isNotEmpty(newMetaSet)
                ? metaService.addMasterMeta(new ArrayList<>(newMetaSet))
                : new ArrayList<>();

        // 2. 사용하지 않는 meta-content 제거
        final Set<MetaDto> obsoleteMetaSet = new HashSet<>(existingMetaList);
        newMetaList.forEach(obsoleteMetaSet::remove);
        if (CollectionUtils.isNotEmpty(obsoleteMetaSet)) {
            metaContentService.delObsoleteMetaContents(attachableKey, new ArrayList<>(obsoleteMetaSet));
        }

        // 3. 새 meta-content 추가
        if (CollectionUtils.isNotEmpty(createdMetaList)) {
            metaContentService.addMetaContents(attachableKey, createdMetaList);
        }
    }
}

