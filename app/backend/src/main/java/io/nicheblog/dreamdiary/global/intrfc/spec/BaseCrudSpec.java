package io.nicheblog.dreamdiary.global.intrfc.spec;

import io.nicheblog.dreamdiary.global.intrfc.entity.BaseCrudEntity;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * BaseCrudSpec
 * <pre>
 *  (공통/상속) AUDIT 요소에 대한 검색인자 세팅 Specification 추상 클래스.
 * </pre>
 *
 * @author nichefish
 */
public abstract class BaseCrudSpec<Entity extends BaseCrudEntity>
        extends BaseSpec<Entity> {

    /**
     * default: 인자별로 구체적인 검색 조건을 설정하여 목록을 반환한다.
     *
     * @param searchParamMap 검색 파라미터 맵
     * @return {@link Specification} -- 검색 조건에 맞는 Specification 객체
     */
    @Override
    public Specification<Entity> searchWith(final Map<String, Object> searchParamMap) {
        // filter
        searchParamMap.remove("backToList");
        searchParamMap.remove("actvtyCtgr");

        return (root, query, builder) -> {
            List<Predicate> predicate = new ArrayList<>();
            try {
                // basePredicte 먼저 처리 후 나머지에 대해 처리
                predicate = getPredicateWithParams(searchParamMap, root, query, builder);
            } catch (final Exception e) {
                log.warn("Failed to build search predicate.", e);
            }
            this.postQuery(root, query, builder, searchParamMap);
            return builder.and(predicate.toArray(new Predicate[0]));
        };
    }
}
