package io.nicheblog.dreamdiary.global.intrfc.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;
import java.util.stream.Stream;

/**
 * BaseStreamRepository
 * <pre>
 *  Stream 조회 처리용 Custom (JPA) Repository 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@NoRepositoryBean
public interface BaseStreamRepository<T, ID extends Serializable>
        extends BaseRepository<T, ID>, JpaRepository<T, ID>, JpaSpecificationExecutor<T> {

    /**
     * 검색 조건에 맞는 데이터를 스트림으로 조회
     *
     * @param searchWith 검색 조건에 맞는 Specification 객체
     * @return 검색된 결과를 스트림으로 반환
     */
    Stream<T> streamAllBy(Specification<T> searchWith);

    /**
     * 검색 조건에 맞는 데이터를 스트림으로 조회 (+정렬)
     *
     * @param searchWith 검색 조건에 맞는 Specification 객체
     * @param sort 정렬 기준
     * @return 검색된 결과를 정렬된 스트림으로 반환
     */
    Stream<T> streamAllBy(Specification<T> searchWith, Sort sort);
}
