package io.nicheblog.dreamdiary.feature.admin.tmplat.spec;

import io.nicheblog.dreamdiary.auth.intrfc.spec.BaseAuditSpec;
import io.nicheblog.dreamdiary.feature.admin.tmplat.entity.TmplatEntity;
import org.springframework.stereotype.Component;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * TmplatSpec
 * <pre>
 *  템플릿 정보 검색인자 세팅 Specification.
 * </pre>
 *
 * @author nichefish
 */
@Component
public class TmplatSpec
        extends BaseAuditSpec<TmplatEntity> {

    /**
     * 인자별로 구체적인 검색 조건을 세팅한다. (override)
     *
     * @param searchParamMap 검색 파라미터 맵
     * @param root 검색할 엔티티의 Root 객체
     * @param query - CriteriaQuery 객체
     * @param builder 검색 조건을 생성하는 CriteriaBuilder 객체
     * @return {@link List} -- 설정된 검색 조건(Predicate) 리스트
     */
    @Override
    public List<Predicate> getPredicateWithParams(
            final Map<String, Object> searchParamMap,
            final Root<TmplatEntity> root,
            final CriteriaQuery<?> query,
            final CriteriaBuilder builder
    ) throws Exception {

        final List<Predicate> predicate = new ArrayList<>();

        // 파라미터 비교 :: 조건 파라미터에 대해 equal 검색
        for (final String key : searchParamMap.keySet()) {
            final Object value = searchParamMap.get(key);
            try {
                predicate.add(builder.equal(root.get(key), value));
            } catch (final Exception e) {
                log.info("unable to locate attribute '{}' while trying root.get(key).", key);
            }
        }
        return predicate;
    }
}