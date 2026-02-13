package io.nicheblog.dreamdiary.global.intrfc.entity;

/**
 * Sortable
 * <pre>
 *  (공통/상속) idx 인터페이스
 * </pre>
 *
 * @author nichefish
 */
public interface Sortable {

    /**
     * idx 반환
     * @return idx
     */
    Integer getIdx();
    
    /**
     * idx 세팅
     * @param idx idx
     */
    void setIdx(final Integer idx);
}
