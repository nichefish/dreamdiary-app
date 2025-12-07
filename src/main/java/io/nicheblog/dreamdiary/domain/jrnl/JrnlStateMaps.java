package io.nicheblog.dreamdiary.domain.jrnl;

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
    private Map<Integer, JrnlState> diaryMap;
    private Map<Integer, JrnlState> dreamMap;
    private Map<Integer, JrnlState> intrptMap;
}
