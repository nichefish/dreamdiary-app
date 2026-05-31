package io.nicheblog.dreamdiary.feature.attachable.tag.handler;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.journal.day.model.JournalDayTagQuery;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryTagQuery;
import io.nicheblog.dreamdiary.infrastructure.cache.util.EhCacheUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * JournalTagCacheUpdtWorker
 * <pre>
 *  Journal tag cache evict worker.
 * </pre>
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class JournalTagCacheUpdtWorker {

    @Transactional
    public void handle(final String contentType, final String username, final Integer yy, final Integer mnth) {
        if (StringUtils.isBlank(contentType) || StringUtils.isBlank(username) || yy == null || mnth == null) return;

        final ContentType resolvedType = ContentType.get(contentType);
        if (ContentType.DEFAULT.equals(resolvedType)) return;

        if (ContentType.JOURNAL_DAY.equals(resolvedType)) {
            evictJournalDayCaches(username, yy, mnth);
            return;
        }

        if (isJournalEntryType(resolvedType)) {
            evictJournalEntryCaches(username, yy, mnth, resolvedType);
        }
    }

    private void evictJournalDayCaches(final String username, final Integer yy, final Integer mnth) {
        EhCacheUtils.evictUserCacheByKey(
                "journalDayPeriodTagListByUser",
                username,
                JournalDayTagQuery.of(yy, mnth)
        );
        EhCacheUtils.evictUserCacheByKey(
                "journalDayTagCountMapByUser",
                username,
                JournalDayTagQuery.of(yy, mnth)
        );
        EhCacheUtils.clearUserCache("journalDayPeriodTagListByUser", username);
        EhCacheUtils.clearUserCache("journalDayTagCountMapByUser", username);
        EhCacheUtils.clearUserCache("journalDayTagCategoryMapByUser", username);
    }

    private void evictJournalEntryCaches(
            final String username,
            final Integer yy,
            final Integer mnth,
            final ContentType contentType
    ) {
        EhCacheUtils.evictUserCacheByKey("journalEntryTagListByUser", username, contentType);
        EhCacheUtils.evictUserCacheByKey("journalEntryTagCategoryMapByUser", username, contentType);
        EhCacheUtils.evictUserCacheByKey(
                "journalEntryPeriodTagListByUser",
                username,
                JournalEntryTagQuery.of(contentType, yy, mnth)
        );
        EhCacheUtils.evictUserCacheByKey(
                "journalEntryTagCountMapByUser",
                username,
                JournalEntryTagQuery.of(contentType, yy, mnth)
        );
        EhCacheUtils.clearUserCache("journalEntryPeriodTagListByUser", username);
        EhCacheUtils.clearUserCache("journalEntryTagCountMapByUser", username);
    }

    private boolean isJournalEntryType(final ContentType contentType) {
        return ContentType.JOURNAL_DIARY.equals(contentType)
                || ContentType.JOURNAL_DREAM.equals(contentType);
    }
}
