package io.nicheblog.dreamdiary.domain.jrnl.dream.service.strategy;

import io.nicheblog.dreamdiary.domain.jrnl.dream.entity.JrnlDreamContentTagEntity;
import io.nicheblog.dreamdiary.domain.jrnl.dream.entity.JrnlDreamEntity;
import io.nicheblog.dreamdiary.domain.jrnl.dream.entity.JrnlDreamTagEntity;
import io.nicheblog.dreamdiary.domain.jrnl.dream.model.JrnlDreamDto;
import io.nicheblog.dreamdiary.domain.jrnl.dream.service.JrnlDreamService;
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
        // 데이터 조회
        JrnlDreamDto jrnlDream = this.getDataByKey(key);
        if (jrnlDream == null) return;
        // jrnl_dream
        EhCacheUtils.evictMyCacheAll("myJrnlDreamList");
        EhCacheUtils.evictMyCache("myJrnlDreamDtlDto", key);
        // 년도-월에 따른 캐시 삭제
        final Integer yy = jrnlDream.getYy();
        final Integer mnth = jrnlDream.getMnth();
        // jrnl_day
        EhCacheUtils.evictMyCache("myJrnlDayDtlDto", jrnlDream.getJrnlDayNo());
        this.evictMyCacheForPeriod("myJrnlDayList", yy, mnth);
        this.evictMyCacheForPeriod("myJrnlDayCalList", yy, mnth);
        // jrnl_dream_tag
        this.evictMyCacheForPeriod("myJrnlDreamTagList", yy, mnth);
        this.evictMyCacheForPeriod("myJrnlDreamSizedTagList", yy, mnth);
        this.evictMyCacheForPeriod("myCountDreamSize", yy, mnth);
        EhCacheUtils.evictMyCacheAll("myJrnlDreamTagDtl");
        // L2캐시 처리
        EhCacheUtils.clearL2Cache(JrnlDreamEntity.class);
        EhCacheUtils.clearL2Cache(JrnlDreamTagEntity.class);
        EhCacheUtils.clearL2Cache(JrnlDreamContentTagEntity.class);
    }

    /**
     * 캐시에서 (부재시 실제) 객체를 조회하여 반환한다.
     *
     * @param key 조회할 객체의 키 (Integer)
     * @return {@link JrnlDreamDto} 조회된 객체, 조회 실패 시 {@code null}
     * @throws Exception 조회 과정에서 발생한 예외
     */
    @Override
    public JrnlDreamDto getDataByKey(final Integer key) throws Exception {
        JrnlDreamDto jrnlDream = (JrnlDreamDto) EhCacheUtils.getObjectFromCache("myJrnlDreamDtlDto", key);
        if (jrnlDream == null) {
            try {
                return jrnlDreamService.getDtlDto(key);
            } catch (final Exception e) {
                return jrnlDreamService.getDeletedDtlDto(key);
            }
        }
        return jrnlDream;
    }
}
