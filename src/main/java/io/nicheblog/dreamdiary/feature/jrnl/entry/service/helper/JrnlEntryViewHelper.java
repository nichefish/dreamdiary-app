package io.nicheblog.dreamdiary.feature.jrnl.entry.service.helper;

import io.nicheblog.dreamdiary.feature.clsf.state.StateCd;
import io.nicheblog.dreamdiary.feature.jrnl._shared.state.JrnlState;
import io.nicheblog.dreamdiary.feature.jrnl.diary.service.helper.JrnlDiaryViewHelper;
import io.nicheblog.dreamdiary.feature.jrnl.entry.model.JrnlEntryDto;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
 * JrnlEntryViewHelper
 *
 * @author nichefish
 */
public class JrnlEntryViewHelper {

    /**
     * 캐시에 저장된 상태 맵(entry/diary)을 기준으로 조회된 {@link JrnlEntryDto} 트리 구조에 상태를 반영한다.
     *
     * @param listDto 조회된 저널 일자 목록 DTO
     * @param entryMap entry postNo → {@link JrnlState} 맵
     * @param diaryMap diary postNo → {@link JrnlState} 맵
     */
    public static void applyStates(
        final List<JrnlEntryDto> listDto,
        final Map<Integer, JrnlState> entryMap,
        final Map<Integer, JrnlState> diaryMap
    ) {

        if (CollectionUtils.isEmpty(listDto)) return;
        for (final JrnlEntryDto entry : listDto) {

            final JrnlState s = entryMap.get(entry.getPostNo());
            if (s != null) {
                entry.state.apply(StateCd.COLLAPSED, s.getCollapsed());
            }

            JrnlDiaryViewHelper.applyStates(entry.getJrnlDiaryList(), diaryMap);
        }
    }
}
