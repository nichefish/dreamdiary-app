package io.nicheblog.dreamdiary.feature.user.emplym;

import lombok.AllArgsConstructor;

/**
 * Rank
 * 사용자 직급 코드와 화면·검증용 한글 설명을 쌍으로 둔다.
 *
 * @author nichefish
 */
@AllArgsConstructor
public enum Rank {
    INTN("인턴"),
    STAFF("사원"),
    DAERI("대리"),
    GWJANG("과장"),
    CHJANG("차장"),
    BJANG("부장"),
    SLJANG("실장"),
    DRCTR("이사"),
    PRSDNT("사장");

    public final String desc;
}
