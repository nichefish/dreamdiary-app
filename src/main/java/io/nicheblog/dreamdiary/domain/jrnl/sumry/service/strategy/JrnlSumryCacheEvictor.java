package io.nicheblog.dreamdiary.domain.jrnl.sumry.service.strategy;

import io.nicheblog.dreamdiary.extension.cache.event.JrnlCacheEvictEvent;
import io.nicheblog.dreamdiary.extension.cache.model.JrnlCacheEvictParam;
import io.nicheblog.dreamdiary.extension.cache.service.CacheEvictor;
import io.nicheblog.dreamdiary.extension.cache.util.EhCacheUtils;
import io.nicheblog.dreamdiary.extension.clsf.ContentType;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
@Log4j2
public class JrnlSumryCacheEvictor
        implements CacheEvictor<JrnlCacheEvictEvent> {

    /**
     * 해당 컨텐츠 타입 관련 캐시를 제거한다.
     *
     * @param event 캐시 삭제 이벤트 객체
     */
    @Override
    @Transactional
    public void evict(final JrnlCacheEvictEvent event) throws Exception {
        final ContentType refContentType = event.getContentType();
        try {
            final JrnlCacheEvictParam param = event.getCacheEvictParam();
            final Integer postNo = param.getPostNo();
            // 목록 캐시 초기화
            EhCacheUtils.evictMyCacheAll("myJrnlSumryList");
            EhCacheUtils.evictMyCacheAll("myJrnlTotalSumry");
            // 상세 캐시 초기화
            EhCacheUtils.evictMyCache("myJrnlSumryDtl", postNo);
            EhCacheUtils.evictMyCache("myJrnlSumryDtlByYy", param.getYy());
            // 태그 캐시 처리
            EhCacheUtils.evictCache("tagContentEntityListByRef", postNo + "_JRNL_SUMRY");
        } catch (final Exception e) {
            log.error("CacheEvictor error [{}]: {}", refContentType, e.getMessage(), e);
            throw e;
        }
    }
}
