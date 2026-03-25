package io.nicheblog.dreamdiary.feature.jrnl.entry.service.strategy;

import io.nicheblog.dreamdiary.feature.clsf.ContentType;
import io.nicheblog.dreamdiary.feature.jrnl._shared.model.JrnlCacheEvictParam;
import io.nicheblog.dreamdiary.infrastructure.cache.service.CacheEvictor;
import io.nicheblog.dreamdiary.infrastructure.cache.util.EhCacheUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * JrnlEntryCacheEvictor
 * <p>
 *  저널 항목 관련 캐시 evictor.
 * </p>
 *
 * @author nichefish
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class JrnlEntryCacheEvictor
        implements CacheEvictor<JrnlCacheEvictParam> {

    /**
     * 해당 컨텐츠 타입 관련 캐시를 제거한다.
     *
     * @param param 캐시 삭제 파라미터 객체
     */
    @Override
    @Transactional
    public void evict(final JrnlCacheEvictParam param) throws Exception {
        final ContentType refContentType = ContentType.JRNL_ENTRY;
        try {
            final Integer postNo = param.getPostNo();
            final Integer jrnlDayNo = param.getJrnlDayNo();
            final Integer yy = param.getYy();
            final Integer mnth = param.getMnth();
            // jrnl_day
            // fail-safe: 등록/수정 직후 화면 갱신 누락 방지를 위해 목록 캐시는 항상 비운다.
            EhCacheUtils.evictMyCacheAll("myJrnlDayList");
            EhCacheUtils.evictMyCacheAll("myJrnlDayCalList");
            if (jrnlDayNo != null) {
                EhCacheUtils.evictMyCache("myJrnlDayDtlDto", jrnlDayNo);
            }
            if (yy != null && mnth != null) {
                this.evictMyCacheForPeriod("myJrnlDayList", yy, mnth);
                this.evictMyCacheForPeriod("myJrnlDayCalList", yy, mnth);
            }
            // 태그 캐시 처리
            if (postNo != null) {
                EhCacheUtils.evictCache("tagContentEntityListByRef", postNo + "_JRNL_ENTRY");
            }
        } catch (final Exception e) {
            log.error("CacheEvictor error [{}]: {}", refContentType, e.getMessage(), e);
            throw e;
        }
    }
}
