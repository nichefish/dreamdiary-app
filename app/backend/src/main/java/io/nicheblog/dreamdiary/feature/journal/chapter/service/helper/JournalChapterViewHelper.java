package io.nicheblog.dreamdiary.feature.journal.chapter.service.helper;

import io.nicheblog.dreamdiary.feature.attachable.state.StateKey;
import io.nicheblog.dreamdiary.feature.journal._shared.state.JournalState;
import io.nicheblog.dreamdiary.feature.journal.chapter.model.JournalChapterDto;
import io.nicheblog.dreamdiary.feature.journal.entry.service.helper.JournalEntryStateViewHelper;
import io.nicheblog.dreamdiary.feature.journal.entry.service.helper.JournalEntryViewProjectionHelper;
import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
 * JournalChapterViewHelper
 * 저널 챕터 트리에 일기·해석 등 하위 엔트리의 state·lifecycle 캐시를 반영한다.
 *
 * @author nichefish
 */
@UtilityClass
public class JournalChapterViewHelper {

    /**
     * 캐시에 저장된 entry/diary state 값을 chapter 트리에 반영한다.
     *
     * @param listDto 조회 대상 chapter 목록 DTO
     * @param chapterMap entry id 기준 state 맵
     * @param diaryMap diary id 기준 state 맵
     */
    public static void applyStates(
        final List<JournalChapterDto> listDto,
        final Map<Integer, JournalState> chapterMap,
        final Map<Integer, JournalState> diaryMap,
        final Map<Integer, JournalState> reflectionStateMap
    ) {
        applyStates(listDto, chapterMap, diaryMap, Map.of(), reflectionStateMap, Map.of());
    }

    /**
     * 캐시에 저장된 entry/diary state와 lifecycle 값을 chapter 트리에 반영한다.
     *
     * @param listDto 조회 대상 chapter 목록 DTO
     * @param chapterMap entry id 기준 state 맵
     * @param diaryMap diary id 기준 state 맵
     * @param diaryLifecycleMap diary id 기준 lifecycle 맵
     * @param reflectionStateMap reflection id 기준 state 맵
     * @param reflectionLifecycleMap reflection id 기준 lifecycle 맵
     */
    public static void applyStates(
        final List<JournalChapterDto> listDto,
        final Map<Integer, JournalState> chapterMap,
        final Map<Integer, JournalState> diaryMap,
        final Map<Integer, String> diaryLifecycleMap,
        final Map<Integer, JournalState> reflectionStateMap,
        final Map<Integer, String> reflectionLifecycleMap
    ) {

        if (CollectionUtils.isEmpty(listDto)) return;
        for (final JournalChapterDto entry : listDto) {

            final JournalState s = chapterMap.get(entry.getId());
            if (s != null) {
                entry.state.apply(StateKey.COLLAPSED, s.getCollapsed());
            }

            JournalEntryStateViewHelper.applyStates(
                    JournalEntryViewProjectionHelper.getDiaryEntries(entry),
                    diaryMap,
                    diaryLifecycleMap,
                    reflectionStateMap,
                    reflectionLifecycleMap
            );

            // 1급 독립 리플렉션(target 없거나 다른 챕터 target) — 임베드가 아니라 챕터 직속 엔트리라 별도 적용
            JournalEntryStateViewHelper.applyStates(
                    JournalEntryViewProjectionHelper.getReflectionEntries(entry),
                    reflectionStateMap,
                    reflectionLifecycleMap,
                    reflectionStateMap,
                    reflectionLifecycleMap
            );
        }
    }
}
