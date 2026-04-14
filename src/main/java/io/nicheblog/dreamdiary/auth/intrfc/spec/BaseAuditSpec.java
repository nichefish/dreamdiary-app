package io.nicheblog.dreamdiary.auth.intrfc.spec;

import io.nicheblog.dreamdiary.auth.security.entity.AuditorInfo;
import io.nicheblog.dreamdiary.global.intrfc.entity.BaseCrudEntity;
import io.nicheblog.dreamdiary.global.intrfc.spec.BaseCrudSpec;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * BaseCrudSpec
 * <pre>
 *  (공통/상속) AUDIT 요소에 대한 검색인자 세팅 Specification 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
public interface BaseAuditSpec<Entity extends BaseCrudEntity>
        extends BaseCrudSpec<Entity> {

    /**
     * default: 인자별로 구체적인 검색 조건을 세팅한다.
     *
     * @param searchParamMap 검색 파라미터 맵
     * @param root 검색할 엔티티의 Root 객체
     * @param query - CriteriaQuery 객체
     * @param builder 검색 조건을 생성하는 CriteriaBuilder 객체
     * @return {@link List} -- 설정된 검색 조건(Predicate) 리스트
     */
    default List<Predicate> getBasePredicate(
            final Map<String, Object> searchParamMap,
            final Root<Entity> root,
            final CriteriaQuery<?> query,
            final CriteriaBuilder builder
    ) {

        final List<Predicate> predicate = new ArrayList<>();
        final List<String> keysToRemove = new ArrayList<>();

        for (final String key : searchParamMap.keySet()) {
            switch(key) {
                case "createdBy":
                    predicate.add(builder.equal(root.get(key), searchParamMap.get(key)));
                    keysToRemove.add(key);      // 처리된 키 저장
                    continue;
                case "nickNm":
                    // 작성자 이름 = 조인 후 LIKE 검색
                    Join<Entity, AuditorInfo> createdByJoin = root.join("createdByInfo", JoinType.LEFT);
                    Expression<String> nickNmExp = createdByJoin.get("nickNm");
                    predicate.add(builder.like(nickNmExp, "%" + searchParamMap.get(key) + "%"));
                    keysToRemove.add(key);      // 처리된 키 저장
            }
        }
        keysToRemove.forEach(searchParamMap::remove);       // 처리된 키를 searchParamMap에서 제거

        return predicate;
    }
}
