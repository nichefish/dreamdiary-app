package io.nicheblog.dreamdiary.domain.jrnl.day.service.strategy;

import io.nicheblog.dreamdiary.domain.jrnl.day.entity.JrnlDayContentTagEntity;
import io.nicheblog.dreamdiary.domain.jrnl.day.entity.JrnlDayEntity;
import io.nicheblog.dreamdiary.domain.jrnl.day.entity.JrnlDayTagEntity;
import io.nicheblog.dreamdiary.domain.jrnl.day.model.JrnlDayDto;
import io.nicheblog.dreamdiary.domain.jrnl.day.service.JrnlDayService;
import io.nicheblog.dreamdiary.extension.cache.service.CacheEvictor;
import io.nicheblog.dreamdiary.extension.cache.util.EhCacheUtils;
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
        // 데이터 조회
        final JrnlDayDto jrnlDay = this.getDataByKey(key);
        if (jrnlDay == null) return;
        // 년도-월에 따른 캐시 삭제
        final String yy = jrnlDay.getYy();
        final String mnth = jrnlDay.getMnth();
        // jrnl_day
        EhCacheUtils.evictMyCache("myJrnlDayDtlDto", key);
        EhCacheUtils.evictMyCacheAll("myJrnlDayList");
        this.evictMyCacheForPeriod("myJrnlDayList", yy, mnth);
        this.evictMyCacheForPeriod("myJrnlDayCalList", yy, mnth);
        // jrnl_day_tag
        this.evictMyCacheForPeriod("myJrnlDayTagList", yy, mnth);
        this.evictMyCacheForPeriod("myJrnlDaySizedTagList", yy, mnth);
        EhCacheUtils.evictMyCacheAll("myJrnlDayTagCtgrMap");
        this.evictMyCacheForPeriod("myCountDaySize", yy, mnth);
        EhCacheUtils.evictMyCacheAll("myJrnlDayTagDtl");
        // L2캐시 처리
        EhCacheUtils.clearL2Cache(JrnlDayEntity.class);
        EhCacheUtils.clearL2Cache(JrnlDayTagEntity.class);
        EhCacheUtils.clearL2Cache(JrnlDayContentTagEntity.class);
    }

    /**
     * 캐시에서 (부재시 실제) 객체를 조회하여 반환한다.
     *
     * @param key 조회할 객체의 키 (Integer)
     * @return {@link JrnlDayDto} 조회된 객체, 조회 실패 시 {@code null}
     * @throws Exception 조회 과정에서 발생한 예외
     */
    @Override
    public JrnlDayDto getDataByKey(final Integer key) throws Exception {
        JrnlDayDto jrnlDay = (JrnlDayDto) EhCacheUtils.getObjectFromCache("myJrnlDayDtlDto", key);
        if (jrnlDay == null) {
            try {
                return jrnlDayService.getDtlDto(key);
            } catch (final Exception e) {
                return jrnlDayService.getDeletedDtlDto(key);
            }
        }
        return jrnlDay;
    }
}
