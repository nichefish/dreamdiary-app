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
 *
 * @author nichefish
 */
@UtilityClass
public class JournalInterpretationViewHelper {

    /**
     * 캐시에 저장된 상태 맵을 기준으로 조회된 {@link JournalInterpretationDto} 트리 구조에 상태를 반영한다.
     *
     * @param listDto 조회된 저널 일자 목록 DTO
     * @param interpretationMap interpretation id → {@link JournalState} map
     */
    public static void applyState(List<JournalInterpretationDto> listDto, Map<Integer, JournalState> interpretationMap) {
        if (CollectionUtils.isEmpty(listDto)) return;
        for (final JournalInterpretationDto interpretation : listDto) {
            final JournalState d = interpretationMap.get(interpretation.getId());
            if (d != null) {
                interpretation.state.apply(StateKey.COLLAPSED, d.getCollapsed());
                interpretation.state.apply(StateKey.RESOLVED, d.getResolved());
            }
        }
    }
}
