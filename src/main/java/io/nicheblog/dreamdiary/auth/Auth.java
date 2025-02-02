package io.nicheblog.dreamdiary.auth;

import lombok.RequiredArgsConstructor;

/**
 * 권한(role) Enum
 *
 * @author nichefish
 */
@RequiredArgsConstructor
public enum Auth {

    USER("USER", "ROLE_USER", "사용자"),
    MNGR("MNGR", "ROLE_MNGR", "관리자"),
    DEV("DEV", "ROLE_DEV", "개발자");

    public final String key;
    public final String role;
    public final String desc;
}