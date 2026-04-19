package io.nicheblog.dreamdiary.feature.journal.chapter.spec;

import io.nicheblog.dreamdiary.feature.attachable._shared.spec.BaseAttachableSpec;
import io.nicheblog.dreamdiary.feature.attachable.tag.entity.TagContentEntity;
import io.nicheblog.dreamdiary.feature.attachable.tag.entity.embed.TagEmbed;
import io.nicheblog.dreamdiary.feature.journal.chapter.entity.JournalChapterEntity;
import io.nicheblog.dreamdiary.feature.journal.chapter.type.ChapterType;
import io.nicheblog.dreamdiary.feature.journal.day.entity.JournalDaySmpEntity;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * JournalChapterSpec
 * <pre>
 *  저널 챕터 목록 검색인자 세팅 Specification.
 * </pre>
 *
 * @author nichefish
 */
@Component
@Log4j2
public class JournalChapterSpec
        implements BaseAttachableSpec<JournalChapterEntity> {

    /**
     * 검색 조건 세팅 후 쿼리 후처리. (override)
     * 
     * @param root 조회할 엔티티의 Root 객체
     * @param query - CriteriaQuery 객체
     * @param builder CriteriaBuilder 객체
     */
    @Override
    public void postQuery(
            final Root<JournalChapterEntity> root,
            final CriteriaQuery<?> query,
            final CriteriaBuilder builder
    ) {
        // 정렬 순서 변경
        final List<Order> order = new ArrayList<>();
        final Join<JournalChapterEntity, JournalDaySmpEntity> journalDayJoin = root.join("journalDay", JoinType.INNER);
        order.add(builder.desc(journalDayJoin.get("journalDate")));
        // 동일 일자(또는 동일 조회 묶음) 내: 일기 → 노트 → 꿈, 그 다음 sort_order
        final Expression<Integer> chapterTypeRank = builder.<Integer>selectCase()
                .when(builder.equal(root.get("chapterType"), ChapterType.DIARY), builder.literal(1))
                .when(builder.equal(root.get("chapterType"), ChapterType.NOTE), builder.literal(2))
                .when(builder.equal(root.get("chapterType"), ChapterType.DREAM), builder.literal(3))
                .otherwise(builder.literal(99));
        order.add(builder.asc(chapterTypeRank));
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
            final Root<JournalChapterEntity> root,
            final CriteriaQuery<?> query,
            final CriteriaBuilder builder
    ) throws Exception {

        final List<Predicate> predicate = new ArrayList<>();
        // expressions
        final Join<JournalChapterEntity, JournalDaySmpEntity> journalDayJoin = root.join("journalDay", JoinType.INNER);
        final Expression<Date> effectiveDtExp = journalDayJoin.get("journalDate");
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
                case "dreamKeyword":
                    // 내용 like 검색
                    predicate.add(builder.like(root.get("content"), "%" + value + "%"));
                    continue;
                case "tagId":
                    // 특정 태그된 항목만 조회
                    final Join<JournalChapterEntity, TagEmbed> tagJoin = root.join("tag", JoinType.INNER);
                    final Join<TagEmbed, TagContentEntity> tagContentJoin = tagJoin.join("list", JoinType.INNER);
                    predicate.add(builder.equal(tagContentJoin.get("createdBy"), createdBy));
                    predicate.add(builder.equal(tagContentJoin.get("tagId"), value));
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

    private String resolveCreatedBy(final Map<String, Object> searchParamMap) {
        final Object createdBy = searchParamMap.get("createdBy");
        if (createdBy != null) {
            final String createdByStr = createdBy.toString();
            if (!createdByStr.isBlank()) return createdByStr;
        }
        throw new IllegalArgumentException("createdBy is required.");
    }

}

