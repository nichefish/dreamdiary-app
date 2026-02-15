package io.nicheblog.dreamdiary.domain.jrnl.diary.spec;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.domain.jrnl.day.entity.JrnlDaySmpEntity;
import io.nicheblog.dreamdiary.domain.jrnl.diary.entity.JrnlDiaryEntity;
import io.nicheblog.dreamdiary.domain.jrnl.diary.entity.JrnlDiarySmpEntity;
import io.nicheblog.dreamdiary.domain.jrnl.entry.entity.JrnlEntrySmpEntity;
import io.nicheblog.dreamdiary.extension.clsf.state.entity.StateEntity;
import io.nicheblog.dreamdiary.extension.clsf.state.entity.embed.StateEmbed;
import io.nicheblog.dreamdiary.extension.clsf.tag.entity.TagContentEntity;
import io.nicheblog.dreamdiary.extension.clsf.tag.entity.embed.TagEmbed;
import io.nicheblog.dreamdiary.global.intrfc.spec.BaseClsfSpec;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.tika.utils.StringUtils;
import org.springframework.stereotype.Component;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * JrnlDiarySpec
 * <pre>
 *  저널 일기 목록 검색인자 세팅 Specification.
 * </pre>
 *
 * @author nichefish
 */
@Component
@Log4j2
public class JrnlDiarySpec
        implements BaseClsfSpec<JrnlDiaryEntity> {

    /**
     * 검색 조건 세팅 후 쿼리 후처리. (override)
     * 
     * @param root 조회할 엔티티의 Root 객체
     * @param query - CriteriaQuery 객체
     * @param builder CriteriaBuilder 객체
     */
    @Override
    public void postQuery(
            final Root<JrnlDiaryEntity> root,
            final CriteriaQuery<?> query,
            final CriteriaBuilder builder
    ) {
        // 정렬 순서 변경
        final List<Order> order = new ArrayList<>();
        final Join<JrnlDiaryEntity, JrnlEntrySmpEntity> jrnlEntryJoin = root.join("jrnlEntry", JoinType.INNER);
        final Join<JrnlEntrySmpEntity, JrnlDaySmpEntity> jrnlDayJoin = jrnlEntryJoin.join("jrnlDay", JoinType.INNER);
        order.add(builder.desc(builder.coalesce(jrnlDayJoin.get("jrnlDt"), jrnlDayJoin.get("aprxmtDt"))));
        order.add(builder.asc(jrnlEntryJoin.get("idx")));
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
            final Root<JrnlDiaryEntity> root,
            final CriteriaQuery<?> query,
            final CriteriaBuilder builder
    ) throws Exception {

        final List<Predicate> predicate = new ArrayList<>();

        // expressions
        final Join<JrnlDiarySmpEntity, JrnlEntrySmpEntity> jrnlEntryJoin = root.join("jrnlEntry", JoinType.INNER);
        final Join<JrnlEntrySmpEntity, JrnlDaySmpEntity> jrnlDayJoin = jrnlEntryJoin.join("jrnlDay", JoinType.INNER);
        final Expression<Date> effectiveDtExp = builder.coalesce(jrnlDayJoin.get("jrnlDt"), jrnlDayJoin.get("aprxmtDt"));

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
                    if (yy != 9999) predicate.add(builder.equal(jrnlDayJoin.get(key), yy));
                    continue;
                case "mnth":
                    // 99 = 모든 월
                    final Integer mnth = (Integer) value;
                    if (mnth != 99) predicate.add(builder.equal(jrnlDayJoin.get(key), mnth));
                    continue;
                case "jrnlDayNo":
                    // 99 = 모든 월
                    predicate.add(builder.equal(jrnlDayJoin.get("postNo"), value));
                    continue;
                case "searchKeywords": {
                    // 내용 like 검색
                    if (!(value instanceof List<?> rawList)) continue;
                    if (CollectionUtils.isEmpty(rawList)) continue;

                    final List<Predicate> likeList = new ArrayList<>();
                    final Expression<String> cnLowerExp = builder.lower(root.get("cn"));
                    for (final Object obj : rawList) {
                        if (obj == null) continue;
                        final String keyword = obj.toString().trim();
                        if (StringUtils.isEmpty(keyword)) continue;

                        likeList.add(builder.like(cnLowerExp, "%" + keyword.toLowerCase() + "%"));
                    }
                    if (CollectionUtils.isEmpty(likeList)) continue;

                    predicate.add(builder.and(likeList.toArray(new Predicate[0])));
                    continue;
                }
                case "tagNo": {
                    // 특정 태그된 꿈만 조회
                    final Join<JrnlDiaryEntity, TagEmbed> tagJoin = root.join("tag", JoinType.INNER);
                    final Join<TagEmbed, TagContentEntity> tagContentJoin = tagJoin.join("list", JoinType.INNER);
                    predicate.add(builder.equal(tagContentJoin.get("regstrId"), AuthUtils.getLgnUserId()));
                    predicate.add(builder.equal(tagContentJoin.get("refTagNo"), value));
                    continue;
                }
                case "tagNos":
                    // 내용 like 검색
                    if (!(value instanceof List<?> rawList)) continue;
                    if (CollectionUtils.isEmpty(rawList)) continue;

                    final Join<JrnlDiaryEntity, TagEmbed> tagJoin = root.join("tag", JoinType.INNER);
                    final Join<TagEmbed, TagContentEntity> tagContentJoin = tagJoin.join("list", JoinType.INNER);
                    predicate.add(builder.equal(tagContentJoin.get("regstrId"), AuthUtils.getLgnUserId()));

                    for (final Object obj : rawList) {
                        if (obj == null) continue;
                        final Integer tagNo = (Integer) obj;

                        Subquery<Long> sub = query.subquery(Long.class);
                        Root<TagContentEntity> subRoot = sub.from(TagContentEntity.class);

                        sub.select(subRoot.get("refPostNo"));
                        sub.where(
                            builder.and(
                                builder.equal(subRoot.get("refPostNo"), root.get("postNo")),
                                builder.equal(subRoot.get("refTagNo"), tagNo),
                                builder.equal(subRoot.get("regstrId"), AuthUtils.getLgnUserId())
                            )
                        );

                        predicate.add(builder.exists(sub));
                    }
                    continue;
                case "state":
                    // 상태 검색
                    final Join<JrnlDiaryEntity, StateEmbed> stateJoin = root.join("state", JoinType.INNER);
                    final Join<StateEmbed, StateEntity> stateListJoin = stateJoin.join("list", JoinType.INNER);
                    predicate.add(builder.equal(stateListJoin.get("stateCd"), value));
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
