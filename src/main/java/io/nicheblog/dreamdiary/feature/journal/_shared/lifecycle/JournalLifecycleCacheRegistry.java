package io.nicheblog.dreamdiary.feature.journal._shared.lifecycle;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import lombok.experimental.UtilityClass;

import java.util.List;

/**
 * 저널 라이프사이클 캐시명을 모아 두는 registry.
 *
 * <p>라이프사이클 캐시 맵은 기존 저널 state 캐시 맵 구조를 따른다.
 * 다만 라이프사이클은 단일 현재값이므로 컨텐츠 ID마다 문자열 키 하나만 보관한다.</p>
 */
@UtilityClass
public class JournalLifecycleCacheRegistry {

    private static final List<ContentType> LIFECYCLE_CONTENT_TYPES = List.of(
            ContentType.JOURNAL_DIARY,
            ContentType.JOURNAL_DREAM,
            ContentType.JOURNAL_INTERPRETATION
    );

    /**
     * 컨텐츠 타입에 저널 라이프사이클 캐시 맵이 등록되어 있는지 확인한다.
     *
     * @param contentType 확인할 컨텐츠 타입
     * @return 월간/주간 라이프사이클 맵이 있으면 {@code true}
     */
    public static boolean supports(final ContentType contentType) {
        return LIFECYCLE_CONTENT_TYPES.contains(contentType);
    }

    /**
     * 저널 라이프사이클 캐시 맵에 참여하는 컨텐츠 타입 목록을 반환한다.
     *
     * @return 지원 컨텐츠 타입 목록
     */
    public static List<ContentType> lifecycleContentTypes() {
        return LIFECYCLE_CONTENT_TYPES;
    }

    /**
     * 월간 라이프사이클 보조 맵 캐시명을 반환한다.
     *
     * @param contentType 저널 컨텐츠 타입
     * @return {@code id -> lifecycleKey} 캐시명
     */
    public static String monthlyMapCacheName(final ContentType contentType) {
        return switch (contentType) {
            case JOURNAL_DIARY -> "journalDiaryLifecycleMapByUser";
            case JOURNAL_DREAM -> "journalDreamLifecycleMapByUser";
            case JOURNAL_INTERPRETATION -> "journalInterpretationLifecycleMapByUser";
            default -> throw new IllegalStateException("Unexpected value: " + contentType);
        };
    }

    /**
     * 주간 라이프사이클 보조 맵 캐시명을 반환한다.
     *
     * @param contentType 저널 컨텐츠 타입
     * @return {@code id -> lifecycleKey} 캐시명
     */
    public static String weeklyMapCacheName(final ContentType contentType) {
        return switch (contentType) {
            case JOURNAL_DIARY -> "journalDiaryWeeklyLifecycleMapByUser";
            case JOURNAL_DREAM -> "journalDreamWeeklyLifecycleMapByUser";
            case JOURNAL_INTERPRETATION -> "journalInterpretationWeeklyLifecycleMapByUser";
            default -> throw new IllegalStateException("Unexpected value: " + contentType);
        };
    }

    /**
     * 라이프사이클 변경 후 비워야 하는 연간 목록 캐시명을 반환한다.
     *
     * @param contentType 저널 컨텐츠 타입
     * @return 연간 라이프사이클 반영 목록 캐시명. 없으면 {@code null}
     */
    public static String annualLifecycleListCacheName(final ContentType contentType) {
        return switch (contentType) {
            case JOURNAL_DIARY, JOURNAL_DREAM -> "journalEntryYyAnnualLifecycledListByUser";
            default -> null;
        };
    }
}
