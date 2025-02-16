package io.nicheblog.dreamdiary.domain.jrnl.diary.handler;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.domain.jrnl.diary.event.JrnlDiaryTagCntAddEvent;
import io.nicheblog.dreamdiary.domain.jrnl.diary.event.JrnlDiaryTagCntSubEvent;
import io.nicheblog.dreamdiary.extension.cache.handler.EhCacheEvictEventListner;
import io.nicheblog.dreamdiary.global.config.AsyncConfig;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JrnlDiaryTagEventListener
 * <pre>
 *  태그 관련 이벤트 처리 핸들러.
 * </pre>
 *
 * @author nichefish
 * @see AsyncConfig
 */
@Component
@RequiredArgsConstructor
public class JrnlDiaryTagEventListener {

    private final CacheManager cacheManager;


    /**
     * 태그 이벤트를 처리한다.
     * 삭제된 엔티티 재조회와 관련될 수 있으므로 별도 트랜잭션으로 처리.
     *
     * @param event 처리할 이벤트 객체
     * @throws Exception 처리 중 발생할 수 있는 예외
     * @see EhCacheEvictEventListner
     */
    @EventListener
    @Async
    public void handleTagAddEvent(final JrnlDiaryTagCntAddEvent event) throws Exception {
        final List<Integer> tagNoList = event.getTagNoList();
        if (CollectionUtils.isEmpty(tagNoList)) return;

        final Integer yy = event.getYy();
        final Integer mnth = event.getMnth();

        final Cache cache = cacheManager.getCache("myCountDiarySizeMap");
        ConcurrentHashMap<Integer, Integer> sizeMap = (ConcurrentHashMap<Integer, Integer>) cache.get(AuthUtils.getLgnUserId() + "_" + yy + "_" + mnth);
        if (sizeMap == null) sizeMap = new ConcurrentHashMap<>();

        for (final Integer tagNo : tagNoList) {
            sizeMap.put(tagNo, sizeMap.getOrDefault(tagNo, 0) + 1);
        }
        cache.put(AuthUtils.getLgnUserId() + "_" + yy + "_" + mnth, sizeMap);
    }

    /**
     * 태그 이벤트를 처리한다.
     * 삭제된 엔티티 재조회와 관련될 수 있으므로 별도 트랜잭션으로 처리.
     * 
     * @param event 처리할 이벤트 객체
     * @throws Exception 처리 중 발생할 수 있는 예외
     * @see EhCacheEvictEventListner
     */
    @EventListener
    @Async
    public void handleTagRemoveEvent(final JrnlDiaryTagCntSubEvent event) throws Exception {
        final List<Integer> tagNoList = event.getTagNoList();
        if (CollectionUtils.isEmpty(tagNoList)) return;

        final Integer yy = event.getYy();
        final Integer mnth = event.getMnth();

        final Cache cache = cacheManager.getCache("myCountDiarySizeMap");
        ConcurrentHashMap<Integer, Integer> sizeMap = (ConcurrentHashMap<Integer, Integer>) cache.get(AuthUtils.getLgnUserId() + "_" + yy + "_" + mnth);
        if (sizeMap == null) sizeMap = new ConcurrentHashMap<>();

        for (final Integer tagNo : tagNoList) {
            sizeMap.put(tagNo, sizeMap.getOrDefault(tagNo, 1) - 1);
        }
        cache.put(AuthUtils.getLgnUserId() + "_" + yy + "_" + mnth, sizeMap);
    }
}
