package io.nicheblog.dreamdiary.feature.jrnl.todo.service.strategy;

import io.nicheblog.dreamdiary.feature.clsf._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.jrnl._shared.handler.JrnlCacheEvictor;
import io.nicheblog.dreamdiary.feature.jrnl._shared.model.JrnlCacheEvictParam;
import io.nicheblog.dreamdiary.infrastructure.cache.util.EhCacheUtils;
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
        implements JrnlCacheEvictor {

    /**
     * 해당 컨텐츠 타입 관련 캐시를 제거한다.
     *
     * @param param 캐시 삭제 파라미터 객체
     */
    @Override
    @Transactional
    public void evict(final JrnlCacheEvictParam param) throws Exception {
        final ContentType refContentType = ContentType.JRNL_TODO;
        try {
            final String username = param.getRegstrId();
            final Integer postNo = param.getPostNo();
            final Integer yy = param.getYy();
            final Integer mnth = param.getMnth();
            // jrnl_todo
            this.evictMyYyMnthCache(username, "jrnlTodoListByUser", yy, mnth);
            EhCacheUtils.evictUserCacheByKey("jrnlTodoDtlDtoByUser", username, postNo);
            // 태그 캐시 처리
            EhCacheUtils.evictCacheByKey("tagContentEntityListByRef", postNo + "_JRNL_TODO");
        } catch (final Exception e) {
            log.error("CacheEvictor error [{}]: {}", refContentType, e.getMessage(), e);
            throw e;
        }
    }
}
