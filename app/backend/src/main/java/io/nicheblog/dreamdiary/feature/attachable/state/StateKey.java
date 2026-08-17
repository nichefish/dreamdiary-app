package io.nicheblog.dreamdiary.feature.attachable.state;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * StateKey
 * <pre>
 *  attachable 글에 부여되는 상태의 저장/전송용 키 (DB {@code state.state_key}, API JSON {@code stateKey}).
 * </pre>
 *
 * @author nichefish
 */
@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public enum StateKey {

    COLLAPSED("COLLAPSED", "접기"),
    IMPRTC("IMPRTC", "중요"),
    REFRNC("REFRNC", "참조"),
    /** 저널 꿈 전용 의미 상태: 악몽 */
    NHTMR("NHTMR", "악몽"),
    /** 저널 꿈 전용 의미 상태: 입면 환각 */
    HALLUC("HALLUC", "입면 환각");

    /** DB 및 API에 사용되는 문자열 키 */
    public final String key;
    public final String desc;
    public String icon;

    /**
     * 키 문자열과 일치하는 enum 반환
     * @param key 상태 키 문자열
     * @return 일치 시 enum, 없으면 null
     */
    public static StateKey getByKey(final String key) {
        if (key == null) return null;
        for (final StateKey sk : StateKey.values()) {
            if (sk.key.equals(key)) return sk;
        }
        return null;
    }
}
