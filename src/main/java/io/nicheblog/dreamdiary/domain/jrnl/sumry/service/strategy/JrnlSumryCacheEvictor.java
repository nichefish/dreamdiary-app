package io.nicheblog.dreamdiary.domain.jrnl.sumry.service.strategy;

import io.nicheblog.dreamdiary.domain.jrnl.sumry.entity.JrnlSumryEntity;
import io.nicheblog.dreamdiary.domain.jrnl.sumry.model.JrnlSumryDto;
import io.nicheblog.dreamdiary.extension.cache.event.JrnlCacheEvictEvent;
import io.nicheblog.dreamdiary.extension.cache.model.JrnlCacheEvictParam;
import io.nicheblog.dreamdiary.extension.cache.service.CacheEvictor;
import io.nicheblog.dreamdiary.extension.cache.util.EhCacheUtils;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class JrnlSumryCacheEvictor
        implements CacheEvictor<JrnlCacheEvictEvent> {

    /**
     * 해당 컨텐츠 타입 관련 캐시를 제거한다.
     *
     * @param event 캐시 삭제 이벤트 객체
     * @throws Exception 캐시 삭제 과정에서 발생할 수 있는 예외
     */
    @Override
    public void evict(final JrnlCacheEvictEvent event) throws Exception {
        final JrnlCacheEvictParam param = event.getCacheEvictParam();
        final Integer postNo = param.getPostNo();
        // 목록 캐시 초기화
        EhCacheUtils.evictMyCacheAll("myJrnlSumryList");
        EhCacheUtils.evictMyCacheAll("myJrnlTotalSumry");
        // 상세 캐시 초기화
        EhCacheUtils.evictMyCache("myJrnlSumryDtl", postNo);
        final JrnlSumryDto jrnlSumry = (JrnlSumryDto) EhCacheUtils.getObjectFromCache("myJrnlSumryDtl", postNo);
        if (jrnlSumry != null) EhCacheUtils.evictMyCache("myJrnlSumryDtlByYy", jrnlSumry.getYy());
        // L2캐시 처리
        EhCacheUtils.clearL2Cache(JrnlSumryEntity.class);
    }
}
