package io.nicheblog.dreamdiary.feature.jrnl.dream.service.helper;

import io.nicheblog.dreamdiary.feature.jrnl.dream.model.JrnlDreamDto;
import io.nicheblog.dreamdiary.feature.jrnl.intrpt.service.helper.JrnlIntrptViewHelper;
import io.nicheblog.dreamdiary.feature.jrnl.state.JrnlState;
import io.nicheblog.dreamdiary.feature.clsf.state.StateCd;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
 * JrnlDreamViewHelper
 *
 * @author nichefish
 */
public class JrnlDreamViewHelper {

    /**
     * 캐시에 저장된 상태 맵(entry/diary)을 기준으로 조회된 {@link JrnlDreamDto} 트리 구조에 상태를 반영한다.
     *
     * @param listDto 조회된 저널 일자 목록 DTO
     * @param dreamMap entry postNo → {@link JrnlState} 맵
     * @param intrptMap diary postNo → {@link JrnlState} 맵
     */
    public static void applyStates(List<JrnlDreamDto> listDto, Map<Integer, JrnlState> dreamMap, Map<Integer, JrnlState> intrptMap) {
        if (CollectionUtils.isEmpty(listDto)) return;

        for (final JrnlDreamDto dream : listDto) {

            final JrnlState s = dreamMap.get(dream.getPostNo());
            if (s != null) {
                dream.state.apply(StateCd.COLLAPSED, s.getCollapsed());
                dream.state.apply(StateCd.RESOLVED, s.getResolved());
                dream.state.apply(StateCd.IMPRTC, s.getImprtc());
                dream.state.apply(StateCd.REFRNC, s.getRefrnc());
            }

            JrnlIntrptViewHelper.applyState(dream.getJrnlIntrptList(), intrptMap);
        }
    }
}
