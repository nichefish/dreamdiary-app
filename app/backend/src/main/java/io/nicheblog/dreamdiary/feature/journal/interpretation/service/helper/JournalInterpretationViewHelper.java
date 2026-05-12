package io.nicheblog.dreamdiary.feature.journal.interpretation.service.helper;

import io.nicheblog.dreamdiary.feature.attachable.state.StateKey;
import io.nicheblog.dreamdiary.feature.journal._shared.state.JournalState;
import io.nicheblog.dreamdiary.feature.journal.interpretation.model.JournalInterpretationDto;
import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
 * JournalInterpretationViewHelper
 * 저널 해석 뷰 모델에 상태 캐시 값을 반영한다.
 *
 * @author nichefish
 */
@UtilityClass
public class JournalInterpretationViewHelper {

    /**
     * 캐시에 저장된 state 맵을 기준으로 해석 트리에 상태를 반영한다.
     *
     * @param listDto 조회 대상 해석 목록 DTO
     * @param interpretationMap interpretation id 기준 state 맵
     */
    public static void applyState(List<JournalInterpretationDto> listDto, Map<Integer, JournalState> interpretationMap) {
        if (CollectionUtils.isEmpty(listDto)) return;
        for (final JournalInterpretationDto interpretation : listDto) {
            final JournalState d = interpretationMap.get(interpretation.getId());
            if (d != null) {
                interpretation.state.apply(StateKey.COLLAPSED, d.getCollapsed());
            }
        }
    }
}
