package io.nicheblog.dreamdiary.feature.clsf.tag.handler;

import io.nicheblog.dreamdiary.feature.clsf.ContentType;
import io.nicheblog.dreamdiary.feature.clsf.tag.model.TagDto;
import io.nicheblog.dreamdiary.feature.clsf.tag.service.TagProcService;
import io.nicheblog.dreamdiary.feature.clsf.tag.service.TagService;
import io.nicheblog.dreamdiary.infrastructure.cache.util.EhCacheUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.MapUtils;
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
        final Cache cache = cacheManager.getCache(cacheNm);
        if (cache == null) return;

        final ConcurrentHashMap<Integer, Integer> sizeMap = Objects.requireNonNullElseGet(
                cache.get(cacheKey, ConcurrentHashMap.class),
                ConcurrentHashMap::new
        );

        for (final Map.Entry<Integer, Integer> entry : tagCntChangeMap.entrySet()) {
            sizeMap.compute(entry.getKey(), (k, v) -> (v == null) ? entry.getValue() : v + entry.getValue());
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
        final Cache cache = cacheManager.getCache(sizedListCacheNm);
        if (cache == null) return;

        final List<TagDto> sizedTagList = Optional.ofNullable(cache.get(cacheKey, List.class))
                .map(list -> new ArrayList<>(list))
                .orElseGet(ArrayList::new);

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
        final String listCacheNm = this.getTagListCacheNmByContentType(contentType);
        final Cache listCache = cacheManager.getCache(listCacheNm);
        if (listCache != null) listCache.put(cacheKey, sizedTagList);

        final Cache yyMnthListCache = cacheManager.getCache(listCacheNm + "YyMnth");
        if (yyMnthListCache != null) yyMnthListCache.put(cacheKey, sizedTagList);

        EhCacheUtils.evictCache(sizedListCacheNm, cacheKey);
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
