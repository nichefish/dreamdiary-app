package io.nicheblog.dreamdiary.feature.clsf.tag.handler;

import io.nicheblog.dreamdiary.feature.clsf.ContentType;
import io.nicheblog.dreamdiary.feature.clsf.tag.model.TagDto;
import io.nicheblog.dreamdiary.feature.clsf.tag.service.TagProcService;
import io.nicheblog.dreamdiary.feature.clsf.tag.service.TagService;
import io.nicheblog.dreamdiary.infrastructure.cache.util.EhCacheUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JrnlTagCacheUpdtWorker
 * <pre>
 *  태그 캐시 처리 Worker
 * </pre>
 *
 * @author nichefish
 * @see TagProcService
 **/
@Component
@RequiredArgsConstructor
@Log4j2
public class JrnlTagCacheUpdtWorker {

    private final TagService tagService;
    private final CacheManager cacheManager;

    /**
     * 태그 처리
     *
     * @param contentType String
     * @param cacheKey String
     */
    @Transactional
    public void handle(final String contentType, final String cacheKey, final Map<Integer, Integer> tagCntChangeMap) throws Exception {
        if (MapUtils.isEmpty(tagCntChangeMap)) return;
        if (StringUtils.isBlank(contentType) || StringUtils.isBlank(cacheKey)) return;

        updtSizedMapCache(contentType, cacheKey, tagCntChangeMap);
        updtSizedListCache(contentType, cacheKey, tagCntChangeMap);
    }

    /**
     * 태그 개수 캐시(Map)를 업데이트한다.
     *
     * @param contentType 콘텐츠 유형
     * @param cacheKey 캐시 키 (사용자별 YY-MM 식별자)
     * @param tagCntChangeMap 변경된 태그 개수 정보 (태그 ID → 증가/감소 값)
     */
    private void updtSizedMapCache(
            final String contentType,
            final String cacheKey,
            final Map<Integer, Integer> tagCntChangeMap
    ) {
        final String cacheNm = this.getSizedTagMapCacheNmByContentType(contentType);
        final String sizedListCacheNm = this.getSizedTagListCacheNmByContentType(contentType);
        final String listCacheNm = this.getTagListCacheNmByContentType(contentType);
        if (StringUtils.isBlank(cacheNm) || StringUtils.isBlank(sizedListCacheNm) || StringUtils.isBlank(listCacheNm)) return;

        final Cache cache = cacheManager.getCache(cacheNm);
        if (cache == null) {
            evictTagCachesByKey(cacheNm, sizedListCacheNm, listCacheNm, cacheKey);
            return;
        }

        final ConcurrentHashMap<Integer, Integer> sizeMap = cache.get(cacheKey, ConcurrentHashMap.class);
        if (sizeMap == null) {
            // partial update를 금지하고 full reload가 일어나도록 key cache를 비운다.
            evictTagCachesByKey(cacheNm, sizedListCacheNm, listCacheNm, cacheKey);
            return;
        }

        for (final Map.Entry<Integer, Integer> entry : tagCntChangeMap.entrySet()) {
            sizeMap.compute(entry.getKey(), (k, v) -> {
                final int curr = (v == null) ? 0 : v;
                final int next = curr + entry.getValue();
                return (next <= 0) ? null : next;
            });
        }

        cache.put(cacheKey, sizeMap);
    }

