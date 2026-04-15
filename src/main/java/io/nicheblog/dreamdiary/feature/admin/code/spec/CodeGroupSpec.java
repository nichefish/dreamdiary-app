package io.nicheblog.dreamdiary.feature.admin.code.spec;

import io.nicheblog.dreamdiary.global.intrfc.spec.BaseSpec;
import io.nicheblog.dreamdiary.infrastructure.code.entity.CodeGroupEntity;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * CodeGroupSpec
 */
@Component
@Log4j2
public class CodeGroupSpec
        implements BaseSpec<CodeGroupEntity> {

    public List<Predicate> getPredicateWithParams(
            final Map<String, Object> searchParamMap,
            final Root<CodeGroupEntity> root,
            final CriteriaBuilder builder
    ) throws Exception {
        final List<Predicate> predicate = new ArrayList<>();

        for (final String key : searchParamMap.keySet()) {
            final Object value = searchParamMap.get(key);
            switch (key) {
                case "clCd":
                case "clCdNm":
                case "dc":
                    final Expression<String> keyExp = root.get(key);
                    predicate.add(builder.like(keyExp, "%" + value + "%"));
                    continue;
                default:
                    try {
                        predicate.add(builder.equal(root.get(key), value));
                    } catch (final Exception e) {
                        log.info("unable to locate attribute '{}' while trying root.get(key).", key);
                    }
            }
        }
        return predicate;
    }
}
