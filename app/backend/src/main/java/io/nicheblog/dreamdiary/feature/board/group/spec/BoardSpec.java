package io.nicheblog.dreamdiary.feature.board.group.spec;

import io.nicheblog.dreamdiary.auth.intrfc.spec.BaseAuditSpec;
import io.nicheblog.dreamdiary.feature.board.group.entity.BoardEntity;
import org.springframework.stereotype.Component;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

@Component
public class BoardSpec extends BaseAuditSpec<BoardEntity> {

    @Override
    public void postQuery(
            final Root<BoardEntity> root,
            final CriteriaQuery<?> query,
            final CriteriaBuilder builder
    ) {
        final List<Order> orders = new ArrayList<>();
        orders.add(builder.asc(root.get("sortOrder")));
        query.orderBy(orders);
    }
}
