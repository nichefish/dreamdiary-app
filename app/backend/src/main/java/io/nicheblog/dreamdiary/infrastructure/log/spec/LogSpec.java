package io.nicheblog.dreamdiary.infrastructure.log.spec;

import io.nicheblog.dreamdiary.global.intrfc.spec.BaseSpec;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import io.nicheblog.dreamdiary.infrastructure.log.entity.LogEntity;
import io.nicheblog.dreamdiary.infrastructure.log.type.LogType;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Component
@Log4j2
public class LogSpec
        implements BaseSpec<LogEntity> {

    @Override
    public List<Predicate> getPredicateWithParams(
            final Map<String, Object> searchParamMap,
            final Root<LogEntity> root,
            final CriteriaQuery<?> query,
            final CriteriaBuilder builder
    ) throws Exception {

        final List<Predicate> predicate = new ArrayList<>();
        final Expression<Date> createdAtExp = root.get("createdAt");

        predicate.add(builder.or(
                builder.isNull(root.get("logType")),
                builder.notEqual(root.get("logType"), LogType.SYSTEM)
        ));

        for (final String key : searchParamMap.keySet()) {
            final Object value = searchParamMap.get(key);
            switch (key) {
                case "searchStartDt":
                    predicate.add(builder.greaterThanOrEqualTo(createdAtExp, DateUtils.asDate(value)));
                    continue;
                case "searchEndDt":
                    predicate.add(builder.lessThanOrEqualTo(createdAtExp, DateUtils.asDate(value)));
                    continue;
                case "rslt":
                    final Expression<Boolean> resultExp = root.get("result");
                    predicate.add("true".equals(value) ? builder.isTrue(resultExp) : builder.isFalse(resultExp));
                    continue;
                case "minDurationMs":
                    predicate.add(builder.greaterThanOrEqualTo(root.get("durationMs"), Long.valueOf(String.valueOf(value))));
                    continue;
                case "hasException":
                    if (Boolean.parseBoolean(String.valueOf(value))) {
                        predicate.add(builder.isNotNull(root.get("exceptionName")));
                    }
                    continue;
                case "requestUri":
                case "traceId":
                case "username":
                case "message":
                case "signature":
                    predicate.add(builder.like(
                            builder.lower(root.get(key).as(String.class)),
                            "%" + String.valueOf(value).toLowerCase() + "%"
                    ));
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
