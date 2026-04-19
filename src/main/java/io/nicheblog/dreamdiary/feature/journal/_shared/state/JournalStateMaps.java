package io.nicheblog.dreamdiary.feature.journal._shared.state;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

/**
 * JournalStateMaps
 *
 * @author nichefish
 */
@Getter
@Builder
@AllArgsConstructor
public class JournalStateMaps {
    /** 항목 상태 맵 */
    private Map<Integer, JournalState> chapterMap;
    /** 일기 상태 맵 */
    private Map<Integer, JournalState> diaryMap;
    private Map<Integer, JournalState> noteMap;
    /** 꿈 상태 맵 */
    private Map<Integer, JournalState> dreamMap;
    /** 해석 상태 맵 */
    private Map<Integer, JournalState> interpretationMap;
}
