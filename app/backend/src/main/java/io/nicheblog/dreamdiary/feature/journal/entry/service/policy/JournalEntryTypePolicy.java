package io.nicheblog.dreamdiary.feature.journal.entry.service.policy;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.journal._shared.lifecycle.JournalLifecycleCacheRegistry;
import io.nicheblog.dreamdiary.feature.journal._shared.state.JournalStateCacheRegistry;
import io.nicheblog.dreamdiary.feature.journal.chapter.type.ChapterType;

import java.util.Arrays;
import java.util.List;

/**
 * 저널 항목 타입별 행동 정책.
 * DIARY/DREAM/NOTE 간 분기는 오직 이 정책 객체를 통해서만 표현한다.
 */
public enum JournalEntryTypePolicy {

    DIARY(ContentType.JOURNAL_DIARY, ChapterType.DIARY) {
        @Override public boolean supportsChapterChange() { return true; }
        @Override public boolean supportsInterpretation() { return true; }
        @Override public String stateCacheName() { return JournalStateCacheRegistry.monthlyMapCacheName(ContentType.JOURNAL_DIARY); }
        @Override public String lifecycleCacheName() { return JournalLifecycleCacheRegistry.monthlyMapCacheName(ContentType.JOURNAL_DIARY); }
        @Override public Integer resolveModifiedChapterId(Integer dtoChapterId, Integer entityChapterId) {
            return dtoChapterId;
        }
    },

    DREAM(ContentType.JOURNAL_DREAM, ChapterType.DREAM) {
        @Override public boolean supportsChapterChange() { return false; }
        @Override public boolean supportsInterpretation() { return true; }
        @Override public String stateCacheName() { return JournalStateCacheRegistry.monthlyMapCacheName(ContentType.JOURNAL_DREAM); }
        @Override public String lifecycleCacheName() { return JournalLifecycleCacheRegistry.monthlyMapCacheName(ContentType.JOURNAL_DREAM); }
        @Override public Integer resolveModifiedChapterId(Integer dtoChapterId, Integer entityChapterId) {
            return dtoChapterId != null ? dtoChapterId : entityChapterId;
        }
    };

    public final ContentType contentType;
    public final ChapterType expectedChapterType;

    JournalEntryTypePolicy(ContentType contentType, ChapterType expectedChapterType) {
        this.contentType = contentType;
        this.expectedChapterType = expectedChapterType;
    }

    /** 챕터 변경 지원 여부. */
    public abstract boolean supportsChapterChange();

    /** 해석(interpretation) 지원 여부. */
    public abstract boolean supportsInterpretation();

    /**
     * 월별 상태 캐시 이름. 상태 캐시가 없는 타입은 null 반환.
     * JournalEntryMyViewService의 state merge 여부 판단에 사용.
     */
    public abstract String stateCacheName();

    public abstract String lifecycleCacheName();

    /**
     * 수정 요청의 챕터 ID 결정.
     * DREAM은 dto가 null이면 기존 엔티티의 챕터를 유지한다.
     *
     * @param dtoChapterId 요청 DTO의 챕터 ID
     * @param entityChapterId 기존 엔티티의 챕터 ID
     * @return 최종 반영할 챕터 ID
     */
    public abstract Integer resolveModifiedChapterId(Integer dtoChapterId, Integer entityChapterId);

    /**
     * 콘텐츠 타입에 맞는 엔트리 정책을 반환한다.
     *
     * @param contentType 콘텐츠 타입
     * @return 엔트리 정책
     */
    public static JournalEntryTypePolicy from(final ContentType contentType) {
        return switch (contentType) {
            case JOURNAL_DREAM -> DREAM;
            default -> DIARY;
        };
    }

    /**
     * 해당 콘텐츠 타입이 journal entry 타입인지 확인한다.
     *
     * @param contentType 콘텐츠 타입
     * @return 엔트리 타입 여부
     */
    public static boolean isEntryType(final ContentType contentType) {
        if (contentType == null) return false;
        return Arrays.stream(values()).anyMatch(p -> p.contentType == contentType);
    }

    /**
     * interpretation을 지원하는 정책 목록을 반환한다.
     *
     * @return 해석 지원 정책 목록
     */
    public static List<JournalEntryTypePolicy> interpretableTypes() {
        return INTERPRETABLE_TYPES;
    }

    private static final List<JournalEntryTypePolicy> INTERPRETABLE_TYPES;

    static {
        INTERPRETABLE_TYPES = Arrays.stream(values())
                .filter(JournalEntryTypePolicy::supportsInterpretation)
                .toList();
    }
}
