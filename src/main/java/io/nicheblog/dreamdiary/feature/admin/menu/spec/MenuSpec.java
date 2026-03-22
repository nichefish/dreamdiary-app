package io.nicheblog.dreamdiary.feature.admin.menu.spec;

import io.nicheblog.dreamdiary.auth.intrfc.spec.BaseAuditSpec;
import io.nicheblog.dreamdiary.feature.admin.menu.entity.MenuEntity;
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
        implements BaseAuditSpec<MenuEntity> {

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
