package io.nicheblog.dreamdiary.feature.journal.entry.service.helper;

import io.nicheblog.dreamdiary.feature.attachable.state.StateKey;
import io.nicheblog.dreamdiary.feature.journal._shared.lifecycle.JournalLifecycleViewHelper;
import io.nicheblog.dreamdiary.feature.journal._shared.state.JournalState;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryDto;
import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Map;

@UtilityClass
public class JournalEntryStateViewHelper {

    /**
     * 엔트리 상태값을 DTO 상태 필드에 반영한다.
     *
     * @param listDto 병합 대상 entry DTO 목록
     * @param stateMap entry ID 기준 state map
     */
    public static void applyStates(
            final List<JournalEntryDto> listDto,
            final Map<Integer, JournalState> stateMap
    ) {
        if (CollectionUtils.isEmpty(listDto)) return;

        for (final JournalEntryDto dto : listDto) {
            final JournalState state = stateMap.get(dto.getKey());
            if (state == null) continue;

            dto.getState().apply(StateKey.COLLAPSED, state.getCollapsed());
            dto.getState().apply(StateKey.IMPRTC, state.getImprtc());
            dto.getState().apply(StateKey.REFRNC, state.getRefrnc());
        }
    }

    /**
     * 엔트리 상태와 하위 Reflection 상태를 함께 반영한다.
     *
     * @param listDto 대상 목록
     * @param stateMap 상태 맵
     * @param reflectionStateMap Reflection 상태 맵
     */
    public static void applyStates(
            final List<JournalEntryDto> listDto,
            final Map<Integer, JournalState> stateMap,
            final Map<Integer, JournalState> reflectionStateMap
    ) {
        applyStates(listDto, stateMap, Map.of(), reflectionStateMap, Map.of());
    }

    public static void applyStates(
            final List<JournalEntryDto> listDto,
            final Map<Integer, JournalState> stateMap,
            final Map<Integer, String> lifecycleMap,
            final Map<Integer, JournalState> reflectionStateMap
    ) {
        applyStates(listDto, stateMap, lifecycleMap, reflectionStateMap, Map.of());
    }

    /**
     * 일기 state/라이프사이클과 하위 Reflection state/라이프사이클을 함께 적용한다.
     * Reflection 은 entry 이므로 하위 목록에도 entry-level 병합을 재사용한다.
     *
     * @param listDto 대상 목록
     * @param stateMap entry 상태 맵
     * @param lifecycleMap entry 라이프사이클 맵
     * @param reflectionStateMap 하위 Reflection 상태 맵
     * @param reflectionLifecycleMap 하위 Reflection 라이프사이클 맵
     */
    public static void applyStates(
            final List<JournalEntryDto> listDto,
            final Map<Integer, JournalState> stateMap,
            final Map<Integer, String> lifecycleMap,
            final Map<Integer, JournalState> reflectionStateMap,
            final Map<Integer, String> reflectionLifecycleMap
    ) {
        applyStates(listDto, stateMap);
        JournalLifecycleViewHelper.applyEntryLifecycle(listDto, lifecycleMap);
        if (CollectionUtils.isEmpty(listDto)) return;

        for (final JournalEntryDto dto : listDto) {
            final List<JournalEntryDto> childReflections = dto.getReflectionList();
            if (CollectionUtils.isEmpty(childReflections)) continue;
            applyStates(childReflections, reflectionStateMap);
            JournalLifecycleViewHelper.applyEntryLifecycle(childReflections, reflectionLifecycleMap);
        }
    }

    /**
     * 꿈 일기 state/라이프사이클과 하위 Reflection state/라이프사이클을 함께 적용한다.
     *
     * @param listDto 병합 대상 dream entry DTO 목록
     * @param stateMap dream entry ID 기준 state map
     * @param reflectionStateMap 하위 Reflection state map
     */
    public static void applyDreamStates(
            final List<JournalEntryDto> listDto,
            final Map<Integer, JournalState> stateMap,
            final Map<Integer, JournalState> reflectionStateMap
    ) {
        applyDreamStates(listDto, stateMap, Map.of(), reflectionStateMap, Map.of());
    }

    public static void applyDreamStates(
            final List<JournalEntryDto> listDto,
            final Map<Integer, JournalState> stateMap,
            final Map<Integer, String> lifecycleMap,
            final Map<Integer, JournalState> reflectionStateMap
    ) {
        applyDreamStates(listDto, stateMap, lifecycleMap, reflectionStateMap, Map.of());
    }

    public static void applyDreamStates(
            final List<JournalEntryDto> listDto,
            final Map<Integer, JournalState> stateMap,
            final Map<Integer, String> lifecycleMap,
            final Map<Integer, JournalState> reflectionStateMap,
            final Map<Integer, String> reflectionLifecycleMap
    ) {
        if (CollectionUtils.isEmpty(listDto)) return;

        for (final JournalEntryDto dto : listDto) {
            final JournalState state = stateMap.get(dto.getKey());
            if (state != null) {
                dto.getState().apply(StateKey.COLLAPSED, state.getCollapsed());
                dto.getState().apply(StateKey.IMPRTC, state.getImprtc());
                dto.getState().apply(StateKey.REFRNC, state.getRefrnc());
                dto.getState().apply(StateKey.NHTMR, Boolean.TRUE.equals(state.getNhtmr()));
                dto.getState().apply(StateKey.HALLUC, Boolean.TRUE.equals(state.getHalluc()));
            }
            JournalLifecycleViewHelper.applyEntryLifecycle(List.of(dto), lifecycleMap);

            final List<JournalEntryDto> childReflections = dto.getReflectionList();
            if (!CollectionUtils.isEmpty(childReflections)) {
                applyStates(childReflections, reflectionStateMap);
                JournalLifecycleViewHelper.applyEntryLifecycle(childReflections, reflectionLifecycleMap);
            }
        }
    }
}
