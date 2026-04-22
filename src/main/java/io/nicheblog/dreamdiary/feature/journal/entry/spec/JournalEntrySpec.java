package io.nicheblog.dreamdiary.feature.journal.entry.spec;

import io.nicheblog.dreamdiary.feature.attachable._shared.spec.BaseAttachableSpec;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.journal.chapter.entity.JournalChapterSmpEntity;
import io.nicheblog.dreamdiary.feature.journal.day.entity.JournalDaySmpEntity;
import io.nicheblog.dreamdiary.feature.journal.entry.entity.JournalEntryEntity;
import io.nicheblog.dreamdiary.feature.journal.entry.service.policy.JournalEntryTypePolicy;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Log4j2
public class JournalEntrySpec implements BaseAttachableSpec<JournalEntryEntity> {

    /**
     * 조회 후 정렬 조건과 distinct 설정을 적용한다.
     *
     * @param root 조회 루트
     * @param query Criteria 쿼리
     * @param builder Criteria 빌더
     * @param searchParamMap 검색 파라미터
     */
    @Override
    public void postQuery(
            final Root<JournalEntryEntity> root,
            final CriteriaQuery<?> query,
            final CriteriaBuilder builder,
            final Map<String, Object> searchParamMap
    ) {
        final List<Order> order = new ArrayList<>();
        final Join<JournalEntryEntity, JournalChapterSmpEntity> chapterJoin = root.join("journalChapter", JoinType.INNER);
        final Join<JournalChapterSmpEntity, JournalDaySmpEntity> journalDayJoin = chapterJoin.join("journalDay", JoinType.INNER);
        final String sort = String.valueOf(searchParamMap.getOrDefault("sort", "desc")).toLowerCase();
        final Expression<Date> dateExp = journalDayJoin.get("journalDate");
        order.add("desc".equals(sort) ? builder.desc(dateExp) : builder.asc(dateExp));
        order.add(builder.asc(chapterJoin.get("sortOrder")));
        order.add(builder.asc(root.get("sortOrder")));
        query.orderBy(order);
        query.distinct(true);
    }

    /**
     * 검색 파라미터를 기반으로 엔트리 조회 Predicate 목록을 구성한다.
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
            final Root<JournalEntryEntity> root,
            final CriteriaQuery<?> query,
            final CriteriaBuilder builder
    ) throws Exception {

        final List<Predicate> predicate = new ArrayList<>();
        final ContentType contentType = resolveContentType(searchParamMap);
        final Join<JournalEntryEntity, JournalChapterSmpEntity> chapterJoin = root.join("journalChapter", JoinType.INNER);
        final Join<JournalChapterSmpEntity, JournalDaySmpEntity> journalDayJoin = chapterJoin.join("journalDay", JoinType.INNER);
        final Expression<Date> effectiveDtExp = journalDayJoin.get("journalDate");
        final String createdBy = resolveCreatedBy(searchParamMap);
        predicate.add(builder.equal(root.get("contentType"), contentType.key));

        for (final String key : searchParamMap.keySet()) {
            if ("sort".equals(key)) continue;

            final Object value = searchParamMap.get(key);
            switch (key) {
                case "searchStartDt":
                    predicate.add(builder.greaterThanOrEqualTo(effectiveDtExp, DateUtils.asDate(value)));
                    continue;
                case "searchEndDt":
                    predicate.add(builder.lessThanOrEqualTo(effectiveDtExp, DateUtils.asDate(value)));
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
                    predicate.add(builder.equal(journalDayJoin.get("id"), value));
                    continue;
                case "journalChapterId":
                    predicate.add(builder.equal(chapterJoin.get("id"), value));
                    continue;
                case "searchKeywords":
                    if (!(value instanceof List<?> rawKeywordList) || CollectionUtils.isEmpty(rawKeywordList)) continue;

                    final List<Predicate> likeList = new ArrayList<>();
                    final Expression<String> cnLowerExp = builder.lower(root.get("content"));
                    for (final Object obj : rawKeywordList) {
                        if (obj == null) continue;
                        final String keyword = obj.toString().trim().toLowerCase();
                        if (StringUtils.isEmpty(keyword)) continue;
                        likeList.add(builder.like(cnLowerExp, "%" + keyword + "%"));
                    }
                    if (CollectionUtils.isEmpty(likeList)) continue;

                    predicate.add(builder.and(likeList.toArray(new Predicate[0])));
                    continue;
                case "tagId":
                    resolveTagIdPredicate(predicate, root, builder, value, createdBy, contentType);
                    continue;
                case "tagIds":
                    resolveTagIdsPredicate(predicate, root, query, builder, value, createdBy, contentType);
                    continue;
                case "states":
                    resolveStatesPredicate(predicate, root, query, builder, value, createdBy, contentType);
                    break;
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

    /**
     * 검색 파라미터에서 엔트리용 콘텐츠 타입을 해석한다.
     *
     * @param searchParamMap 검색 파라미터
     * @return 엔트리 콘텐츠 타입
     */
    private ContentType resolveContentType(final Map<String, Object> searchParamMap) {
        final Object value = searchParamMap.get("contentType");
        final ContentType contentType = value instanceof ContentType type
                ? type
                : ContentType.get(value != null ? value.toString() : null);
        if (!JournalEntryTypePolicy.isEntryType(contentType)) {
            throw new IllegalArgumentException("contentType is required for journal entry search.");
        }
        return contentType;
    }

}
