package io.nicheblog.dreamdiary.domain.jrnl.intrpt.spec;

import io.nicheblog.dreamdiary.domain.jrnl.day.entity.JrnlDaySmpEntity;
import io.nicheblog.dreamdiary.domain.jrnl.dream.entity.JrnlDreamSmpEntity;
import io.nicheblog.dreamdiary.domain.jrnl.intrpt.entity.JrnlIntrptSmpEntity;
import io.nicheblog.dreamdiary.domain.jrnl.intrpt.entity.JrnlIntrptTagContentEntity;
import io.nicheblog.dreamdiary.domain.jrnl.intrpt.entity.JrnlIntrptTagEntity;
import io.nicheblog.dreamdiary.extension.clsf.ContentType;
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
 * JrnlIntrptTagSpec
 * <pre>
 *  저널 일기 태그 목록 검색인자 세팅 Specification.
 * </pre>
 *
 * @author nichefish
 */
@Component
@Log4j2
public class JrnlIntrptTagSpec
        implements BaseSpec<JrnlIntrptTagEntity> {

    /**
     * 검색 조건 세팅 후 쿼리 후처리. (override)
     * 
     * @param root 조회할 엔티티의 Root 객체
     * @param query - CriteriaQuery 객체
     * @param builder CriteriaBuilder 객체
     */
    @Override
    public void postQuery(
            final Root<JrnlIntrptTagEntity> root,
            final CriteriaQuery<?> query,
            final CriteriaBuilder builder
    ) {
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
     */
    @Override
    public List<Predicate> getPredicateWithParams(
            final Map<String, Object> searchParamMap,
            final Root<JrnlIntrptTagEntity> root,
            final CriteriaBuilder builder
    ) throws Exception {

        final List<Predicate> predicate = new ArrayList<>();

        // 태그 조인
        final Join<JrnlIntrptTagEntity, JrnlIntrptTagContentEntity> JrnlIntrptTagJoin = root.join("jrnlIntrptTagList", JoinType.INNER);
        final Join<JrnlIntrptTagContentEntity, JrnlIntrptSmpEntity> JrnlIntrptJoin = JrnlIntrptTagJoin.join("jrnlIntrpt", JoinType.INNER);
        final Join<JrnlIntrptSmpEntity, JrnlDreamSmpEntity> jrnlDreamJoin = JrnlIntrptJoin.join("jrnlDream", JoinType.INNER);
        final Join<JrnlDreamSmpEntity, JrnlDaySmpEntity> jrnlDayJoin = jrnlDreamJoin.join("jrnlDay", JoinType.INNER);
        final Expression<Date> effectiveDtExp = builder.coalesce(jrnlDayJoin.get("jrnlDt"), jrnlDayJoin.get("aprxmtDt"));

        predicate.add(builder.equal(JrnlIntrptTagJoin.get("refContentType"), ContentType.JRNL_DIARY.key));
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
                    if (yy != 9999) predicate.add(builder.equal(jrnlDayJoin.get(key), yy));
                    continue;
                case "mnth":
                    // 99 = 모든 월
                    final Integer mnth = (Integer) value;
                    if (mnth != 99) predicate.add(builder.equal(jrnlDayJoin.get(key), mnth));
                    continue;
                case "regstrId":
                    predicate.add(builder.equal(JrnlIntrptTagJoin.get("regstrId"), value));     // 등록자 ID 기준으로 조회
            }
        }

        return predicate;
    }
}
