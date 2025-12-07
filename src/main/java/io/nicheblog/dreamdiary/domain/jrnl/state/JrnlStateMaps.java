package io.nicheblog.dreamdiary.domain.jrnl.state;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

/**
 * JrnlStateMaps
 *
 * @author nichefish
 */
@Getter
@AllArgsConstructor
public class JrnlStateMaps {
    /** 일기 상태 맵 */
    private Map<Integer, JrnlState> diaryMap;
    /** 꿈 상태 맵 */
    private Map<Integer, JrnlState> dreamMap;
    /** 해석 상태 맵 */
    private Map<Integer, JrnlState> intrptMap;
}
