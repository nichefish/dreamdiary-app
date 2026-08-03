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
        @Override public boolean canBeReflectionTarget() { return true; }
        @Override public String stateCacheName() { return JournalStateCacheRegistry.monthlyMapCacheName(ContentType.JOURNAL_DIARY); }
        @Override public String lifecycleCacheName() { return JournalLifecycleCacheRegistry.monthlyMapCacheName(ContentType.JOURNAL_DIARY); }
        @Override public Integer resolveModifiedChapterId(Integer dtoChapterId, Integer entityChapterId) {
            return dtoChapterId;
        }
    },

    DREAM(ContentType.JOURNAL_DREAM, ChapterType.DREAM) {
        @Override public boolean supportsChapterChange() { return false; }
        @Override public boolean canBeReflectionTarget() { return true; }
        @Override public String stateCacheName() { return JournalStateCacheRegistry.monthlyMapCacheName(ContentType.JOURNAL_DREAM); }
        @Override public String lifecycleCacheName() { return JournalLifecycleCacheRegistry.monthlyMapCacheName(ContentType.JOURNAL_DREAM); }
        @Override public Integer resolveModifiedChapterId(Integer dtoChapterId, Integer entityChapterId) {
            return dtoChapterId != null ? dtoChapterId : entityChapterId;
        }
    },

    /**
     * Reflection = 별도 Aggregate(journal_reflection)의 콘텐츠 타입. Entry 스트림의 chapter 를 갖지 않으므로
     * expectedChapterType 는 null 이다. 이 정책 항목은 Reflection 을 reflection target 타입으로 분류하고
     * state·lifecycle 캐시명을 제공하는 데 쓰인다. 쓰기는 {@code JournalReflectionService} 가 담당한다.
     */
    REFLECTION(ContentType.JOURNAL_REFLECTION, null) {
        @Override public boolean supportsChapterChange() { return true; }
        @Override public boolean canBeReflectionTarget() { return true; }
        @Override public String stateCacheName() { return JournalStateCacheRegistry.monthlyMapCacheName(ContentType.JOURNAL_REFLECTION); }
        @Override public String lifecycleCacheName() { return JournalLifecycleCacheRegistry.monthlyMapCacheName(ContentType.JOURNAL_REFLECTION); }
        @Override public Integer resolveModifiedChapterId(Integer dtoChapterId, Integer entityChapterId) {
            return dtoChapterId != null ? dtoChapterId : entityChapterId;
        }
    };

    public final ContentType contentType;
    /** 이 타입을 담는 chapter 타입. REFLECTION은 본질 타입이라 chapter가 타입을 지시하지 않으므로 null(universal placement). */
    public final ChapterType expectedChapterType;

    JournalEntryTypePolicy(ContentType contentType, ChapterType expectedChapterType) {
        this.contentType = contentType;
        this.expectedChapterType = expectedChapterType;
    }

    /** 챕터 변경 지원 여부. */
    public abstract boolean supportsChapterChange();

    /** Reflection target 가능 여부. reflection 이 이 타입을 target 으로 가리킬 수 있는가(DIARY/DREAM/REFLECTION). */
    public abstract boolean canBeReflectionTarget();

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
            case JOURNAL_REFLECTION -> REFLECTION;
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
     * Reflection target 이 될 수 있는 정책 목록을 반환한다.
     *
     * @return reflection target 가능 정책 목록 (DIARY/DREAM/REFLECTION)
     */
    public static List<JournalEntryTypePolicy> reflectionTargetTypes() {
        return REFLECTION_TARGET_TYPES;
    }

    private static final List<JournalEntryTypePolicy> REFLECTION_TARGET_TYPES;

    static {
        REFLECTION_TARGET_TYPES = Arrays.stream(values())
                .filter(JournalEntryTypePolicy::canBeReflectionTarget)
                .toList();
    }
}
