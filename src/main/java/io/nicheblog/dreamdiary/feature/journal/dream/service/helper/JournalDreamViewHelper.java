package io.nicheblog.dreamdiary.feature.journal.dream.service.helper;

import io.nicheblog.dreamdiary.feature.clsf.state.StateCd;
import io.nicheblog.dreamdiary.feature.journal._shared.state.JournalState;
import io.nicheblog.dreamdiary.feature.journal.dream.model.JournalDreamDto;
import io.nicheblog.dreamdiary.feature.journal.intrpt.service.helper.JournalIntrptViewHelper;
import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
 * JournalDreamViewHelper
 *
 * @author nichefish
 */
@UtilityClass
public class JournalDreamViewHelper {

    /**
     * 캐시에 저장된 상태 맵을 기준으로 조회된 {@link JournalDreamDto} 트리 구조에 상태를 반영한다.
     *
     * @param listDto 조회된 저널 일자 목록 DTO
     * @param dreamMap dream id → {@link JournalState} 맵
     * @param intrptMap intrpt id → {@link JournalState} 맵
     */
    public static void applyStates(List<JournalDreamDto> listDto, Map<Integer, JournalState> dreamMap, Map<Integer, JournalState> intrptMap) {
        if (CollectionUtils.isEmpty(listDto)) return;

        for (final JournalDreamDto dream : listDto) {

            final JournalState s = dreamMap.get(dream.getId());
            if (s != null) {
                dream.state.apply(StateCd.COLLAPSED, s.getCollapsed());
                dream.state.apply(StateCd.RESOLVED, s.getResolved());
                dream.state.apply(StateCd.IMPRTC, s.getImprtc());
                dream.state.apply(StateCd.REFRNC, s.getRefrnc());
            }

            JournalIntrptViewHelper.applyState(dream.getJournalIntrptList(), intrptMap);
        }
    }
}

