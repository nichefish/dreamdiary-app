package io.nicheblog.dreamdiary.feature.journal.day.spec;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.journal.day.entity.JournalDaySmpEntity;
import io.nicheblog.dreamdiary.feature.journal.day.entity.JournalDayTagContentEntity;
import io.nicheblog.dreamdiary.feature.journal.day.entity.JournalDayTagEntity;
import io.nicheblog.dreamdiary.global.intrfc.spec.BaseSpec;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * JournalDayTagSpec
 * <pre>
 *  저널 일자 태그 목록 검색용 Specification.
 * </pre>
 *
 * @author nichefish
 */
@Component
@Log4j2
public class JournalDayTagSpec
        implements BaseSpec<JournalDayTagEntity> {

    /**
     * 검색 조건 세팅 후 쿼리 후처리. (override)
     * 
     * @param root 조회할 엔티티의 Root 객체
     * @param query - CriteriaQuery 객체
     * @param builder CriteriaBuilder 객체
     */
    @Override
    public void postQuery(
            final Root<JournalDayTagEntity> root,
            final CriteriaQuery<?> query,
            final CriteriaBuilder builder
    ) {
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
            final Root<JournalDayTagEntity> root,
            final CriteriaQuery<?> query,
            final CriteriaBuilder builder
    ) throws Exception {

        final List<Predicate> predicate = new ArrayList<>();

        // 태그 조인
        final Join<JournalDayTagEntity, JournalDayTagContentEntity> journalDayTagJoin = root.join("journalDayTagList", JoinType.INNER);
        final Join<JournalDayTagContentEntity, JournalDaySmpEntity> journalDayJoin = journalDayTagJoin.join("journalDay", JoinType.INNER);
        final Expression<Date> effectiveDtExp = builder.coalesce(journalDayJoin.get("journalDt"), journalDayJoin.get("aprxmtDt"));

        predicate.add(builder.equal(journalDayTagJoin.get("refContentType"), ContentType.JOURNAL_DAY.key));
        // 파라미터 비교
        for (final String key : searchParamMap.keySet()) {
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
                case "weekStartDt":
                    predicate.add(builder.equal(journalDayJoin.get(key), DateUtils.asDate(value)));
                    continue;
                case "createdBy":
                    predicate.add(builder.equal(journalDayTagJoin.get("createdBy"), value));
                    continue;
                default:
                    continue;
            }
        }

        return predicate;
    }
}

