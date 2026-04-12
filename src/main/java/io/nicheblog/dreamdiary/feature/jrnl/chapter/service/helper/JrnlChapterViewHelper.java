package io.nicheblog.dreamdiary.feature.jrnl.chapter.service.helper;

import io.nicheblog.dreamdiary.feature.clsf.state.StateCd;
import io.nicheblog.dreamdiary.feature.jrnl._shared.state.JrnlState;
import io.nicheblog.dreamdiary.feature.jrnl.diary.service.helper.JrnlDiaryViewHelper;
import io.nicheblog.dreamdiary.feature.jrnl.chapter.model.JrnlChapterDto;
import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
 * JrnlChapterViewHelper
 *
 * @author nichefish
 */
@UtilityClass
public class JrnlChapterViewHelper {

    /**
     * 캐시에 저장된 상태 맵(entry/diary)을 기준으로 조회된 {@link JrnlChapterDto} 트리 구조에 상태를 반영한다.
     *
     * @param listDto 조회된 저널 일자 목록 DTO
     * @param chapterMap entry postNo → {@link JrnlState} 맵
     * @param diaryMap diary postNo → {@link JrnlState} 맵
     */
    public static void applyStates(
        final List<JrnlChapterDto> listDto,
        final Map<Integer, JrnlState> chapterMap,
        final Map<Integer, JrnlState> diaryMap
    ) {

        if (CollectionUtils.isEmpty(listDto)) return;
        for (final JrnlChapterDto entry : listDto) {

            final JrnlState s = chapterMap.get(entry.getPostNo());
            if (s != null) {
                entry.state.apply(StateCd.COLLAPSED, s.getCollapsed());
            }

            JrnlDiaryViewHelper.applyStates(entry.getJrnlDiaryList(), diaryMap);
        }
    }
}
