package io.nicheblog.dreamdiary.feature.journal.sumry.type;

import lombok.AllArgsConstructor;

/**
 * JournalSumryTy
 *
 * @author nichefish
 */
@AllArgsConstructor
public enum JournalSumryTy {
    DREAM("꿈"),
    DIARY("일기");

    public final String desc;
}
