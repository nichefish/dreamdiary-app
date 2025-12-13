package io.nicheblog.dreamdiary.global.intrfc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;

/**
 * BaseRepository
 * <pre>
 *  (공통/상속) entityManager.refresh 사용 위한 커스텀 (JPA) Repository 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@NoRepositoryBean
public interface BaseRepository<T, ID extends Serializable>
        extends JpaRepository<T, ID>, JpaSpecificationExecutor<T> {

    /**
     * entity refresh
     * @param t - 새로 고칠 엔티티 객체
     */
    void refresh(final T t);

    /**
     * entity merge
     * 주어진 엔티티의 상태를 현재 컨텍스트와 병합합니다.
     * @param t - 병합할 엔티티 객체
     */
    void merge(final T t);
}
