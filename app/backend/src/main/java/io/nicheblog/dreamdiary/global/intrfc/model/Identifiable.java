package io.nicheblog.dreamdiary.global.intrfc.model;

/**
 * Identifiable
 * <pre>
 *  (공통/상속) 식별키 조회 인터페이스
 * </pre>
 *
 * @author nichefish
 */
public interface Identifiable<KeyType> {

    /**
     * 키 반환 메소드 (각 dto에서 구현)
     * @return KeyType - 해당 Dto의 고유 키를 나타내는 KeyType 객체
     */
    KeyType getKey();
}
