package io.nicheblog.dreamdiary.global.intrfc.spec;

import io.nicheblog.dreamdiary.global.intrfc.entity.BasePostEntity;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * BasePostSpec
 * <pre>
 *  (공통/상속) 게시판 검색인자 세팅 Specification 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
public interface BasePostSpec<Entity extends BasePostEntity>
        extends BaseClsfSpec<Entity> {

    /**
     * default: 인자별로 구체적인 검색 조건을 설정하여 목록을 반환한다.
     */
    @Override
    default Specification<Entity> searchWith(final Map<String, Object> searchParamMap) {
        // filter
        searchParamMap.remove("backToList");
        searchParamMap.remove("actvtyCtgr");

        return (root, query, builder) -> {
            List<Predicate> basePredicate = new ArrayList<>();
            List<Predicate> clsfPredicate = new ArrayList<>();
            List<Predicate> postPredicate = new ArrayList<>();
            List<Predicate> predicate = new ArrayList<>();
            try {
                // basePredicte 먼저 처리 후 나머지에 대해 처리
                basePredicate = getBasePredicate(searchParamMap, root, builder);
                clsfPredicate = getClsfPredicate(searchParamMap, root, builder);
                postPredicate = getPostPredicate(searchParamMap, root, builder);
                predicate = getPredicateWithParams(searchParamMap, root, builder);
            } catch (final Exception e) {
                e.printStackTrace();
            }
            predicate.addAll(basePredicate);
            predicate.addAll(clsfPredicate);
            predicate.addAll(postPredicate);
            this.postQuery(root, query, builder, searchParamMap);
            return builder.and(predicate.toArray(new Predicate[0]));
        };
    }

    /**
     * default: CLSF 요소에 대해 인자별로 구체적인 검색 조건을 세팅한다.
     *
     * @param searchParamMap 검색 파라미터 맵
     * @param root 검색할 엔티티의 Root 객체
     * @param builder 검색 조건을 생성하는 CriteriaBuilder 객체
     * @return {@link List} -- 설정된 검색 조건(Predicate) 리스트
     * @throws Exception 검색 조건 생성 중 발생할 수 있는 예외
     */
    default List<Predicate> getPostPredicate(
            final Map<String, Object> searchParamMap,
            final Root<Entity> root,
            final CriteriaBuilder builder
    ) throws Exception {

        List<Predicate> predicate = new ArrayList<>();
        List<String> keysToRemove = new ArrayList<>();

        // expressions
        Expression<Date> regDtExp = root.get("regDt");

        for (String key : searchParamMap.keySet()) {
            switch(key) {
                case "title":
                    // 제목 = LIKE 검색
                    Expression<String> keyExp = root.get(key);
                    predicate.add(builder.like(keyExp, "%" + searchParamMap.get(key) + "%"));
                    keysToRemove.add(key);      // 처리된 키 저장
                    continue;
            }
        }
        keysToRemove.forEach(searchParamMap::remove);       // 처리된 키를 searchParamMap에서 제거

        return predicate;
    }
}
