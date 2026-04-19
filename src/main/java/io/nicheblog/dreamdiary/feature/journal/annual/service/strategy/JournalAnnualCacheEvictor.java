package io.nicheblog.dreamdiary.feature.journal.annual.service.strategy;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.journal._shared.handler.JournalCacheEvictor;
import io.nicheblog.dreamdiary.feature.journal._shared.model.JournalCacheEvictParam;
import io.nicheblog.dreamdiary.infrastructure.cache.util.EhCacheUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * JournalAnnualCacheEvictor
 * <p>
 *  저널 결산 관련 캐시 evictor.
 * </p>
 *
 * @author nichefish
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class JournalAnnualCacheEvictor
        implements JournalCacheEvictor {

    /**
     * 해당 컨텐츠 타입 관련 캐시를 제거한다.
     *
     * @param param 캐시 삭제 파라미터 객체
     */
    @Override
    @Transactional
    public void evict(final JournalCacheEvictParam param) throws Exception {
        final ContentType refContentType = ContentType.JOURNAL_ANNUAL;
        try {
            final String username = param.getCreatedBy();
            final Integer id = param.getId();
            // 목록 캐시 초기화
            EhCacheUtils.clearUserCache("journalAnnualListByUser", username);
            EhCacheUtils.clearUserCache("journalAnnualTotalListByUser", username);
            // 상세 캐시 초기화
            EhCacheUtils.evictUserCacheByKey("journalAnnualDtlDtoByUser", username, id);
            EhCacheUtils.evictUserCacheByKey("journalAnnualYyDtlDtoByUser", username, param.getYy());
            // 태그 캐시 처리
            EhCacheUtils.evictCacheByKey("tagContentEntityListByRef", id + "_JOURNAL_ANNUAL");
        } catch (final Exception e) {
            log.error("CacheEvictor error [{}]: {}", refContentType, e.getMessage(), e);
            throw e;
        }
    }
}

