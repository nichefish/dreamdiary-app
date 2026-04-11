package io.nicheblog.dreamdiary.feature.jrnl.sumry.type;

import lombok.AllArgsConstructor;

/**
 * JrnlSumryTy
 *
 * @author nichefish
 */
@AllArgsConstructor
public enum JrnlSumryTy {
    DREAM("꿈"),
    DIARY("일기");

    public final String desc;
}
