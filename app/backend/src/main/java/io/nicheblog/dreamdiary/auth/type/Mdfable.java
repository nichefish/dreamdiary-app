package io.nicheblog.dreamdiary.auth.type;

import io.nicheblog.dreamdiary.global.type.LocalizedEnum;
import lombok.RequiredArgsConstructor;

/**
 * 수정 권한 Enum
 *
 * @author nichefish
 */
@RequiredArgsConstructor
public enum Mdfable implements LocalizedEnum {

    REGSTR("등록자"),
    MNGR("관리자"),
    USER("사용자"),
    ALL("전체");

    public final String desc;
}
