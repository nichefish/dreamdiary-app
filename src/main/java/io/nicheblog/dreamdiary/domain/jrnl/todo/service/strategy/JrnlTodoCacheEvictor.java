package io.nicheblog.dreamdiary.domain.jrnl.todo.service.strategy;

import io.nicheblog.dreamdiary.domain.jrnl.todo.entity.JrnlTodoEntity;
import io.nicheblog.dreamdiary.extension.cache.event.JrnlCacheEvictEvent;
import io.nicheblog.dreamdiary.extension.cache.model.JrnlCacheEvictParam;
import io.nicheblog.dreamdiary.extension.cache.service.CacheEvictor;
import io.nicheblog.dreamdiary.extension.cache.util.EhCacheUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

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
public class JrnlTodoCacheEvictor
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
        final Integer yy = param.getYy();
        final Integer mnth = param.getMnth();
        // jrnl_todo
        this.evictMyCacheForPeriod("myJrnlTodoList", yy, mnth);
        // 태그 처리
        EhCacheUtils.evictCache("contentTagEntityListByRef", postNo + "_JRNL_TODO");
        // L2캐시 처리
        EhCacheUtils.clearL2Cache(JrnlTodoEntity.class);
    }
}
