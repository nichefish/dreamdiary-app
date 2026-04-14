package io.nicheblog.dreamdiary.feature.jrnl.intrpt.service.helper;

import io.nicheblog.dreamdiary.feature.clsf.state.StateCd;
import io.nicheblog.dreamdiary.feature.jrnl._shared.state.JrnlState;
import io.nicheblog.dreamdiary.feature.jrnl.intrpt.model.JrnlIntrptDto;
import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
 * JrnlIntrptViewHelper
 *
 * @author nichefish
 */
@UtilityClass
public class JrnlIntrptViewHelper {

    /**
     * 캐시에 저장된 상태 맵을 기준으로 조회된 {@link JrnlIntrptDto} 트리 구조에 상태를 반영한다.
     *
     * @param listDto 조회된 저널 일자 목록 DTO
     * @param intrptMap intrpt id → {@link JrnlState} 맵
     */
    public static void applyState(List<JrnlIntrptDto> listDto, Map<Integer, JrnlState> intrptMap) {
        if (CollectionUtils.isEmpty(listDto)) return;
        for (final JrnlIntrptDto intrpt : listDto) {
            final JrnlState d = intrptMap.get(intrpt.getId());
            if (d != null) {
                intrpt.state.apply(StateCd.COLLAPSED, d.getCollapsed());
                intrpt.state.apply(StateCd.RESOLVED, d.getResolved());
            }
        }
    }
}
