package io.nicheblog.dreamdiary.global.intrfc.spec;

import io.nicheblog.dreamdiary.domain.notice.entity.NoticeEntity;
import io.nicheblog.dreamdiary.extension.clsf.tag.entity.ContentTagEntity;
import io.nicheblog.dreamdiary.global.intrfc.entity.BaseClsfEntity;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.CollectionUtils;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * BaseClsfSpec
 * <pre>
 *  (공통/상속) 검색인자 세팅 Specification 인터페이스.
 * </pre>
 *
 * @author nichefish
 * @see BasePostSpec
 */
public interface BaseClsfSpec<Entity extends BaseClsfEntity>
        extends BaseCrudSpec<Entity> {

    /**
     * default: 인자별로 구체적인 검색 조건을 설정하여 목록을 반환한다.
     * 
     * @param searchParamMap 검색 조건을 포함하는 매개변수 맵
     * @return {@link Specification} -- 검색 조건에 따른 Specification 객체
     */
    @Override
    default Specification<Entity> searchWith(final Map<String, Object> searchParamMap) {
        // filter
        searchParamMap.remove("backToList");
        searchParamMap.remove("actvtyCtgr");

        return (root, query, builder) -> {
            List<Predicate> basePredicate = new ArrayList<>();
            List<Predicate> clsfPredicate = new ArrayList<>();
            List<Predicate> predicate = new ArrayList<>();
            try {
                // basePredicte 먼저 처리 후 나머지에 대해 처리
                basePredicate = getBasePredicate(searchParamMap, root, builder);
                clsfPredicate = getClsfPredicate(searchParamMap, root, builder);
                predicate = getPredicateWithParams(searchParamMap, root, builder);
            } catch (final Exception e) {
                e.printStackTrace();
            }
            predicate.addAll(basePredicate);
            predicate.addAll(clsfPredicate);
            this.postQuery(root, query, builder, searchParamMap);
            return builder.and(predicate.toArray(new Predicate[0]));
        };
    }

    /**
     * default: CLSF 요소에 대해 인자별로 구체적인 검색 조건을 세팅한다.
     * 
     * @param searchParamMap 검색 조건을 포함하는 매개변수 맵
     * @param root 엔티티의 루트 객체
     * @param builder CriteriaBuilder 객체
     * @return {@link List} -- 생성된 검색 조건의 리스트
     * @throws Exception 검색 조건 생성 중 발생할 수 있는 예외
     */
    default List<Predicate> getClsfPredicate(
            final Map<String, Object> searchParamMap,
            final Root<Entity> root,
            final CriteriaBuilder builder
    ) throws Exception {

        List<Predicate> predicate = new ArrayList<>();
        List<String> keysToRemove = new ArrayList<>();

        for (String key : searchParamMap.keySet()) {
            switch(key) {
                // 태그 모듈
                case "tags":
                    try {
                        List<Integer> refTagNoList = (List<Integer>) searchParamMap.get(key);
                        if (CollectionUtils.isEmpty(refTagNoList)) continue;
                        // 태그 검색
                        Join<NoticeEntity, ContentTagEntity> contentTagJoin = root.join("tag").join("list", JoinType.INNER);
                        Expression<String> contentTagExp = contentTagJoin.get("refTagNo");
                        predicate.add(contentTagExp.in(refTagNoList)); // IN 절 사용
                    } catch (final Exception e) {
                        e.printStackTrace();
                    }
                    keysToRemove.add(key);      // 처리된 키 저장
                    continue;
                // TODO:
            }
        }
        keysToRemove.forEach(searchParamMap::remove);       // 처리된 키를 searchParamMap에서 제거

        return predicate;
    }
}
