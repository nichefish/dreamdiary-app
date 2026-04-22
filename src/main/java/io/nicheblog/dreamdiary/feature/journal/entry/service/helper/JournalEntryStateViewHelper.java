package io.nicheblog.dreamdiary.feature.journal.entry.service.helper;

import io.nicheblog.dreamdiary.feature.attachable.state.StateKey;
import io.nicheblog.dreamdiary.feature.journal._shared.state.JournalState;
import io.nicheblog.dreamdiary.feature.journal.entry.model.JournalEntryDto;
import io.nicheblog.dreamdiary.feature.journal.interpretation.service.helper.JournalInterpretationViewHelper;
import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Map;

@UtilityClass
public class JournalEntryStateViewHelper {

    /**
     * 엔트리 상태값을 DTO 상태 필드에 반영한다.
     *
     * @param listDto 대상 목록
     * @param stateMap 상태 맵
     * @param <Dto> 상태 표시 DTO 타입
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
            dto.getState().apply(StateKey.RESOLVED, state.getResolved());
            dto.getState().apply(StateKey.IMPRTC, state.getImprtc());
            dto.getState().apply(StateKey.REFRNC, state.getRefrnc());
        }
    }

    /**
     * 엔트리 상태와 해석 상태를 함께 반영한다.
     *
     * @param listDto 대상 목록
     * @param stateMap 상태 맵
     * @param interpretationMap 해석 상태 맵
     * @param <Dto> 해석 포함 상태 DTO 타입
     */
    public static void applyStates(
            final List<JournalEntryDto> listDto,
            final Map<Integer, JournalState> stateMap,
            final Map<Integer, JournalState> interpretationMap
    ) {
        applyStates(listDto, stateMap);
        if (CollectionUtils.isEmpty(listDto)) return;

        for (final JournalEntryDto dto : listDto) {
            JournalInterpretationViewHelper.applyState(dto.getJournalInterpretationList(), interpretationMap);
        }
    }

    /**
     * 꿈 기록 전용 상태(악몽/환각 포함)와 해석 상태를 반영한다.
     *
     * @param listDto 대상 목록
     * @param stateMap 상태 맵
     * @param interpretationMap 해석 상태 맵
     * @param <Dto> 해석 포함 상태 DTO 타입
     */
    public static void applyDreamStates(
            final List<JournalEntryDto> listDto,
            final Map<Integer, JournalState> stateMap,
            final Map<Integer, JournalState> interpretationMap
    ) {
        if (CollectionUtils.isEmpty(listDto)) return;

        for (final JournalEntryDto dto : listDto) {
            final JournalState state = stateMap.get(dto.getKey());
            if (state != null) {
                dto.getState().apply(StateKey.COLLAPSED, state.getCollapsed());
                dto.getState().apply(StateKey.RESOLVED, state.getResolved());
                dto.getState().apply(StateKey.IMPRTC, state.getImprtc());
                dto.getState().apply(StateKey.REFRNC, state.getRefrnc());
                dto.getState().apply(StateKey.NHTMR, Boolean.TRUE.equals(state.getNhtmr()));
                dto.getState().apply(StateKey.HALLUC, Boolean.TRUE.equals(state.getHalluc()));
            }

            JournalInterpretationViewHelper.applyState(dto.getJournalInterpretationList(), interpretationMap);
        }
    }
}
