package io.nicheblog.dreamdiary.feature.user.emplym;

import lombok.AllArgsConstructor;

/**
 * Emplym
 * 사용자 고용 형태 코드와 화면용 한글 설명을 쌍으로 둔다.
 *
 * @author nichefish
 */
@AllArgsConstructor
public enum Emplym {
    EMPLYM("재직"),
    FREE("프리랜서");

    public final String desc;
}
