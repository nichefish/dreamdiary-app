package io.nicheblog.dreamdiary.domain.jrnl.intrpt.service.helper;

import io.nicheblog.dreamdiary.domain.jrnl.intrpt.model.JrnlIntrptDto;
import io.nicheblog.dreamdiary.domain.jrnl.state.JrnlState;
import io.nicheblog.dreamdiary.domain.clsf.state.StateCd;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
 * JrnlIntrptViewHelper
 *
 * @author nichefish
 */
public class JrnlIntrptViewHelper {

    /**
     * 캐시에 저장된 상태 맵(entry/diary)을 기준으로 조회된 {@link JrnlIntrptDto} 트리 구조에 상태를 반영한다.
     *
     * @param listDto 조회된 저널 일자 목록 DTO
     * @param intrptMap diary postNo → {@link JrnlState} 맵
     */
    public static void applyState(List<JrnlIntrptDto> listDto, Map<Integer, JrnlState> intrptMap) {
        if (CollectionUtils.isEmpty(listDto)) return;
        for (final JrnlIntrptDto intrpt : listDto) {
            final JrnlState d = intrptMap.get(intrpt.getPostNo());
            if (d != null) {
                intrpt.state.apply(StateCd.COLLAPSED, d.getCollapsed());
                intrpt.state.apply(StateCd.RESOLVED, d.getResolved());
            }
        }
    }
}
