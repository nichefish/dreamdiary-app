package io.nicheblog.dreamdiary.feature.journal.entry.spec;

import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.journal.entry.service.policy.JournalEntryTagAxis;
import io.nicheblog.dreamdiary.feature.journal.chapter.entity.JournalChapterSmpEntity;
import io.nicheblog.dreamdiary.feature.journal.day.entity.JournalDaySmpEntity;
import io.nicheblog.dreamdiary.feature.journal.entry.entity.JournalEntrySmpEntity;
import io.nicheblog.dreamdiary.feature.journal.entry.entity.JournalEntryTagContentEntity;
import io.nicheblog.dreamdiary.feature.journal.entry.entity.JournalEntryTagEntity;
import io.nicheblog.dreamdiary.global.intrfc.spec.BaseSpec;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import org.springframework.stereotype.Component;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Component
public class JournalEntryTagSpec
        extends BaseSpec<JournalEntryTagEntity> {

    /**
     * 태그 조회의 후처리 조건(distinct)을 설정한다.
     *
     * @param root 조회 루트
     * @param query Criteria 쿼리
     * @param builder Criteria 빌더
     */
    @Override
    public void postQuery(
            final Root<JournalEntryTagEntity> root,
            final CriteriaQuery<?> query,
            final CriteriaBuilder builder
    ) {
        query.distinct(true);
    }

    /**
     * 태그 조회용 검색 파라미터를 Predicate 목록으로 변환한다.
     *
     * @param searchParamMap 검색 파라미터
     * @param root 조회 루트
     * @param query Criteria 쿼리
     * @param builder Criteria 빌더
     * @return Predicate 목록
     * @throws Exception 변환/해석 중 예외
     */
    @Override
    public List<Predicate> getPredicateWithParams(
            final Map<String, Object> searchParamMap,
            final Root<JournalEntryTagEntity> root,
            final CriteriaQuery<?> query,
            final CriteriaBuilder builder
    ) throws Exception {
        final List<Predicate> predicate = new ArrayList<>();
        final ContentType contentType = ContentType.get((String) searchParamMap.get("contentType"));

        final Join<JournalEntryTagEntity, JournalEntryTagContentEntity> tagContentJoin = root.join("journalEntryTagList", JoinType.INNER);
        final Join<JournalEntryTagContentEntity, JournalEntrySmpEntity> entryJoin = tagContentJoin.join("journalEntry", JoinType.INNER);
        final Join<JournalEntrySmpEntity, JournalChapterSmpEntity> chapterJoin = entryJoin.join("journalChapter", JoinType.INNER);
        final Join<JournalChapterSmpEntity, JournalDaySmpEntity> journalDayJoin = chapterJoin.join("journalDay", JoinType.INNER);
        final Expression<LocalDate> effectiveDtExp = journalDayJoin.get("journalDate");

        final List<String> typeKeys = JournalEntryTagAxis.expandKeys(contentType);
        predicate.add(tagContentJoin.get("refContentType").in(typeKeys));
        predicate.add(entryJoin.get("contentType").in(typeKeys));

        for (final String key : searchParamMap.keySet()) {
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
                case "weekStartDt":
                    predicate.add(builder.equal(journalDayJoin.get(key), DateUtils.asLocalDate(value)));
                    continue;
                case "createdBy":
                    predicate.add(builder.equal(tagContentJoin.get("createdBy"), value));
                    continue;
                default:
                    continue;
            }
        }

        return predicate;
    }
}
