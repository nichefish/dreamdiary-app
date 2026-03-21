package io.nicheblog.dreamdiary.feature.board.def.spec;

import io.nicheblog.dreamdiary.feature.board.def.entity.BoardDefEntity;
import io.nicheblog.dreamdiary.global.intrfc.spec.BaseCrudSpec;
import org.springframework.stereotype.Component;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

/**
 * BoardDefSpec
 * <pre>
 *  게시판 정의 정보 목록 검색인자 세팅 Specification.
 *  ※게시판 정의(board_def) = 게시판 분류. 게시판 게시물(board_post)을 1:N으로 관리한다.
 * </pre>
 *
 * @author nichefish
 */
@Component
public class BoardDefSpec
        implements BaseCrudSpec<BoardDefEntity> {

    /**
     * 검색 조건 세팅 후 쿼리 후처리. (override)
     * 
     * @param root 조회할 엔티티의 Root 객체
     * @param query - CriteriaQuery 객체
     * @param builder CriteriaBuilder 객체
     */
    @Override
    public void postQuery(
            final Root<BoardDefEntity> root,
            final CriteriaQuery<?> query,
            final CriteriaBuilder builder
    ) {
        // 정렬 순서 변경
        List<Order> orders = new ArrayList<>();
        orders.add(builder.asc(root.get("idx")));
        query.orderBy(orders);
    }
}
