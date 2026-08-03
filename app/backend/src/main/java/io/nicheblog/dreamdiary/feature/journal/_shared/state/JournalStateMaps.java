package io.nicheblog.dreamdiary.feature.journal._shared.state;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

/**
 * JournalStateMaps
 * 챕터·일기·꿈별 상태 캐시 맵을 한 묶음으로 전달하기 위한 DTO.
 *
 * <p>Reflection 은 별도 Aggregate(journal_reflection)이므로 이 묶음에 포함하지 않는다.
 * Reflection state 캐시 맵은 {@code JournalDayService} 가 대상 역참조 로드로 별도 구성한다.</p>
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
    /** 꿈 상태 맵 */
    private Map<Integer, JournalState> dreamMap;
}
