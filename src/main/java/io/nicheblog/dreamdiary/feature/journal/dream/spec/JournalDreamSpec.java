package io.nicheblog.dreamdiary.feature.journal.dream.spec;

import io.nicheblog.dreamdiary.feature.clsf._shared.spec.BaseClsfSpec;
import io.nicheblog.dreamdiary.feature.clsf._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.clsf.state.entity.StateEntity;
import io.nicheblog.dreamdiary.feature.clsf.tag.entity.TagContentEntity;
import io.nicheblog.dreamdiary.feature.clsf.tag.entity.embed.TagEmbed;
import io.nicheblog.dreamdiary.feature.journal.day.entity.JournalDaySmpEntity;
import io.nicheblog.dreamdiary.feature.journal.dream.entity.JournalDreamEntity;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.persistence.criteria.*;
import java.util.*;

/**
 * JournalDreamSpec
 * <pre>
 *  저널 꿈 목록 검색인자 세팅 Specification.
 * </pre>
 *
 * @author nichefish
 */
@Component
@Log4j2
public class JournalDreamSpec
        implements BaseClsfSpec<JournalDreamEntity> {

    /**
     * 검색 조건 세팅 후 쿼리 후처리. (override)
     * 
     * @param root 조회할 엔티티의 Root 객체
     * @param query CriteriaQuery 객체
     * @param builder CriteriaBuilder 객체
     * @param searchParamMap 검색 조건을 담은 파라미터 맵
     */
    @Override
    public void postQuery(
            final Root<JournalDreamEntity> root,
            final CriteriaQuery<?> query,
            final CriteriaBuilder builder,
            final Map<String, Object> searchParamMap
    ) {
        // 정렬 순서 변경
        final List<Order> order = new ArrayList<>();
        final Join<JournalDreamEntity, JournalDaySmpEntity> journalDayJoin = root.join("journalDay", JoinType.INNER);
        final String sort = String.valueOf(searchParamMap.getOrDefault("sort", "desc")).toLowerCase();
        final Expression<Date> dateExp = builder.coalesce(journalDayJoin.get("journalDt"), journalDayJoin.get("aprxmtDt"));
        if ("desc".equals(sort)) {
            order.add(builder.desc(dateExp));
        } else {
            order.add(builder.asc(dateExp));
        }
        order.add(builder.desc(builder.coalesce(journalDayJoin.get("journalDt"), journalDayJoin.get("aprxmtDt"))));
        order.add(builder.asc(root.get("idx")));
        query.orderBy(order);
        // distinct
        query.distinct(true);
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
            final Root<JournalDreamEntity> root,
            final CriteriaQuery<?> query,
            final CriteriaBuilder builder
    ) throws Exception {

        final List<Predicate> predicate = new ArrayList<>();
        // expressions
        final Join<JournalDreamEntity, JournalDaySmpEntity> journalDayJoin = root.join("journalDay", JoinType.INNER);
        final Expression<Date> effectiveDtExp = builder.coalesce(journalDayJoin.get("journalDt"), journalDayJoin.get("aprxmtDt"));
        final String createdBy = resolveCreatedBy(searchParamMap);

        // 파라미터 비교
        for (final String key : searchParamMap.keySet()) {
            if ("sort".equals(key)) continue;  // "sort" 파라미터는 건너뜀

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
                    final Integer yy = (Integer) value;
                    if (yy != 9999) predicate.add(builder.equal(journalDayJoin.get(key), yy));
                    continue;
                case "mnth":
                    // 99 = 모든 월
                    final Integer mnth = (Integer) value;
                    if (mnth != 99) predicate.add(builder.equal(journalDayJoin.get(key), mnth));
                    continue;
                case "journalDayId":
                    // 99 = 모든 월
                    predicate.add(builder.equal(journalDayJoin.get("id"), value));
                    continue;
                case "searchKeywords":
                    // 내용 like 검색
                    if (!(value instanceof List<?> rawList)) continue;
                    if (CollectionUtils.isEmpty(rawList)) continue;

                    final List<Predicate> likeList = new ArrayList<>();
                    final Expression<String> cnLowerExp = builder.lower(root.get("cn"));
                    for (final Object obj : rawList) {
                        if (obj == null) continue;
                        final String keyword = obj.toString().trim().toLowerCase();
                        if (StringUtils.isEmpty(keyword)) continue;

                        likeList.add(builder.like(cnLowerExp, "%" + keyword + "%"));
                    }
                    if (CollectionUtils.isEmpty(likeList)) continue;

                    predicate.add(builder.and(likeList.toArray(new Predicate[0])));
                    continue;
                case "tagId": {
                    // 특정 태그된 꿈만 조회
                    final Join<JournalDreamEntity, TagEmbed> tagJoin = root.join("tag", JoinType.INNER);
                    final Join<TagEmbed, TagContentEntity> tagContentJoin = tagJoin.join("list", JoinType.INNER);
                    predicate.add(builder.equal(tagContentJoin.get("createdBy"), createdBy));
                    predicate.add(builder.equal(tagContentJoin.get("tagId"), value));
                    predicate.add(builder.equal(tagContentJoin.get("refContentType"), ContentType.JOURNAL_DIARY.key));
                    continue;
                }
                case "tagIds":
                    // 태그 exists 검색
                    if (!(value instanceof List<?> rawList)) continue;
                    if (CollectionUtils.isEmpty(rawList)) continue;

                    final List<Integer> tagIds = rawList.stream()
                        .filter(Objects::nonNull)
                        .map(o -> (Integer) o)
                        .toList();

                    if (tagIds.isEmpty()) break;
                    final Subquery<Long> sub = query.subquery(Long.class);
                    final Root<TagContentEntity> subRoot = sub.from(TagContentEntity.class);

                    sub.select(subRoot.get("refId"));
                    sub.where(
                        builder.and(
                            builder.equal(subRoot.get("refId"), root.get("id")),
                            builder.equal(subRoot.get("refContentType"), ContentType.JOURNAL_DREAM.key),
                            builder.equal(subRoot.get("createdBy"), createdBy),
                            subRoot.get("tagId").in(tagIds)
                        )
                    );

                    predicate.add(builder.exists(sub));
                    continue;
                // 상태 검색
                case "states":
                    if (!(value instanceof List<?> rawList)) continue;
                    if (CollectionUtils.isEmpty(rawList)) continue;

                    final Subquery<Long> subquery = query.subquery(Long.class);
                    final Root<StateEntity> stateRoot = subquery.from(StateEntity.class);
                    final List<String> states = rawList.stream()
                        .filter(Objects::nonNull)
                        .map(o -> o.toString().trim())
                        .filter(StringUtils::isNotEmpty)
                        .toList();

                    subquery.select(stateRoot.get("refId"));
                    subquery.where(
                        builder.and(
                            builder.equal(stateRoot.get("refId"), root.get("id")),
                            builder.equal(stateRoot.get("refContentType"), ContentType.JOURNAL_DREAM.key),
                            stateRoot.get("stateCd").in(states)
                        )
                    );

                    predicate.add(builder.equal(root.get("createdBy"), createdBy));
                    predicate.add(builder.exists(subquery));
                    break;
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

    private String resolveCreatedBy(final Map<String, Object> searchParamMap) {
        final Object createdBy = searchParamMap.get("createdBy");
        if (createdBy != null) {
            final String createdByStr = createdBy.toString();
            if (!createdByStr.isBlank()) return createdByStr;
        }
        throw new IllegalArgumentException("createdBy is required.");
    }

}

