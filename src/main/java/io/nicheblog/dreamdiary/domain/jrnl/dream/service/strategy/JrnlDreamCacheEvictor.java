package io.nicheblog.dreamdiary.domain.jrnl.dream.service.strategy;

import io.nicheblog.dreamdiary.domain.jrnl.dream.entity.JrnlDreamContentTagEntity;
import io.nicheblog.dreamdiary.domain.jrnl.dream.entity.JrnlDreamEntity;
import io.nicheblog.dreamdiary.domain.jrnl.dream.entity.JrnlDreamTagEntity;
import io.nicheblog.dreamdiary.extension.cache.event.JrnlCacheEvictEvent;
import io.nicheblog.dreamdiary.extension.cache.model.JrnlCacheEvictParam;
import io.nicheblog.dreamdiary.extension.cache.service.CacheEvictor;
import io.nicheblog.dreamdiary.extension.cache.util.EhCacheUtils;
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
        final Integer jrnlDayNo = param.getJrnlDayNo();
        final Integer yy = param.getYy();
        final Integer mnth = param.getMnth();
        // jrnl_dream
        EhCacheUtils.evictMyCacheAll("myJrnlDreamList");
        EhCacheUtils.evictMyCache("myJrnlDreamDtlDto", postNo);
        // jrnl_day
        EhCacheUtils.evictMyCache("myJrnlDayDtlDto", jrnlDayNo);
        this.evictMyCacheForPeriod("myJrnlDayList", yy, mnth);
        this.evictMyCacheForPeriod("myJrnlDayCalList", yy, mnth);
        // jrnl_dream_tag
        EhCacheUtils.evictMyCacheAll("myJrnlDreamTagCtgrMap");
        EhCacheUtils.evictMyCacheAll("myJrnlDreamTagDtl");
        // 태그 처리
        EhCacheUtils.evictCache("contentTagEntityListByRef", postNo + "_JRNL_DREAM");
        // L2캐시 처리
        EhCacheUtils.clearL2Cache(JrnlDreamEntity.class);
        EhCacheUtils.clearL2Cache(JrnlDreamTagEntity.class);
        EhCacheUtils.clearL2Cache(JrnlDreamContentTagEntity.class);
    }
}
