package io.nicheblog.dreamdiary.domain.jrnl.diary.service.strategy;

import io.nicheblog.dreamdiary.domain.jrnl.diary.entity.JrnlDiaryContentTagEntity;
import io.nicheblog.dreamdiary.domain.jrnl.diary.entity.JrnlDiaryEntity;
import io.nicheblog.dreamdiary.domain.jrnl.diary.entity.JrnlDiaryTagEntity;
import io.nicheblog.dreamdiary.domain.jrnl.diary.model.JrnlDiaryDto;
import io.nicheblog.dreamdiary.domain.jrnl.diary.service.JrnlDiaryService;
import io.nicheblog.dreamdiary.extension.cache.service.CacheEvictor;
import io.nicheblog.dreamdiary.extension.cache.util.EhCacheUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * JrnlDiaryCacheEvictor
 * <p>
 *  저널 일기 관련 캐시 evictor.
 * </p>
 *
 * @author nichefish
 */
@Component
@RequiredArgsConstructor
public class JrnlDiaryCacheEvictor
        implements CacheEvictor<Integer> {

    private final JrnlDiaryService jrnlDiaryService;

    /**
     * 해당 컨텐츠 타입 관련 캐시를 제거한다.
     *
     * @param key 캐시에서 삭제할 대상 키
     * @throws Exception 캐시 삭제 과정에서 발생할 수 있는 예외
     */
    @Override
    public void evict(final Integer key) throws Exception {
        // 데이터 조회
        final JrnlDiaryDto jrnlDiary = this.getDataByKey(key);
        if (jrnlDiary == null) return;
        // jrnl_dream
        EhCacheUtils.evictMyCacheAll("myJrnlDiaryList");
        EhCacheUtils.evictMyCache("myJrnlDiaryDtlDto", key);
        // 년도-월에 따른 캐시 삭제
        final Integer yy = jrnlDiary.getYy();
        final Integer mnth = jrnlDiary.getMnth();
        // jrnl_day
        EhCacheUtils.evictMyCache("myJrnlDayDtlDto", jrnlDiary.getJrnlDayNo());
        this.evictMyCacheForPeriod("myJrnlDayList", yy, mnth);
        this.evictMyCacheForPeriod("myJrnlDayCalList", yy, mnth);
        // jrnl_diary_tag
        this.evictMyCacheForPeriod("myJrnlDiaryTagList", yy, mnth);
        this.evictMyCacheForPeriod("myJrnlDiarySizedTagList", yy, mnth);
        EhCacheUtils.evictMyCacheAll("myJrnlDiaryTagCtgrMap");
        this.evictMyCacheForPeriod("myCountDiarySize", yy, mnth);
        EhCacheUtils.evictMyCacheAll("myJrnlDiaryTagDtl");
        // L2캐시 처리
        EhCacheUtils.clearL2Cache(JrnlDiaryEntity.class);
        EhCacheUtils.clearL2Cache(JrnlDiaryTagEntity.class);
        EhCacheUtils.clearL2Cache(JrnlDiaryContentTagEntity.class);
    }

    /**
     * 캐시에서 (부재시 실제) 객체를 조회하여 반환한다.
     *
     * @param key 조회할 객체의 키 (Integer)
     * @return {@link JrnlDiaryDto} 조회된 객체, 조회 실패 시 {@code null}
     * @throws Exception 조회 과정에서 발생한 예외
     */
    @Override
    public JrnlDiaryDto getDataByKey(final Integer key) throws Exception {
        JrnlDiaryDto jrnlDiary = (JrnlDiaryDto) EhCacheUtils.getObjectFromCache("myJrnlDiaryDtlDto", key);
        if (jrnlDiary == null) {
            try {
                return jrnlDiaryService.getDtlDto(key);
            } catch (final Exception e) {
                return jrnlDiaryService.getDeletedDtlDto(key);
            }
        }
        return jrnlDiary;
    }
}
