package io.nicheblog.dreamdiary.feature.user.info.spec;

import io.nicheblog.dreamdiary.feature.user.profile.entity.UserProfileEntity;
import io.nicheblog.dreamdiary.global.intrfc.spec.BaseSpec;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import io.nicheblog.dreamdiary.infrastructure.code.entity.CodeItemEntity;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Component
@Log4j2
public class UserProfileSpec implements BaseSpec<UserProfileEntity> {

    public Specification<UserProfileEntity> searchWith(final String searchMode, final String yyStr) {
        return (root, query, builder) -> {
            List<Predicate> predicate = new ArrayList<>();
            try {
                predicate = getCrdtUser(searchMode, yyStr, root, builder);
                final List<Order> order = getOrderByTitleAndEcnyDt(root, builder);
                query.orderBy(order);
            } catch (final Exception e) {
                e.printStackTrace();
            }
            return builder.and(predicate.toArray(new Predicate[0]));
        };
    }

    public List<Predicate> getPredicateWithParams(
            final Map<String, Object> searchParamMap,
            final Root<UserProfileEntity> root,
            final CriteriaQuery<?> query,
            final CriteriaBuilder builder
    ) {
        final List<Predicate> predicate = new ArrayList<>();
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

    public List<Predicate> getCrdtUser(
            final String searchMode,
            final String yyStr,
            final Root<UserProfileEntity> root,
            final CriteriaBuilder builder
    ) throws Exception {
        final List<Predicate> predicate = new ArrayList<>();
        if (StringUtils.isEmpty(searchMode)) return predicate;
        if ("crdtUser".equals(searchMode)) {
            final Expression<Date> retireDtExp = root.get("retireDt");
            final Date firstDay = DateUtils.Parser.bfDateParse(DateUtils.asDate(yyStr + "0101"));
            final Predicate notRetired = builder.isNull(retireDtExp);
            final Predicate retiredAfterFirstDay = builder.greaterThanOrEqualTo(retireDtExp, firstDay);
            predicate.add(builder.or(notRetired, retiredAfterFirstDay));
        }
        return predicate;
    }

    private static List<Order> getOrderByTitleAndEcnyDt(
            final Root<UserProfileEntity> root,
            final CriteriaBuilder builder
    ) {
        final List<Order> order = new ArrayList<>();
        final Join<UserProfileEntity, CodeItemEntity> rankCdJoin = root.join("rankCdInfo", JoinType.LEFT);
        order.add(builder.desc(rankCdJoin.get("sortOrder")));
        order.add(builder.asc(root.get("ecnyDt")));
        return order;
    }
}