    /**
     * 태그 목록 캐시(List)를 업데이트한다.
     *
     * @param contentType 콘텐츠 유형
     * @param cacheKey 캐시 키 (사용자별 YY-MM 식별자)
     * @param tagCntChangeMap 변경된 태그 개수 정보 (태그 ID → 증가/감소 값)
     */
    public void updtSizedListCache(final String contentType, final String cacheKey, final Map<Integer, Integer> tagCntChangeMap) throws Exception {
        final String sizedListCacheNm = this.getSizedTagListCacheNmByContentType(contentType);
        final String sizedMapCacheNm = this.getSizedTagMapCacheNmByContentType(contentType);
        final String listCacheNm = this.getTagListCacheNmByContentType(contentType);
        if (StringUtils.isBlank(sizedMapCacheNm) || StringUtils.isBlank(sizedListCacheNm) || StringUtils.isBlank(listCacheNm)) return;

        final Cache cache = cacheManager.getCache(sizedListCacheNm);
        if (cache == null) {
            evictTagCachesByKey(sizedMapCacheNm, sizedListCacheNm, listCacheNm, cacheKey);
            return;
        }

        final List<TagDto> cachedSizedTagList = cache.get(cacheKey, List.class);
        if (cachedSizedTagList == null) {
            // miss 시 증분 갱신을 수행하면 "변경 태그만 남는" 상태가 생길 수 있다.
            evictTagCachesByKey(sizedMapCacheNm, sizedListCacheNm, listCacheNm, cacheKey);
            return;
        }
        final List<TagDto> sizedTagList = new ArrayList<>(cachedSizedTagList);

        final Iterator<TagDto> iterator = sizedTagList.iterator();
        final Set<Integer> processedTags = new HashSet<>();
        while (iterator.hasNext()) {
            final TagDto tag = iterator.next();
            final Integer changeValue = tagCntChangeMap.get(tag.getTagNo());
            if (changeValue == null) continue;

            final int newSize = tag.getContentSize() + changeValue;
            if (newSize <= 0) {
                iterator.remove();
            } else {
                tag.setContentSize(newSize);
            }
            processedTags.add(tag.getTagNo());
        }

        // 새로 추가해야 할 태그 처리
        for (final Map.Entry<Integer, Integer> entry : tagCntChangeMap.entrySet()) {
            final Integer tagNo = entry.getKey();
            final Integer changeValue = entry.getValue();

            if (changeValue > 0 && !processedTags.contains(tagNo)) {
                final TagDto tagDto = tagService.getDtlDto(tagNo);
                if (tagDto != null) {
                    tagDto.setContentSize(changeValue);
                    sizedTagList.add(tagDto);
                }
            }
        }

        // 변경된 태그 목록 캐시 저장
        final Cache listCache = cacheManager.getCache(listCacheNm);
        if (listCache != null) listCache.put(cacheKey, sizedTagList);

        final Cache yyMnthListCache = cacheManager.getCache(listCacheNm + "YyMnth");
        if (yyMnthListCache != null) yyMnthListCache.put(cacheKey, sizedTagList);

        EhCacheUtils.evictCacheByKey(sizedListCacheNm, cacheKey);
    }

    /**
     * 증분 갱신 전제조건이 맞지 않을 때, key 단위 캐시를 제거해 다음 조회에서 full rebuild 하도록 유도한다.
     */
    private void evictTagCachesByKey(
            final String sizedMapCacheNm,
            final String sizedListCacheNm,
            final String listCacheNm,
            final String cacheKey
    ) {
        EhCacheUtils.evictCacheByKey(sizedMapCacheNm, cacheKey);
        EhCacheUtils.evictCacheByKey(sizedListCacheNm, cacheKey);
        EhCacheUtils.evictCacheByKey(listCacheNm, cacheKey);
        EhCacheUtils.evictCacheByKey(listCacheNm + "YyMnth", cacheKey);
    }

    /**
     * 콘텐츠 타입에 따른 태그 갯수 맵 캐시 이름 반환 :: 메소드 분리
     *
     * @param contentType 콘텐츠 유형 (String)
     * @return {@link String} -- 해당 콘텐츠 유형에 맞는 캐시 이름.
     */
    public String getSizedTagMapCacheNmByContentType(final String contentType) {
        if (ContentType.JRNL_DAY.key.equals(contentType)) {
            return "myCountDaySizeMap";
        } else if (ContentType.JRNL_DIARY.key.equals(contentType)) {
            return "myCountDiarySizeMap";
        } else if (ContentType.JRNL_DREAM.key.equals(contentType)) {
            return "myCountDreamSizeMap";
        }
        return "";
    }

    /**
     * 콘텐츠 타입에 따른 태그 목록 캐시 이름 반환 :: 메소드 분리
     *
     * @param contentType 콘텐츠 유형 (String)
     * @return {@link String} -- 해당 콘텐츠 유형에 맞는 캐시 이름.
     */
    private String getTagListCacheNmByContentType(final String contentType) {
        if (ContentType.JRNL_DAY.key.equals(contentType)) {
            return "myJrnlDayTagList";
        } else if (ContentType.JRNL_DIARY.key.equals(contentType)) {
            return "myJrnlDiaryTagList";
        } else if (ContentType.JRNL_DREAM.key.equals(contentType)) {
            return "myJrnlDreamTagList";
        }
        return "";
    }

    /**
     * 콘텐츠 타입에 따른 태그 목록(Sized) 캐시 이름 반환 :: 메소드 분리
     *
     * @param contentType 콘텐츠 유형 (String)
     * @return {@link String} -- 해당 콘텐츠 유형에 맞는 캐시 이름.
     */
    private String getSizedTagListCacheNmByContentType(final String contentType) {
        if (ContentType.JRNL_DAY.key.equals(contentType)) {
            return "myJrnlDaySizedTagList";
        } else if (ContentType.JRNL_DIARY.key.equals(contentType)) {
            return "myJrnlDiarySizedTagList";
        } else if (ContentType.JRNL_DREAM.key.equals(contentType)) {
            return "myJrnlDreamSizedTagList";
        }
        return "";
    }
}
