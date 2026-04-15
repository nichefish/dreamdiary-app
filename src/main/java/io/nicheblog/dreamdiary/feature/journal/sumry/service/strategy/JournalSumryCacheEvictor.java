package io.nicheblog.dreamdiary.feature.journal.sumry.service.strategy;

import io.nicheblog.dreamdiary.feature.clsf._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.journal._shared.handler.JournalCacheEvictor;
import io.nicheblog.dreamdiary.feature.journal._shared.model.JournalCacheEvictParam;
import io.nicheblog.dreamdiary.infrastructure.cache.util.EhCacheUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * JournalSumryCacheEvictor
 * <p>
 *  저널 결산 관련 캐시 evictor.
 * </p>
 *
 * @author nichefish
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class JournalSumryCacheEvictor
        implements JournalCacheEvictor {

    /**
     * 해당 컨텐츠 타입 관련 캐시를 제거한다.
     *
     * @param param 캐시 삭제 파라미터 객체
     */
    @Override
    @Transactional
    public void evict(final JournalCacheEvictParam param) throws Exception {
        final ContentType refContentType = ContentType.JOURNAL_SUMRY;
        try {
            final String username = param.getCreatedBy();
            final Integer id = param.getId();
            // 목록 캐시 초기화
            EhCacheUtils.clearUserCache("journalSumryListByUser", username);
            EhCacheUtils.clearUserCache("journalSumryTotalListByUser", username);
            // 상세 캐시 초기화
            EhCacheUtils.evictUserCacheByKey("journalSumryDtlDtoByUser", username, id);
            EhCacheUtils.evictUserCacheByKey("journalSumryYyDtlDtoByUser", username, param.getYy());
            // 태그 캐시 처리
            EhCacheUtils.evictCacheByKey("tagContentEntityListByRef", id + "_JOURNAL_SUMRY");
        } catch (final Exception e) {
            log.error("CacheEvictor error [{}]: {}", refContentType, e.getMessage(), e);
            throw e;
        }
    }
}

