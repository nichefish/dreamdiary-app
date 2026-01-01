package io.nicheblog.dreamdiary.global.intrfc.entity;

/**
 * Usable
 * <pre>
 *  (공통/상속) 사용 인터페이스
 * </pre>
 *
 * @author nichefish
 */
public interface Usable {

    /**
     * 사용 여부 반환
     * @return String - "Y"\"N"
     */
    String getUseYn();
    
    /**
     * 사용 여부 세팅
     * @param useYn 사용 여부
     */
    void setUseYn(final String useYn);
}
