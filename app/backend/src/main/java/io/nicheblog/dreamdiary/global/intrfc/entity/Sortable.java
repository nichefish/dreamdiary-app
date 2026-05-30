package io.nicheblog.dreamdiary.global.intrfc.entity;

/**
 * Sortable
 * <pre>
 *  (공통/상속) sortOrder 인터페이스
 * </pre>
 *
 * @author nichefish
 */
public interface Sortable {

    /**
     * sortOrder 반환
     * @return sortOrder
     */
    Integer getSortOrder();
    
    /**
     * sortOrder 세팅
     * @param sortOrder sortOrder
     */
    void setSortOrder(final Integer sortOrder);
}
