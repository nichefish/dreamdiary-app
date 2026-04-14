package io.nicheblog.dreamdiary.feature.jrnl.diary.service.helper;

import io.nicheblog.dreamdiary.feature.clsf.state.StateCd;
import io.nicheblog.dreamdiary.feature.jrnl._shared.state.JrnlState;
import io.nicheblog.dreamdiary.feature.jrnl.diary.model.JrnlDiaryDto;
import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
 * JrnlDiaryViewHelper
 *
 * @author nichefish
 */
@UtilityClass
public class JrnlDiaryViewHelper {

    /**
     * 캐시에 저장된 상태 맵(diary)을 기준으로 조회된 {@link JrnlDiaryDto} 트리 구조에 상태를 반영한다.
     *
     * @param listDto 조회된 저널 일자 목록 DTO
     * @param diaryMap diary id → {@link JrnlState} 맵
     */
    public static void applyStates(List<JrnlDiaryDto> listDto, Map<Integer, JrnlState> diaryMap) {
        if (CollectionUtils.isEmpty(listDto)) return;
        for (final JrnlDiaryDto diary : listDto) {
            final JrnlState d = diaryMap.get(diary.getId());
            if (d != null) {
                diary.state.apply(StateCd.COLLAPSED, d.getCollapsed());
                diary.state.apply(StateCd.RESOLVED, d.getResolved());
                diary.state.apply(StateCd.IMPRTC, d.getImprtc());
                diary.state.apply(StateCd.REFRNC, d.getRefrnc());
            }
        }
    }
}
