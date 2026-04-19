package io.nicheblog.dreamdiary.feature.journal.annual.type;

import lombok.AllArgsConstructor;

/**
 * JournalAnnualTy
 *
 * @author nichefish
 */
@AllArgsConstructor
public enum JournalAnnualTy {
    DREAM("꿈"),
    DIARY("일기");

    public final String desc;
}
