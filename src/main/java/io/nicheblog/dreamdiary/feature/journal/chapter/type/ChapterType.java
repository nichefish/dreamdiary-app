package io.nicheblog.dreamdiary.feature.journal.chapter.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * ChapterType
 * <pre>
 *  저널 챕터 타입 (DIARY | DREAM)
 * </pre>
 *
 * @author nichefish
 */
@Getter
@RequiredArgsConstructor
public enum ChapterType {

    DIARY("DIARY", "일기"),
    DREAM("DREAM", "꿈"),
    NOTE("NOTE", "노트");

    public final String key;
    public final String desc;
}
