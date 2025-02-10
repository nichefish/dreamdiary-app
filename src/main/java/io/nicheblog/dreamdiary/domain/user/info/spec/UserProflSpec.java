package io.nicheblog.dreamdiary.domain.user.info.spec;

import io.nicheblog.dreamdiary.domain.user.profl.entity.UserProflEntity;
import io.nicheblog.dreamdiary.extension.cd.entity.DtlCdEntity;
import io.nicheblog.dreamdiary.global.intrfc.spec.BaseSpec;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * UserProflSpec
 * <pre>
 *  사용자 프로필 목록 검색인자 세팅 Specification.
 * </pre>
 *
 * @author nichefish
 */
@Component
@Log4j2
public class UserProflSpec
        implements BaseSpec<UserProflEntity> {

    /**
     * 검색 모드별로 preset된 특정 검색 조건 목록을 반환한다.
     *
     * @param searchMode 검색 모드
     * @param yyStr 년도
     * @return {@link Predicate} -- 설정된 검색 조건(Predicate) 리스트
     */
    public Specification<UserProflEntity> searchWith(final String searchMode, final String yyStr) {
        return (root, query, builder) -> {
            List<Predicate> predicate = new ArrayList<>();
            try {
                predicate = getCrdtUser(searchMode, yyStr, root, builder);
                final List<Order> order = getOrderByTitleAndEcnyDt(root, builder);
                query.orderBy(order);
            } catch (final Exception e) {
                e.printStackTrace();
            }

            return builder.and(predicate.toArray(new Predicate[0]));
        };
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
    public List<Predicate> getPredicateWithParams(
            final Map<String, Object> searchParamMap,
            final Root<UserProflEntity> root,
            final CriteriaBuilder builder
    ) {

        final List<Predicate> predicate = new ArrayList<>();

        // 파라미터 비교
        for (String key : searchParamMap.keySet()) {
            switch (key) {
                // 기타 조건 처리
                default:
                    // default :: 조건 파라미터에 대해 equal 검색
                    try {
                        predicate.add(builder.equal(root.get(key), searchParamMap.get(key)));
                    } catch (final Exception e) {
                        log.info("unable to locate attribute '{}' while trying root.get(key).", key);
                        // e.printStackTrace();
                    }
            }
        }

        return predicate;
    }

    /**
     * preset된 특정 검색 조건 목록을 반환한다. (내부직원 검색조건 세팅)
     *
     * @param searchMode 검색 모드
     * @param yyStr 년도
     * @param root 검색할 엔티티의 Root 객체
     * @param builder 검색 조건을 생성하는 CriteriaBuilder 객체
     * @return {@link Predicate} -- 설정된 검색 조건(Predicate) 리스트
     * @throws Exception 검색 조건 생성 중 발생할 수 있는 예외
     */
    public List<Predicate> getCrdtUser(
            final String searchMode,
            final String yyStr,
            final Root<UserProflEntity> root,
            final CriteriaBuilder builder
    ) throws Exception {
        final List<Predicate> predicate = new ArrayList<>();
        if (StringUtils.isEmpty(searchMode)) return predicate;

        if ("crdtUser".equals(searchMode)) {
            final Expression<String> cmpyCdExp = root.get("cmpyCd");
            final Expression<Date> retireDtExp = root.get("retireDt");
            // 2. 기간조건 :: 해당 년도 내에 근무내역이 있음 (입사일은 당연히 있다고 간주, 퇴사일만 체크)
            final Date firstDay = DateUtils.Parser.bfDateParse(DateUtils.asDate(yyStr + "0101"));
            final Predicate notRetired = builder.isNull(retireDtExp);
            final Predicate retiredAfterFirstDay = builder.greaterThanOrEqualTo(retireDtExp, firstDay);
            predicate.add(builder.or(notRetired, retiredAfterFirstDay));
            return predicate;
        }

        return predicate;
    }

    /**
     * preset된 특정 정렬 조건 목록을 반환한다.
     *
     * @param root {@link Root} 엔티티 객체, 검색할 사용자 엔티티의 루트
     * @param builder {@link CriteriaBuilder} 객체, 정렬 조건을 생성하는 빌더
     * @return {@link Order} 정렬 조건 목록
     */
    private static List<Order> getOrderByTitleAndEcnyDt(
            final Root<UserProflEntity> root,
            final CriteriaBuilder builder
    ) {
        final List<Order> order = new ArrayList<>();
        final Join<UserProflEntity, DtlCdEntity> rankCdJoin = root.join("rankCdInfo", JoinType.LEFT);
        order.add(builder.desc(rankCdJoin.get("state").get("sortOrdr")));
        order.add(builder.asc(root.get("ecnyDt")));

        return order;
    }
}
