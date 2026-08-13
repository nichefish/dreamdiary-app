package io.nicheblog.dreamdiary.feature.journal.entry.spec;

import io.nicheblog.dreamdiary.feature.attachable._shared.spec.BaseAttachableSpec;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.journal.chapter.entity.JournalChapterSmpEntity;
import io.nicheblog.dreamdiary.feature.journal.day.entity.JournalDaySmpEntity;
import io.nicheblog.dreamdiary.feature.journal.entry.entity.JournalEntryEntity;
import io.nicheblog.dreamdiary.feature.journal.reflection.entity.JournalReflectionEntity;
import io.nicheblog.dreamdiary.feature.journal.entry.service.policy.JournalEntryTagAxis;
import io.nicheblog.dreamdiary.feature.journal.entry.service.policy.JournalEntryTypePolicy;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class JournalEntrySpec extends BaseAttachableSpec<JournalEntryEntity> {

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
        final String sortField = String.valueOf(searchParamMap.getOrDefault("sortField", "DATE")).toUpperCase();
        final boolean asc = "asc".equals(sort);
        final Expression<LocalDate> dateExp = journalDayJoin.get("journalDate");
        if ("TITLE".equals(sortField)) {
            /* 제목 정렬: 빈 제목(null/'')은 방향과 무관하게 항상 맨 뒤로 민다. 그다음 제목 asc/desc,
               동일 제목은 일자·챕터·엔트리 순번으로 안정화한다. */
            final Expression<String> titleExp = root.get("title");
            final Expression<Integer> emptyTitleLast = builder.<Integer>selectCase()
                    .when(builder.or(builder.isNull(titleExp), builder.equal(titleExp, "")), 1)
                    .otherwise(0);
            final Expression<String> titleLowerExp = builder.lower(titleExp);
            order.add(builder.asc(emptyTitleLast));
            order.add(asc ? builder.asc(titleLowerExp) : builder.desc(titleLowerExp));
            order.add(builder.desc(dateExp));
        } else {
            order.add(asc ? builder.asc(dateExp) : builder.desc(dateExp));
        }
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
        final Expression<LocalDate> effectiveDtExp = journalDayJoin.get("journalDate");
        final String createdBy = resolveCreatedBy(searchParamMap);
        predicate.add(buildListContentTypePredicate(builder, root, contentType));

        for (final String key : searchParamMap.keySet()) {
            /* contentType 은 buildListContentTypePredicate 로 이미 스코프했다. default equal 에 들어가면 일기 축 OR 가 깨진다. */
            if ("sort".equals(key) || "sortField".equals(key) || "contentType".equals(key)) continue;

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
                    predicate.add(builder.equal(journalDayJoin.get("id"), value));
                    continue;
                case "journalChapterId":
                    predicate.add(builder.equal(chapterJoin.get("id"), value));
                    continue;
                case "refContentType":
                    // Reflection 한정 서브 facet. REFLECTION 검색에서만 target 유형으로 필터한다.
                    // Reflection 은 대상 필수(About-A)라 대상 없는(INDEPENDENT) 선택지는 없다.
                    if (value == null || contentType != ContentType.JOURNAL_REFLECTION) continue;
                    final String refCtValue = value.toString().trim();
                    if (refCtValue.isEmpty()) continue;
                    predicate.add(builder.equal(root.get("refContentType"), ContentType.get(refCtValue)));
                    continue;
                case "title":
                    /* 제목 전용 검색: 키워드(제목+본문)와 달리 제목만 LIKE 매칭한다. */
                    if (value == null) continue;
                    final String titleKeyword = value.toString().trim().toLowerCase();
                    if (StringUtils.isEmpty(titleKeyword)) continue;
                    predicate.add(builder.like(builder.lower(root.get("title")), "%" + titleKeyword + "%"));
                    continue;
                case "searchKeywords":
                    if (!(value instanceof List<?> rawKeywordList) || CollectionUtils.isEmpty(rawKeywordList)) continue;

                    final List<Predicate> likeList = new ArrayList<>();
                    /* 키워드별 제목 OR 본문, 복수 키워드 사이는 AND 계약을 유지한다.
                       원문·해석 한 몸: 이 엔트리를 target 으로 삼은 REFLECTION 본문에 키워드가 있으면 이 엔트리도 매칭한다
                       (canBeReflectionTarget 인 타입 검색에만 적용; REFLECTION 자체 검색은 자기 본문만 본다). */
                    final Expression<String> titleLowerExp = builder.lower(root.get("title"));
                    final Expression<String> cnLowerExp = builder.lower(root.get("content"));
                    final boolean matchTargetReflections = JournalEntryTypePolicy.from(contentType).canBeReflectionTarget();
                    for (final Object obj : rawKeywordList) {
                        if (obj == null) continue;
                        final String keyword = obj.toString().trim().toLowerCase();
                        if (StringUtils.isEmpty(keyword)) continue;
                        final String keywordPattern = "%" + keyword + "%";
                        final List<Predicate> orParts = new ArrayList<>();
                        orParts.add(builder.like(titleLowerExp, keywordPattern));
                        orParts.add(builder.like(cnLowerExp, keywordPattern));
                        if (matchTargetReflections) {
                            orParts.add(builder.exists(targetReflectionKeywordSubquery(query, builder, root, contentType, keywordPattern)));
                        }
                        likeList.add(builder.or(orParts.toArray(new Predicate[0])));
                    }
                    if (CollectionUtils.isEmpty(likeList)) continue;

                    predicate.add(builder.and(likeList.toArray(new Predicate[0])));
                    continue;
                case "tagId":
                    resolveTagIdPredicate(predicate, root, builder, value, createdBy, JournalEntryTagAxis.searchScopeKeys(contentType));
                    continue;
                case "tagIds":
                    resolveTagIdsPredicate(predicate, root, query, builder, value, createdBy, JournalEntryTagAxis.searchScopeKeys(contentType));
                    continue;
                case "states":
                    resolveStatesPredicate(predicate, root, query, builder, value, createdBy, JournalEntryTagAxis.searchScopeKeys(contentType));
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
     * 목록 검색의 contentType 스코프.
     * 검색 결과 행은 요청 타입의 Primary 엔트리(일기·꿈·노트)만이다. Reflection 은 별도 Aggregate
     * (journal_reflection)이고 대상 필수(About-A)라 검색 결과 행이 되지 않으며, 키워드 검색 시
     * {@link #targetReflectionKeywordSubquery} 로 대상 원문에만 기여한다.
     *
     * @param builder Criteria 빌더
     * @param root 엔트리 루트
     * @param contentType 요청 검색 타입
     * @return contentType 스코프 Predicate
     */
    private Predicate buildListContentTypePredicate(
            final CriteriaBuilder builder,
            final Root<JournalEntryEntity> root,
            final ContentType contentType
    ) {
        return builder.equal(root.get("contentType"), contentType.key);
    }

    /**
     * 이 엔트리를 target(refId)으로 삼은 Reflection 중 본문에 키워드가 있는 것이 존재하는지 확인하는 EXISTS 서브쿼리.
     * 원문·해석을 한 본문으로 취급하는 검색 계약에 쓴다. Reflection 은 별도 Aggregate(journal_reflection)이며 refId·
     * refContentType 으로 target 엔트리에 묶인다. soft-delete 행은 엔티티 {@code @Where(deleted_at IS NULL)} 로 자동 제외된다.
     *
     * @param query 상위 Criteria 쿼리
     * @param builder Criteria 빌더
     * @param root 상위 엔트리 루트(= Reflection 의 target)
     * @param contentType 상위(=target) 콘텐츠 타입
     * @param keywordPattern 소문자 LIKE 패턴("%kw%")
     * @return Reflection 본문 일치 EXISTS 서브쿼리
     */
    private Subquery<Integer> targetReflectionKeywordSubquery(
            final CriteriaQuery<?> query,
            final CriteriaBuilder builder,
            final Root<JournalEntryEntity> root,
            final ContentType contentType,
            final String keywordPattern
    ) {
        final Subquery<Integer> sub = query.subquery(Integer.class);
        final Root<JournalReflectionEntity> refl = sub.from(JournalReflectionEntity.class);
        sub.select(builder.literal(1));
        sub.where(
                builder.equal(refl.get("refId"), root.get("id")),
                builder.equal(refl.get("refContentType"), contentType),
                builder.like(builder.lower(refl.get("content")), keywordPattern)
        );
        return sub;
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
