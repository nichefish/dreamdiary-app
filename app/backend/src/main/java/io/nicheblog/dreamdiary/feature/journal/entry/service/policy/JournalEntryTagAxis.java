package io.nicheblog.dreamdiary.feature.journal.entry.service.policy;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;

import java.util.List;

/**
 * 엔트리 태그·검색 축(일기/꿈)과 contentType 키 집합을 해석한다.
 * <p>
 * 태그를 소유하는 엔트리 타입은 {@code JOURNAL_DIARY}, {@code JOURNAL_DREAM} 이며 집계와 검색은
 * 요청 타입 단독 축을 사용한다. Reflection 은 별도 Aggregate({@code journal_reflection})이고 태그를
 * 소유하지 않으므로 태그 축에 참여하지 않는다.
 * </p>
 */
public final class JournalEntryTagAxis {

    private JournalEntryTagAxis() {
    }

    /**
     * 태그 집계 요청 contentType 을 실제 IN 조건용 키 목록으로 변환한다.
     * 태그 지원 타입은 요청 타입 단독을 반환하고, 미지원 타입은 빈 목록을 반환한다.
     *
     * @param contentType 집계 요청 타입
     * @return IN 조건용 contentType 키 목록
     */
    public static List<String> expandKeys(final ContentType contentType) {
        if (!supportsTags(contentType)) return List.of();
        return List.of(contentType.key);
    }

    /**
     * 엔트리 목록 검색의 태그·state 스코프용 contentType 키 목록을 반환한다.
     * 태그 지원 타입은 요청 타입 단독을 반환하고, 미지원 타입은 빈 목록을 반환한다.
     *
     * @param contentType 검색 요청 타입
     * @return IN 조건용 contentType 키 목록 (요청 타입 단독)
     */
    public static List<String> searchScopeKeys(final ContentType contentType) {
        if (!supportsTags(contentType)) return List.of();
        return List.of(contentType.key);
    }

    /**
     * 엔트리 타입이 태그를 지원하는지 확인한다.
     *
     * @param contentType 콘텐츠 타입
     * @return 일기·꿈이면 {@code true}
     */
    public static boolean supportsTags(final ContentType contentType) {
        return contentType == ContentType.JOURNAL_DIARY
                || contentType == ContentType.JOURNAL_DREAM;
    }
}
