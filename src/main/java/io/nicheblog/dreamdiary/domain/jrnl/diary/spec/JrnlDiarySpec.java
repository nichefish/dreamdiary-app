package io.nicheblog.dreamdiary.domain.jrnl.diary.spec;

import io.nicheblog.dreamdiary.auth.util.AuthUtils;
import io.nicheblog.dreamdiary.domain.jrnl.day.entity.JrnlDaySmpEntity;
import io.nicheblog.dreamdiary.domain.jrnl.diary.entity.JrnlDiaryEntity;
import io.nicheblog.dreamdiary.domain.jrnl.dream.entity.JrnlDreamEntity;
import io.nicheblog.dreamdiary.extension.tag.entity.ContentTagEntity;
import io.nicheblog.dreamdiary.extension.tag.entity.embed.TagEmbed;
import io.nicheblog.dreamdiary.global.intrfc.spec.BasePostSpec;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.extern.log4j.Log4j2;
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
        implements BasePostSpec<JrnlDiaryEntity> {

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
        final Join<JrnlDreamEntity, JrnlDaySmpEntity> jrnlDayJoin = root.join("jrnlDay", JoinType.INNER);
        order.add(builder.desc(builder.coalesce(jrnlDayJoin.get("jrnlDt"), jrnlDayJoin.get("aprxmtDt"))));
        query.orderBy(order);
        // distinct
        query.distinct(true);
    }

    /**
     * 인자별로 구체적인 검색 조건을 세팅한다. (override)
     *
     * @param searchParamMap 검색 파라미터 맵
     * @param root 검색할 엔티티의 Root 객체
     * @param builder 검색 조건을 생성하는 CriteriaBuilder 객체
     * @return {@link List} -- 설정된 검색 조건(Predicate) 리스트
     * @throws Exception 검색 조건 생성 중 발생할 수 있는 예외
     */
    @Override
    public List<Predicate> getPredicateWithParams(
            final Map<String, Object> searchParamMap,
            final Root<JrnlDiaryEntity> root,
            final CriteriaBuilder builder
    ) throws Exception {

        final List<Predicate> predicate = new ArrayList<>();

        // expressions
        final Join<JrnlDreamEntity, JrnlDaySmpEntity> jrnlDayJoin = root.join("jrnlDay", JoinType.INNER);
        final Expression<Date> effectiveDtExp = builder.coalesce(jrnlDayJoin.get("jrnlDt"), jrnlDayJoin.get("aprxmtDt"));

        // 파라미터 비교
        for (String key : searchParamMap.keySet()) {
            if ("sort".equals(key)) continue;  // "sort" 파라미터는 건너뜀

            switch (key) {
                case "searchStartDt":
                    // 기간 검색
                    predicate.add(builder.greaterThanOrEqualTo(effectiveDtExp, DateUtils.asDate(searchParamMap.get(key))));
                    continue;
                case "searchEndDt":
                    // 기간 검색
                    predicate.add(builder.lessThanOrEqualTo(effectiveDtExp, DateUtils.asDate(searchParamMap.get(key))));
                    continue;
                case "yy":
                    // 9999 = 모든 년
                    final Integer yy = (Integer) searchParamMap.get(key);
                    if (yy != 9999) predicate.add(builder.equal(jrnlDayJoin.get(key), yy));
                    continue;
                case "mnth":
                    // 99 = 모든 월
                    final Integer mnth = (Integer) searchParamMap.get(key);
                    if (mnth != 99) predicate.add(builder.equal(jrnlDayJoin.get(key), mnth));
                    continue;
                case "diaryKeyword":
                    // 내용 like 검색
                    predicate.add(builder.like(root.get("cn"), "%" + searchParamMap.get(key) + "%"));
                    continue;
                case "tagNo":
                    // 특정 태그된 꿈만 조회
                    final Join<JrnlDiaryEntity, TagEmbed> tagJoin = root.join("tag", JoinType.INNER);
                    final Join<TagEmbed, ContentTagEntity> contentTagJoin = tagJoin.join("list", JoinType.INNER);
                    predicate.add(builder.equal(contentTagJoin.get("regstrId"), AuthUtils.getLgnUserId()));
                    predicate.add(builder.equal(contentTagJoin.get("refTagNo"), searchParamMap.get(key)));
                    continue;
                default:
                    // default :: 조건 파라미터에 대해 equal 검색
                    try {
                        predicate.add(builder.equal(root.get(key), searchParamMap.get(key)));
                    } catch (final Exception e) {
                        log.info("unable to locate attribute '{}' while trying root.get(key).", key);
                    }
            }
        }

        return predicate;
    }
}
