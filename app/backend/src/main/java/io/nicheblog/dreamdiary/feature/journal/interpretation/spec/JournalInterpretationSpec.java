package io.nicheblog.dreamdiary.feature.journal.interpretation.spec;

import io.nicheblog.dreamdiary.feature.attachable._shared.spec.BaseAttachableSpec;
import io.nicheblog.dreamdiary.feature.journal.day.entity.JournalDaySmpEntity;
import io.nicheblog.dreamdiary.feature.journal.interpretation.entity.JournalInterpretationEntity;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * JournalInterpretationSpec
 * <pre>
 *  저널 해석 목록 검색인자 세팅 Specification.
 * </pre>
 *
 * @author nichefish
 */
@Component
@Log4j2
public class JournalInterpretationSpec
        implements BaseAttachableSpec<JournalInterpretationEntity> {

    /**
     * 검색 조건 세팅 후 쿼리 후처리. (override)
     *
     * @param root 조회할 엔티티의 Root 객체
     * @param query - CriteriaQuery 객체
     * @param builder CriteriaBuilder 객체
     */
    @Override
    public void postQuery(
            final Root<JournalInterpretationEntity> root,
            final CriteriaQuery<?> query,
            final CriteriaBuilder builder
    ) {
        // 정렬 순서 변경: journal_day 직접 join
        final List<Order> order = new ArrayList<>();
        final Join<JournalInterpretationEntity, JournalDaySmpEntity> journalDayJoin = root.join("journalDay", JoinType.LEFT);
        order.add(builder.desc(journalDayJoin.get("journalDate")));
        order.add(builder.asc(root.get("sortOrder")));
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
            final Root<JournalInterpretationEntity> root,
            final CriteriaQuery<?> query,
            final CriteriaBuilder builder
    ) throws Exception {

        final List<Predicate> predicate = new ArrayList<>();
        // journal_day 직접 join (ref_id → journal_day_id 비정규화 활용)
        final Join<JournalInterpretationEntity, JournalDaySmpEntity> journalDayJoin = root.join("journalDay", JoinType.LEFT);
        final Expression<LocalDate> effectiveDtExp = journalDayJoin.get("journalDate");

        // 파라미터 비교
        for (final String key : searchParamMap.keySet()) {
            if ("sort".equals(key)) continue;

            final Object value = searchParamMap.get(key);
            switch (key) {
                case "searchStartDt":
                    predicate.add(builder.greaterThanOrEqualTo(effectiveDtExp, DateUtils.asLocalDate(value)));
                    continue;
                case "searchEndDt":
                    predicate.add(builder.lessThanOrEqualTo(effectiveDtExp, DateUtils.asLocalDate(value)));
                    continue;
                case "yy":
                    final Integer yy = (Integer) value;
                    if (yy != 9999) predicate.add(builder.equal(journalDayJoin.get(key), yy));
                    continue;
                case "mnth":
                    final Integer mnth = (Integer) value;
                    if (mnth != 99) predicate.add(builder.equal(journalDayJoin.get(key), mnth));
                    continue;
                case "journalDayId":
                    predicate.add(builder.equal(root.get("journalDayId"), value));
                    continue;
                case "refId":
                    predicate.add(builder.equal(root.get("refId"), value));
                    continue;
                case "refContentType":
                    predicate.add(builder.equal(root.get("refContentType"), value));
                    continue;
                case "diaryKeyword":
                    predicate.add(builder.like(root.get("content"), "%" + value + "%"));
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
