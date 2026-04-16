package io.nicheblog.dreamdiary.feature.journal.intrpt.service.helper;

import io.nicheblog.dreamdiary.feature.attachable.state.StateCd;
import io.nicheblog.dreamdiary.feature.journal._shared.state.JournalState;
import io.nicheblog.dreamdiary.feature.journal.intrpt.model.JournalIntrptDto;
import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
 * JournalIntrptViewHelper
 *
 * @author nichefish
 */
@UtilityClass
public class JournalIntrptViewHelper {

    /**
     * 캐시에 저장된 상태 맵을 기준으로 조회된 {@link JournalIntrptDto} 트리 구조에 상태를 반영한다.
     *
     * @param listDto 조회된 저널 일자 목록 DTO
     * @param intrptMap intrpt id → {@link JournalState} 맵
     */
    public static void applyState(List<JournalIntrptDto> listDto, Map<Integer, JournalState> intrptMap) {
        if (CollectionUtils.isEmpty(listDto)) return;
        for (final JournalIntrptDto intrpt : listDto) {
            final JournalState d = intrptMap.get(intrpt.getId());
            if (d != null) {
                intrpt.state.apply(StateCd.COLLAPSED, d.getCollapsed());
                intrpt.state.apply(StateCd.RESOLVED, d.getResolved());
            }
        }
    }
}
