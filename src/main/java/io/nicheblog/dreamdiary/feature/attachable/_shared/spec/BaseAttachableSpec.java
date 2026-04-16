package io.nicheblog.dreamdiary.feature.attachable._shared.spec;

import io.nicheblog.dreamdiary.auth.intrfc.spec.BaseAuditSpec;
import io.nicheblog.dreamdiary.feature.board.notice.entity.NoticeEntity;
import io.nicheblog.dreamdiary.feature.attachable._shared.entity.BaseAttachableEntity;
import io.nicheblog.dreamdiary.feature.attachable.tag.entity.TagContentEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * BaseAttachableSpec
 * <pre>
 *  (공통/상속) 검색인자 세팅 Specification 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
public interface BaseAttachableSpec<Entity extends BaseAttachableEntity>
        extends BaseAuditSpec<Entity> {

    /**
     * default: 인자별로 구체적인 검색 조건을 설정하여 목록을 반환한다.
     * 
     * @param searchParamMap 검색 조건을 포함하는 매개변수 맵
     * @return {@link Specification} -- 검색 조건에 따른 Specification 객체
     */
    @Override
    default Specification<Entity> searchWith(final Map<String, Object> searchParamMap) {
        searchParamMap.remove("backToList");
        searchParamMap.remove("actvtyCtgr");

        return (root, query, builder) -> {
            List<Predicate> basePredicate = new ArrayList<>();
            List<Predicate> attachablePredicate = new ArrayList<>();
            List<Predicate> predicate = new ArrayList<>();
            try {
                // basePredicte 먼저 처리 후 나머지에 대해 처리
                final Map<String, Object> baseSearchParamMap = new HashMap<>(searchParamMap);
                final Map<String, Object> attachableSearchParamMap = new HashMap<>(searchParamMap);
                final Map<String, Object> customSearchParamMap = new HashMap<>(searchParamMap);
                basePredicate = getBasePredicate(baseSearchParamMap, root, query, builder);
                attachablePredicate = getAttachablePredicate(attachableSearchParamMap, root, query, builder);
                predicate = getPredicateWithParams(customSearchParamMap, root, query, builder);
            } catch (final Exception e) {
                e.printStackTrace();
            }
            predicate.addAll(basePredicate);
            predicate.addAll(attachablePredicate);
            this.postQuery(root, query, builder, searchParamMap);
            return builder.and(predicate.toArray(new Predicate[0]));
        };
    }

    /**
     * default: attachable 요소에 대해 인자별로 구체적인 검색 조건을 세팅한다.
     * 
     * @param searchParamMap 검색 조건을 포함하는 매개변수 맵
     * @param root 엔티티의 루트 객체
     * @param query - CriteriaQuery 객체
     * @param builder CriteriaBuilder 객체
     * @return {@link List} -- 생성된 검색 조건의 리스트
     */
    default List<Predicate> getAttachablePredicate(
            final Map<String, Object> searchParamMap,
            final Root<Entity> root,
            final CriteriaQuery<?> query,
            final CriteriaBuilder builder
    ) throws Exception {

        final List<Predicate> predicate = new ArrayList<>();
        final List<String> keysToRemove = new ArrayList<>();

        for (final String key : searchParamMap.keySet()) {
            switch (key) {
                case "tags":
                    try {
                        final List<Integer> tagIdList = (List<Integer>) searchParamMap.get(key);
                        if (CollectionUtils.isEmpty(tagIdList)) continue;

                        final Join<NoticeEntity, TagContentEntity> tagContentJoin = root.join("tag").join("list", JoinType.INNER);
                        final Expression<Integer> tagContentExp = tagContentJoin.get("tagId");
                        predicate.add(tagContentExp.in(tagIdList));
                    } catch (final Exception e) {
                        e.printStackTrace();
                    }
                    keysToRemove.add(key);
                    continue;
                default:
                    break;
            }
        }
        keysToRemove.forEach(searchParamMap::remove);

        return predicate;
    }
}
