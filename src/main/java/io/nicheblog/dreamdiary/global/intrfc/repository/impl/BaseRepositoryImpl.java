package io.nicheblog.dreamdiary.global.intrfc.repository.impl;

import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.QueryHint;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Root;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * BaseRepositoryImpl
 * <pre>
 *  (공통/상속) entityManager.refresh 사용 위한 커스텀 Repository 구현체
 * </pre>
 *
 * @author nichefish
 */
@NoRepositoryBean
public class BaseRepositoryImpl<T, ID extends Serializable>
        extends SimpleJpaRepository<T, ID>
        implements BaseStreamRepository<T, ID> {

    @PersistenceContext
    private final EntityManager entityManager;

    /**
     * constructor
     */
    public BaseRepositoryImpl(final JpaEntityInformation entityInformation, final EntityManager entityManager) {
        super(entityInformation, entityManager);
        this.entityManager = entityManager;
    }

    /**
     * entity refresh
     * 자식 엔티티에 대해서는 적용되지 않는다.
     */
    @Override
    @Transactional
    public void refresh(final T t) {
        entityManager.refresh(t);
    }

    /**
     * entity merge
     */
    @Override
    @Transactional
    public void merge(T t) {
        entityManager.merge(t);
    }

    /** Stream 조회 (readonly) */
    @Override
    @Transactional(readOnly = true)
    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    public Stream<T> streamAllBy(Specification<T> searchWith) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = cb.createQuery(getDomainClass());
        Root<T> root = query.from(getDomainClass());
        query.where(searchWith.toPredicate(root, query, cb));
        return entityManager.createQuery(query).getResultStream();
    }

    /** Stream 조회 (readonly) */
    @Override
    @Transactional(readOnly = true)
    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    public Stream<T> streamAllBy(Specification<T> searchWith, Sort sort) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = cb.createQuery(getDomainClass());
        Root<T> root = query.from(getDomainClass());
        query.where(searchWith.toPredicate(root, query, cb));

        // Sort 객체를 이용한 정렬 조건 적용
        if (sort != null) {
            List<Order> orders = new ArrayList<>();
            for (Sort.Order sortOrder : sort) {
                // 정렬 방향에 따라 Order 객체 생성
                javax.persistence.criteria.Order order;
                if (sortOrder.isAscending()) {
                    order = cb.asc(root.get(sortOrder.getProperty()));
                } else {
                    order = cb.desc(root.get(sortOrder.getProperty()));
                }
                orders.add(order);
            }
            query.orderBy(orders);
        }
        return entityManager.createQuery(query).getResultStream();
    }


    /** (전파) 모니터링 대시보드 프로시저 돌리기 ( 등록,삭제:1번 / 수정:2번 ) */
    // @Transactional(readOnly = true)
    // @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    // @Query(value=" CALL INFR_IOC_STATS_ATTACK_TYPE(DATE_FORMAT(:today, '%Y-%m-%d'));" +
    //         " CALL INFR_IOC_STATS_IOC_CNT(DATE_FORMAT(:today, '%Y-%m-%d'));" +
    //         " CALL INFR_IOC_STATS_IOC_TY(DATE_FORMAT(:today, '%Y-%m-%d'));" +
    //         " CALL INFR_IOC_STATS_NTN(DATE_FORMAT(:today, '%Y-%m-%d'));"
    //         , nativeQuery = true)
    // Integer rollDashProcedures(final @Param("today") String today);
}