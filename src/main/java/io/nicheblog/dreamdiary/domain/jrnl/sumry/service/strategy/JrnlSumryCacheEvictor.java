package io.nicheblog.dreamdiary.domain.jrnl.sumry.service.strategy;

import io.nicheblog.dreamdiary.domain.jrnl.sumry.entity.JrnlSumryEntity;
import io.nicheblog.dreamdiary.domain.jrnl.sumry.model.JrnlSumryDto;
import io.nicheblog.dreamdiary.domain.jrnl.sumry.service.JrnlSumryService;
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
        implements CacheEvictor<Integer> {

    private final JrnlSumryService jrnlSumryService;

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

    /**
     * 캐시에서 (부재시 실제) 객체를 조회하여 반환한다.
     *
     * @param key 조회할 객체의 키 (Integer)
     * @return {@link JrnlSumryDto} 조회된 객체, 조회 실패 시 {@code null}
     * @throws Exception 조회 과정에서 발생한 예외
     */
    @Override
    public JrnlSumryDto getDataByKey(final Integer key) throws Exception {
        JrnlSumryDto jrnlSumry = (JrnlSumryDto) EhCacheUtils.getObjectFromCache("myJrnlSumryDtlDto", key);
        if (jrnlSumry == null) {
            try {
                return jrnlSumryService.getDtlDto(key);
            } catch (final Exception e) {
                return null;
            }
        }
        return jrnlSumry;
    }
}
