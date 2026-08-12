package io.nicheblog.dreamdiary.feature.attachable.tag.handler;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.journal.day.model.JournalDayTagQuery;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryTagQuery;
import io.nicheblog.dreamdiary.feature.journal.entry.service.policy.JournalEntryTagAxis;
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
 * <p>
 * 저널 태그 변경 후 파생 조회 캐시를 무효화한다.
 * 일자 태그는 일자 전용 기간·건수·카테고리 캐시를, 엔트리 태그는 DIARY/DREAM 타입별
 * 목록·기간·건수·카테고리 캐시를 무효화한다. 태그를 소유하지 않는 Reflection은
 * 엔트리 태그 캐시 무효화 대상에 포함하지 않는다.
 * </p>
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class JournalTagCacheUpdtWorker {

    /**
     * 태그가 변경된 저널 콘텐츠의 캐시 범위를 해석해 무효화한다.
     * 필수 식별 정보가 없거나 지원하지 않는 콘텐츠 타입이면 캐시를 변경하지 않는다.
     *
     * @param contentType 변경된 콘텐츠 타입 키
     * @param username 캐시 소유 사용자 아이디
     * @param yy 변경 데이터의 기준 연도
     * @param mnth 변경 데이터의 기준 월
     */
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

    /**
     * 일자 태그의 기간·건수·카테고리 캐시를 사용자 단위로 무효화한다.
     *
     * @param username 캐시 소유 사용자 아이디
     * @param yy 변경 데이터의 기준 연도
     * @param mnth 변경 데이터의 기준 월
     */
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

    /**
     * 엔트리 태그의 타입별 목록·기간·건수·카테고리 캐시를 무효화한다.
     * 기간·건수 캐시는 여러 기간 키를 공유하므로 정확 키와 사용자 전체 키를 함께 정리한다.
     *
     * @param username 캐시 소유 사용자 아이디
     * @param yy 변경 데이터의 기준 연도
     * @param mnth 변경 데이터의 기준 월
     * @param contentType 태그를 소유하는 엔트리 타입
     */
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

    /**
     * 엔트리 태그 캐시를 보유하는 콘텐츠 타입인지 확인한다.
     *
     * @param contentType 확인할 콘텐츠 타입
     * @return DIARY 또는 DREAM이면 {@code true}
     */
    private boolean isJournalEntryType(final ContentType contentType) {
        return JournalEntryTagAxis.supportsTags(contentType);
    }
}
