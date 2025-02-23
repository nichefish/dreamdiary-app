package io.nicheblog.dreamdiary.extension.clsf.tag.handler;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.extension.cache.handler.EhCacheEvictEventListner;
import io.nicheblog.dreamdiary.extension.cache.util.EhCacheUtils;
import io.nicheblog.dreamdiary.extension.clsf.ContentType;
import io.nicheblog.dreamdiary.extension.clsf.tag.event.JrnlTagCacheUpdtEvent;
import io.nicheblog.dreamdiary.extension.clsf.tag.model.TagDto;
import io.nicheblog.dreamdiary.extension.clsf.tag.service.TagService;
import io.nicheblog.dreamdiary.global.config.AsyncConfig;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JrnlTagCntCacheUpdtEventListener
 * <pre>
 *  태그 캐시 갱신 이벤트 처리 핸들러.
 *  태그 개수(Map) 및 태그 목록(List)을 업데이트.
 * </pre>
 *
 * @author nichefish
 * @see AsyncConfig
 * @see EhCacheEvictEventListner
 */
@Component
@RequiredArgsConstructor
public class JrnlTagCacheUpdtEventListener {

    private final TagService tagService;
    private final CacheManager cacheManager;

    /**
     * 태그 캐시 갱신 이벤트를 처리한다.
     *
     * @param event 처리할 이벤트 객체
     * @throws Exception 처리 중 발생할 수 있는 예외
     * @see EhCacheEvictEventListner
     */
    @EventListener
    public void handleTagCacheUpdtEvent(final JrnlTagCacheUpdtEvent event) throws Exception {
        final Map<Integer, Integer> tagCntChangeMap = event.getTagCntChangeMap();
        if (MapUtils.isEmpty(tagCntChangeMap)) return;

        final Integer yy = event.getYy();
        final Integer mnth = event.getMnth();
        final String cacheKey = AuthUtils.getLgnUserId() + "_" + yy + "_" + mnth;
        final String contentType = event.getClsfKey().getContentType();

        // CountMap 캐시 업데이트 :: 메소드 분리
        this.updtSizedMapCache(contentType, cacheKey, tagCntChangeMap);

        // SizedList 캐시 업데이트 :: 메소드 분리
        this.updtSizedListCache(contentType, cacheKey, tagCntChangeMap);
    }

    /**
     * 태그 개수 캐시(Map)를 업데이트한다.
     *
     * @param contentType 콘텐츠 유형
     * @param cacheKey 캐시 키 (사용자별 YY-MM 식별자)
     * @param tagCntChangeMap 변경된 태그 개수 정보 (태그 ID → 증가/감소 값)
     */
    private void updtSizedMapCache(final String contentType, final String cacheKey, final Map<Integer, Integer> tagCntChangeMap) {
        final String cacheNm = this.getSizedTagMapCacheNmByContentType(contentType);
        final Cache cache = cacheManager.getCache(cacheNm);
        if (cache == null) return;
        final ConcurrentHashMap<Integer, Integer> sizeMap = Objects.requireNonNullElseGet(cache.get(cacheKey, ConcurrentHashMap.class), ConcurrentHashMap::new);

        // 한 번에 업데이트 (증가량/감소량 반영)
        for (Map.Entry<Integer, Integer> entry : tagCntChangeMap.entrySet()) {
            sizeMap.compute(entry.getKey(), (k, v) -> (v == null) ? entry.getValue() : v + entry.getValue());
        }

        // 업데이트된 정보를 다시 캐시에 저장
        cache.put(cacheKey, sizeMap);
    }

    /**
     * 태그 목록 캐시(List)를 업데이트한다.
     *
     * @param contentType 콘텐츠 유형
     * @param cacheKey 캐시 키 (사용자별 YY-MM 식별자)
     * @param tagCntChangeMap 변경된 태그 개수 정보 (태그 ID → 증가/감소 값)
     * @throws Exception 태그 정보 조회 중 발생할 수 있는 예외
     */
    private void updtSizedListCache(final String contentType, final String cacheKey, final Map<Integer, Integer> tagCntChangeMap) throws Exception {
        final String sizedListCacheNm = this.getSizedTagListCacheNmByContentType(contentType);
        final Cache cache = cacheManager.getCache(sizedListCacheNm);
        final List<TagDto> sizedTagList = Objects.requireNonNullElseGet(cache.get(cacheKey, List.class), ArrayList::new);

        // 기존 태그 목록에서 개수 변경 (Iterator 사용)
        final Iterator<TagDto> iterator = sizedTagList.iterator();
        final Set<Integer> processedTags = new HashSet<>();
        while (iterator.hasNext()) {
            final TagDto tag = iterator.next();
            final Integer changeValue = tagCntChangeMap.get(tag.getTagNo());

            if (changeValue == null) continue;

            final int newSize = tag.getContentSize() + changeValue;
            if (newSize <= 0) {
                iterator.remove(); // 안전한 삭제
            } else {
                tag.setContentSize(newSize);
            }
            processedTags.add(tag.getTagNo());
        }
        // 이미 처리한 태그는 제거
        tagCntChangeMap.keySet().removeAll(processedTags);

        // 새로 추가해야 할 태그 처리
        for (final Map.Entry<Integer, Integer> entry : tagCntChangeMap.entrySet()) {
            final Integer tagNo = entry.getKey();
            final Integer changeValue = entry.getValue();

            if (changeValue > 0) {
                final TagDto tagDto = tagService.getDtlDto(tagNo); // 태그 정보 조회
                if (tagDto != null) {
                    tagDto.setContentSize(changeValue);
                    sizedTagList.add(tagDto);
                }
            }
        }

        // 변경된 태그 목록 캐시 저장
        final String listCacheNm = this.getTagListCacheNmByContentType(contentType);
        final Cache listCache = cacheManager.getCache(listCacheNm);
        listCache.put(cacheKey, sizedTagList);
        // 기존 sizedList 캐시 초기화 (size에 따른 tagClass를 재계산해야 하므로)
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
    private String getTagListCacheNmByContentType(final String  contentType) {
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
    private String getSizedTagListCacheNmByContentType(final String  contentType) {
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