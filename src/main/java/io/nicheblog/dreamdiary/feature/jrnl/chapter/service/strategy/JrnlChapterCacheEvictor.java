package io.nicheblog.dreamdiary.feature.jrnl.chapter.service.strategy;

import io.nicheblog.dreamdiary.feature.clsf._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.jrnl._shared.handler.JrnlCacheEvictor;
import io.nicheblog.dreamdiary.feature.jrnl._shared.model.JrnlCacheEvictParam;
import io.nicheblog.dreamdiary.infrastructure.cache.util.EhCacheUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * JrnlChapterCacheEvictor
 * <p>
 *  저널 챕터 관련 캐시 evictor.
 * </p>
 *
 * @author nichefish
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class JrnlChapterCacheEvictor
        implements JrnlCacheEvictor {

    /**
     * 해당 컨텐츠 타입 관련 캐시를 제거한다.
     *
     * @param param 캐시 삭제 파라미터 객체
     */
    @Override
    @Transactional
    public void evict(final JrnlCacheEvictParam param) throws Exception {
        final ContentType refContentType = ContentType.JRNL_CHAPTER;
        try {
            final String username = param.getRegstrId();
            final Integer postNo = param.getPostNo();
            final Integer jrnlDayNo = param.getJrnlDayNo();
            final Integer yy = param.getYy();
            final Integer mnth = param.getMnth();
            final String weekStartDt = param.getWeekStartDt();
            if (postNo != null) {
                EhCacheUtils.evictUserCacheByKey("jrnlChapterDtlDtoByUser", username, postNo);
            }
            // jrnl_day
            if (jrnlDayNo != null) {
                EhCacheUtils.evictUserCacheByKey("jrnlDayDtlDtoByUser", username, jrnlDayNo);
            }
            this.evictMyJrnlDayYyMnthCaches(username, yy, mnth);
            this.evictMyJrnlDayWeeklyCaches(username, weekStartDt);
            // 태그 캐시 처리
            if (postNo != null) {
                EhCacheUtils.evictCacheByKey("tagContentEntityListByRef", postNo + "_JRNL_CHAPTER");
            }
        } catch (final Exception e) {
            log.error("CacheEvictor error [{}]: {}", refContentType, e.getMessage(), e);
            throw e;
        }
    }
}
