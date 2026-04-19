package io.nicheblog.dreamdiary.feature.journal.note.service.strategy;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.journal._shared.handler.JournalCacheEvictor;
import io.nicheblog.dreamdiary.feature.journal._shared.model.JournalCacheEvictParam;
import io.nicheblog.dreamdiary.infrastructure.cache.util.EhCacheUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * JournalNoteCacheEvictor
 * <p>
 *  저널 노트 관련 캐시 evictor.
 * </p>
 *
 * @author nichefish
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class JournalNoteCacheEvictor
        implements JournalCacheEvictor {

    /**
     * 해당 컨텐츠 타입 관련 캐시를 제거한다.
     *
     * @param param 캐시 삭제 파라미터 객체
     */
    @Override
    @Transactional
    public void evict(final JournalCacheEvictParam param) throws Exception {
        final ContentType refContentType = ContentType.JOURNAL_NOTE;
        try {
            final String username = param.getCreatedBy();
            final Integer id = param.getId();
            final Integer journalDayId = param.getJournalDayId();
            final Integer yy = param.getYy();
            final Integer mnth = param.getMnth();
            final String weekStartDt = param.getWeekStartDt();
            // journal_note
            this.evictMyYyCacheByYyPrefix(username, "journalNoteYyAnnualStatedListByUser", yy);
            EhCacheUtils.evictUserCacheByKey("journalNoteDtlDtoByUser", username, id);
            // journal_day
            if (journalDayId != null) {
                EhCacheUtils.evictUserCacheByKey("journalDayDtlDtoByUser", username, journalDayId);
            }
            this.evictMyJournalDayYyMnthCaches(username, yy, mnth);
            this.evictMyJournalDayWeeklyCaches(username, weekStartDt);
            // journal_note_tag
            EhCacheUtils.clearUserCache("journalNoteTagCtgrMapByUser", username);
            EhCacheUtils.clearUserCache("journalNoteTagListByUser", username);
            EhCacheUtils.clearUserCache("journalNoteYyMnthTagListByUser", username);
            EhCacheUtils.clearUserCache("journalNoteWeeklyTagListByUser", username);
            EhCacheUtils.clearUserCache("journalNoteYyMnthSizedTagListByUser", username);
            EhCacheUtils.clearUserCache("journalNoteWeeklySizedTagListByUser", username);
            EhCacheUtils.clearUserCache("journalNoteCountMapByUser", username);

            // 태그 캐시 처리
            if (id != null) {
                EhCacheUtils.evictCacheByKey("tagContentEntityListByRef", id + "_JOURNAL_NOTE");
            }
        } catch (final Exception e) {
            log.error("CacheEvictor error [{}]: {}", refContentType, e.getMessage(), e);
            throw e;
        }
    }
}

