package io.nicheblog.dreamdiary.domain.admin.menu.spec;

import io.nicheblog.dreamdiary.domain.admin.menu.entity.MenuEntity;
import io.nicheblog.dreamdiary.global.intrfc.spec.BaseCrudSpec;
import org.springframework.stereotype.Component;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

/**
 * MenuSpec
 * <pre>
 *  메뉴 검색인자 세팅 Specification.
 * </pre>
 *
 * @author nichefish
 */
@Component
public class MenuSpec
        implements BaseCrudSpec<MenuEntity> {

    /**
     * 검색 조건 세팅 후 쿼리 후처리. (override)
     *
     * @param root 조회할 엔티티의 Root 객체
     * @param query - CriteriaQuery 객체
     * @param builder CriteriaBuilder 객체
     */
    @Override
    public void postQuery(
            final Root<MenuEntity> root,
            final CriteriaQuery<?> query,
            final CriteriaBuilder builder
    ) {
        // distinct
        List<Order> orders = new ArrayList<>();
        orders.add(builder.asc(root.get("idx")));
        query.orderBy(orders);
    }
}
