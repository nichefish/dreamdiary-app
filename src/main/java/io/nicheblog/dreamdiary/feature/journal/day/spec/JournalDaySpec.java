package io.nicheblog.dreamdiary.feature.journal.day.spec;

import io.nicheblog.dreamdiary.feature.attachable._shared.spec.BaseAttachableSpec;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.meta.entity.MetaContentEntity;
import io.nicheblog.dreamdiary.feature.attachable.meta.entity.embed.MetaEmbed;
import io.nicheblog.dreamdiary.feature.journal.day.entity.JournalDayEntity;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * JournalDaySpec
 * <pre>
 *  저널 일자 목록 검색인자 세팅 Specification.
 * </pre>
 *
 * @author nichefish
 */
@Component
@Log4j2
public class JournalDaySpec
        implements BaseAttachableSpec<JournalDayEntity> {

    /**
     * 검색 조건 세팅 후 쿼리 후처리. (override)
     * 
     * @param root 조회할 엔티티의 Root 객체
     * @param query - CriteriaQuery 객체
     * @param builder CriteriaBuilder 객체
     */
    @Override
    public void postQuery(
            final Root<JournalDayEntity> root,
            final CriteriaQuery<?> query,
            final CriteriaBuilder builder,
            final Map<String, Object> searchParamMap
    ) {
        // 정렬 순서 변경 :: 날짜 오름차순 정렬
        final List<Order> order = new ArrayList<>();
        final String sortStr = (String) searchParamMap.get("sort");
        if (StringUtils.isNotEmpty(sortStr) && "DESC".equals(sortStr)) {
            order.add(builder.desc(root.get("journalDate")));
        } else {
            order.add(builder.asc(root.get("journalDate")));
        }
        query.orderBy(order);
    }

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
            final Root<JournalDayEntity> root,
            final CriteriaQuery<?> query,
            final CriteriaBuilder builder
    ) throws Exception {

        final List<Predicate> predicate = new ArrayList<>();

        final Expression<Date> effectiveDtExp = root.get("journalDate");
        final String createdBy = resolveCreatedBy(searchParamMap);

        // 파라미터 비교
        for (final String key : searchParamMap.keySet()) {
            if ("sort".equals(key)) continue;  // "sort" 파라미터는 건너뜀
            if ("createdBy".equals(key)) continue;

            final Object value = searchParamMap.get(key);
            switch (key) {
                case "searchStartDt":
                    // 기간 검색
                    predicate.add(builder.greaterThanOrEqualTo(effectiveDtExp, DateUtils.asDate(value)));
                    continue;
                case "searchEndDt":
                    // 기간 검색
                    predicate.add(builder.lessThanOrEqualTo(effectiveDtExp, DateUtils.asDate(value)));
                    continue;
                case "yy":
                    // 9999 = 모든 년
                    Integer yy = (Integer) value;
                    if (yy != 9999) predicate.add(builder.equal(root.get(key), yy));
                    continue;
                case "mnth":
                    Integer mnth = (Integer) value;
                    if (mnth != 99) predicate.add(builder.equal(root.get(key), mnth));
                    continue;
                case "stdrdDt":
                    predicate.add(builder.equal(effectiveDtExp, DateUtils.asDate(value)));
                    continue;
                case "weekStartDt":
                    predicate.add(builder.equal(root.get(key), DateUtils.asDate(value)));
                    continue;
                case "tagId":
                    resolveTagIdPredicate(predicate, root, builder, value, createdBy, ContentType.JOURNAL_DAY);
                    continue;
                case "metaId":
                    // 특정 메타 지칭된 일자만 조회
                    final Join<JournalDayEntity, MetaEmbed> metaJoin = root.join("meta", JoinType.INNER);
                    final Join<MetaEmbed, MetaContentEntity> metaContentJoin = metaJoin.join("list", JoinType.INNER);
                    predicate.add(builder.equal(metaContentJoin.get("createdBy"), createdBy));
                    predicate.add(builder.equal(metaContentJoin.get("metaId"), value));
                    continue;
                default:
                    // default :: 조건 파라미터에 대해 equal 검색
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
