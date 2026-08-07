package io.nicheblog.dreamdiary.feature.journal.entry.service.policy;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;

import java.util.List;

/**
 * 엔트리 태그·검색 축(일기/꿈)과 contentType 키 집합을 해석한다.
 * <p>
 * 일기 축 태그 <b>집계</b>(클라우드·결산·챕터 요약)는 {@code JOURNAL_DIARY} 와
 * {@code JOURNAL_REFLECTION} 을 같은 축으로 본다({@link #expandKeys}). Reflection 태그는
 * {@code tag_content.ref_content_type = JOURNAL_REFLECTION} 으로 저장되고 일기 축 집계 시 합쳐진다.
 * </p>
 * <p>
 * 반면 엔트리 목록 <b>검색</b>의 태그·state 스코프는 요청 타입 단독을 쓴다({@link #searchScopeKeys}).
 * Reflection 은 별도 Aggregate(journal_reflection)이고 대상 필수(About-A)라 검색 결과 행이 되지 않으며,
 * Reflection 본문 키워드만 대상 Primary 를 매칭시킨다(원문·해석 한 몸, {@code JournalEntrySpec} EXISTS).
 * 태그·state 는 대상을 매칭시키지 않는다.
 * </p>
 */
public final class JournalEntryTagAxis {

    private JournalEntryTagAxis() {
    }

    /**
     * 일기 축에 속하는 contentType 키 목록을 반환한다.
     *
     * @return {@code JOURNAL_DIARY}, {@code JOURNAL_REFLECTION}
     */
    public static List<String> diaryAxisKeys() {
        return List.of(ContentType.JOURNAL_DIARY.key, ContentType.JOURNAL_REFLECTION.key);
    }

    /**
     * 태그 <b>집계</b> 요청 contentType 을 실제 IN 조건용 키 목록으로 펼친다.
     * 일기({@code JOURNAL_DIARY}) 요청은 일기 축 전체(일기∪Reflection)를, 그 외는 요청 타입 단독을 쓴다.
     * 검색 스코프에는 {@link #searchScopeKeys} 를 쓴다(Reflection 미포함).
     *
     * @param contentType 집계 요청 타입
     * @return IN 조건용 contentType 키 목록
     */
    public static List<String> expandKeys(final ContentType contentType) {
        if (contentType == ContentType.JOURNAL_DIARY) {
            return diaryAxisKeys();
        }
        if (contentType == null || contentType == ContentType.DEFAULT) {
            return List.of();
        }
        return List.of(contentType.key);
    }

    /**
     * 엔트리 목록 <b>검색</b>의 태그·state 스코프용 contentType 키 목록을 반환한다.
     * 검색 결과 행은 요청 타입의 Primary 엔트리만이므로 Reflection 을 축에 합치지 않고 요청 타입 단독을 쓴다.
     *
     * @param contentType 검색 요청 타입
     * @return IN 조건용 contentType 키 목록 (요청 타입 단독)
     */
    public static List<String> searchScopeKeys(final ContentType contentType) {
        if (contentType == null || contentType == ContentType.DEFAULT) {
            return List.of();
        }
        return List.of(contentType.key);
    }

    /**
     * Reflection 태그 변경 시 일기 축 캐시도 같이 비울지 여부.
     *
     * @param contentType 저장·삭제된 콘텐츠 타입
     * @return 일기 축 캐시 동반 무효화 여부
     */
    public static boolean evictsDiaryAxis(final ContentType contentType) {
        return contentType == ContentType.JOURNAL_REFLECTION;
    }
}
