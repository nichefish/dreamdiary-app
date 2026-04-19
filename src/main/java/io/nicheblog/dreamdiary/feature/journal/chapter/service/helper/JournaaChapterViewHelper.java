package io.nicheblog.dreamdiary.feature.journal.chapter.service.helper;

import io.nicheblog.dreamdiary.feature.attachable.state.StateCd;
import io.nicheblog.dreamdiary.feature.journal._shared.state.JournalState;
import io.nicheblog.dreamdiary.feature.journal.chapter.model.JournalChapterDto;
import io.nicheblog.dreamdiary.feature.journal.diary.service.helper.JournalDiaryViewHelper;
import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
 * JournaaChapterViewHelper
 *
 * @author nichefish
 */
@UtilityClass
public class JournaaChapterViewHelper {

    /**
     * 캐시에 저장된 상태 맵(entry/diary)을 기준으로 조회된 {@link JournalChapterDto} 트리 구조에 상태를 반영한다.
     *
     * @param listDto 조회된 저널 일자 목록 DTO
     * @param chapterMap entry id → {@link JournalState} 맵
     * @param diaryMap diary id → {@link JournalState} 맵
     */
    public static void applyStates(
        final List<JournalChapterDto> listDto,
        final Map<Integer, JournalState> chapterMap,
        final Map<Integer, JournalState> diaryMap,
        final Map<Integer, JournalState> interpretationMap
    ) {

        if (CollectionUtils.isEmpty(listDto)) return;
        for (final JournalChapterDto entry : listDto) {

            final JournalState s = chapterMap.get(entry.getId());
            if (s != null) {
                entry.state.apply(StateCd.COLLAPSED, s.getCollapsed());
            }

            JournalDiaryViewHelper.applyStates(entry.getJournalDiaryList(), diaryMap, interpretationMap);
        }
    }
}
