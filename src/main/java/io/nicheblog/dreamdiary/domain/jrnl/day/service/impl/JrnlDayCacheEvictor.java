package io.nicheblog.dreamdiary.domain.jrnl.day.service.impl;

import io.nicheblog.dreamdiary.domain.jrnl.day.entity.JrnlDayContentTagEntity;
import io.nicheblog.dreamdiary.domain.jrnl.day.entity.JrnlDayTagEntity;
import io.nicheblog.dreamdiary.domain.jrnl.day.model.JrnlDayDto;
import io.nicheblog.dreamdiary.domain.jrnl.day.service.JrnlDayService;
import io.nicheblog.dreamdiary.global._common.cache.service.CacheEvictor;
import io.nicheblog.dreamdiary.global._common.cache.util.EhCacheUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * JrnlDayCacheEvictor
 * <p>
 *  저널 일자 관련 캐시 evictor
 * </p>
 *
 * @author nichefish
 */
@Component
@RequiredArgsConstructor
public class JrnlDayCacheEvictor
        implements CacheEvictor<Integer> {

    private final JrnlDayService jrnlDayService;

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
        // 태그가 삭제되었을 때 태그 목록 캐시 초기화
        JrnlDayDto jrnlDay = (JrnlDayDto) EhCacheUtils.getObjectFromCache("jrnlDayDtlDto", key);
        if (jrnlDay == null) {
            try {
                jrnlDay = jrnlDayService.getDtlDto(key);
            } catch (Exception e) {
                jrnlDay = jrnlDayService.getDeletedDtlDto(key);
                if (jrnlDay == null) return;
            }
        }
        // 년도-월에 따른 캐시 삭제
        final String yy = jrnlDay.getYy();
        final String mnth = jrnlDay.getMnth();
        // jrnl_day
        EhCacheUtils.evictCache("jrnlDayDtlDto", key);
        this.evictCacheForPeriod("jrnlDayList", yy, mnth);
        // jrnl_day_tag
        this.evictCacheForPeriod("jrnlDayTagList", yy, mnth);
        this.evictCacheForPeriod("jrnlDaySizedTagList", yy, mnth);
        // L2캐시 처리
        EhCacheUtils.clearL2Cache(JrnlDayTagEntity.class);
        EhCacheUtils.clearL2Cache(JrnlDayContentTagEntity.class);
    }
}
