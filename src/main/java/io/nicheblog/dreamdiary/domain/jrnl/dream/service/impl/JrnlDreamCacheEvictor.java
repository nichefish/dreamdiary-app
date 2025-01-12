package io.nicheblog.dreamdiary.domain.jrnl.dream.service.impl;

import io.nicheblog.dreamdiary.domain.jrnl.dream.entity.JrnlDreamContentTagEntity;
import io.nicheblog.dreamdiary.domain.jrnl.dream.entity.JrnlDreamEntity;
import io.nicheblog.dreamdiary.domain.jrnl.dream.entity.JrnlDreamTagEntity;
import io.nicheblog.dreamdiary.domain.jrnl.dream.model.JrnlDreamDto;
import io.nicheblog.dreamdiary.domain.jrnl.dream.service.JrnlDreamService;
import io.nicheblog.dreamdiary.global._common.cache.service.CacheEvictor;
import io.nicheblog.dreamdiary.global._common.cache.util.EhCacheUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * JrnlDreamCacheEvictor
 * <p>
 *  저널 꿈 관련 캐시 evictor.
 * </p>
 *
 * @author nichefish
 */
@Component
@RequiredArgsConstructor
public class JrnlDreamCacheEvictor
        implements CacheEvictor<Integer> {

    private final JrnlDreamService jrnlDreamService;

    /**
     * 해당 컨텐츠 타입 관련 캐시를 제거한다.
     *
     * @param key 캐시에서 삭제할 대상 키
     * @throws Exception 캐시 삭제 과정에서 발생할 수 있는 예외
     */
    @Override
    public void evict(final Integer key) throws Exception {
        // jrnl_day
        EhCacheUtils.evictCacheAll("jrnlDayList");
        // 태그가 삭제되었을 때 태그 목록 캐시 초기화 (부재시 삭제 데이터 조회)
        JrnlDreamDto jrnlDream = (JrnlDreamDto) EhCacheUtils.getObjectFromCache("myJrnlDreamDtlDto", key);
        if (jrnlDream == null) {
            try {
                jrnlDream = jrnlDreamService.getDtlDto(key);
            } catch (Exception e) {
                jrnlDream = jrnlDreamService.getDeletedDtlDto(key);
                if (jrnlDream == null) return;
            }
        }
        // jrnl_dream
        EhCacheUtils.evictMyCacheAll("jrnlDreamList");
        EhCacheUtils.evictMyCache("myJrnlDreamDtlDto", key);
        // 년도-월에 따른 캐시 삭제
        final Integer yy = jrnlDream.getYy();
        final Integer mnth = jrnlDream.getMnth();
        // jrnl_day
        EhCacheUtils.evictMyCache("myJrnlDayDtlDto", jrnlDream.getJrnlDayNo());
        this.evictMyCacheForPeriod("myJrnlDayList", yy, mnth);
        // jrnl_dream_tag
        this.evictMyCacheForPeriod("myJrnlDreamTagList", yy, mnth);
        this.evictMyCacheForPeriod("myJrnlDreamSizedTagList", yy, mnth);
        // L2캐시 처리
        EhCacheUtils.clearL2Cache(JrnlDreamEntity.class);
        EhCacheUtils.clearL2Cache(JrnlDreamTagEntity.class);
        EhCacheUtils.clearL2Cache(JrnlDreamContentTagEntity.class);
    }
}
