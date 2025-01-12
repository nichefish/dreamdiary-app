package io.nicheblog.dreamdiary.domain.jrnl.sumry.service.impl;

import io.nicheblog.dreamdiary.domain.jrnl.sumry.entity.JrnlSumryEntity;
import io.nicheblog.dreamdiary.domain.jrnl.sumry.model.JrnlSumryDto;
import io.nicheblog.dreamdiary.global._common.cache.service.CacheEvictor;
import io.nicheblog.dreamdiary.global._common.cache.util.EhCacheUtils;
import org.springframework.stereotype.Component;

/**
 * JrnlSumryCacheEvictor
 * <p>
 *  저널 결산 관련 캐시 evictor.
 * </p>
 *
 * @author nichefish
 */
@Component
public class JrnlSumryCacheEvictor
        implements CacheEvictor<Integer> {

    /**
     * 해당 컨텐츠 타입 관련 캐시를 제거한다.
     *
     * @param key 캐시에서 삭제할 대상 키
     * @throws Exception 캐시 삭제 과정에서 발생할 수 있는 예외
     */
    @Override
    public void evict(final Integer key) throws Exception {
        // 목록 캐시 초기화
        EhCacheUtils.evictMyCacheAll("myJrnlSumryList");
        EhCacheUtils.evictMyCacheAll("myJrnlTotalSumry");
        // 상세 캐시 초기화
        EhCacheUtils.evictMyCache("myJrnlSumryDtl", key);
        final JrnlSumryDto jrnlSumry = (JrnlSumryDto) EhCacheUtils.getObjectFromCache("myJrnlSumryDtl", key);
        if (jrnlSumry != null) EhCacheUtils.evictMyCache("myJrnlSumryDtlByYy", jrnlSumry.getYy());
        // L2캐시 처리
        EhCacheUtils.clearL2Cache(JrnlSumryEntity.class);
    }
}
