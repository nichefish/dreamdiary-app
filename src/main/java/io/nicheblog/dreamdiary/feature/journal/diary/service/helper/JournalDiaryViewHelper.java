package io.nicheblog.dreamdiary.feature.journal.diary.service.helper;

import io.nicheblog.dreamdiary.feature.attachable.state.StateKey;
import io.nicheblog.dreamdiary.feature.journal._shared.state.JournalState;
import io.nicheblog.dreamdiary.feature.journal.diary.model.JournalDiaryDto;
import io.nicheblog.dreamdiary.feature.journal.interpretation.service.helper.JournalInterpretationViewHelper;
import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
 * JournalDiaryViewHelper
 *
 * @author nichefish
 */
@UtilityClass
public class JournalDiaryViewHelper {

    /**
     * 캐시에 저장된 상태 맵(diary)을 기준으로 조회된 {@link JournalDiaryDto} 트리 구조에 상태를 반영한다.
     *
     * @param listDto 조회된 저널 일자 목록 DTO
     * @param diaryMap diary id → {@link JournalState} 맵
     */
    public static void applyStates(
        final List<JournalDiaryDto> listDto,
        final Map<Integer, JournalState> diaryMap,
        final Map<Integer, JournalState> interpretationMap
    ) {
        if (CollectionUtils.isEmpty(listDto)) return;
        for (final JournalDiaryDto diary : listDto) {
            final JournalState d = diaryMap.get(diary.getId());
            if (d != null) {
                diary.state.apply(StateKey.COLLAPSED, d.getCollapsed());
                diary.state.apply(StateKey.RESOLVED, d.getResolved());
                diary.state.apply(StateKey.IMPRTC, d.getImprtc());
                diary.state.apply(StateKey.REFRNC, d.getRefrnc());
            }

            JournalInterpretationViewHelper.applyState(diary.getJournalInterpretationList(), interpretationMap);
        }
    }
}
