package io.nicheblog.dreamdiary.feature.journal.annual.type;

import io.nicheblog.dreamdiary.global.type.LocalizedEnum;
import lombok.AllArgsConstructor;

/**
 * JournalAnnualTy
 * 결산 상세에서 다루는 콘텐츠 축(꿈·일기) 구분 enum.
 *
 * @author nichefish
 */
@AllArgsConstructor
public enum JournalAnnualTy implements LocalizedEnum {
    DREAM("꿈"),
    DIARY("일기");

    public final String desc;
}
