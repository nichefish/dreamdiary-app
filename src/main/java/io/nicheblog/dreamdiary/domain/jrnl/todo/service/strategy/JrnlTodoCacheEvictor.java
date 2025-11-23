package io.nicheblog.dreamdiary.domain.jrnl.todo.service.strategy;

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
 * JrnlTodoCacheEvictor
 * <p>
 *  저널 할일 관련 캐시 evictor.
 * </p>
 *
 * @author nichefish
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class JrnlTodoCacheEvictor
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
            final Integer yy = param.getYy();
            final Integer mnth = param.getMnth();
            // jrnl_todo
            this.evictMyCacheForPeriod("myJrnlTodoList", yy, mnth);
            // 태그 처리
            EhCacheUtils.evictCache("contentTagEntityListByRef", postNo + "_JRNL_TODO");
        } catch (final Exception e) {
            log.error("CacheEvictor error [{}]: {}", refContentType, e.getMessage(), e);
            throw e;
        }
    }
}
