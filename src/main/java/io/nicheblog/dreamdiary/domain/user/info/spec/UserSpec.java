package io.nicheblog.dreamdiary.domain.user.info.spec;

import io.nicheblog.dreamdiary.domain.user.emplym.entity.UserEmplymEntity;
import io.nicheblog.dreamdiary.domain.user.info.entity.UserEntity;
import io.nicheblog.dreamdiary.domain.user.profl.entity.UserProflEntity;
import io.nicheblog.dreamdiary.extension.cd.entity.DtlCdEntity;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global.intrfc.spec.BaseCrudSpec;
import io.nicheblog.dreamdiary.global.util.date.DatePtn;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * UserSpec
 * <pre>
 *  사용자(계정) 정보 목록 검색인자 세팅 Specification
 * </pre>
 *
 * @author nichefish
 */
@Component
@Log4j2
public class UserSpec
        implements BaseCrudSpec<UserEntity> {

    /**
     * 인자별로 preset된 특정 검색 조건 목록을 반환한다. (override)
     *
     * @param searchParamMap 검색 파라미터 맵
     * @return {@link Specification} -- 검색 조건에 따른 Specification 객체
     */
    @Override
    public Specification<UserEntity> searchWith(final Map<String, Object> searchParamMap) {
        // filter
        searchParamMap.remove("backToList");
        searchParamMap.remove("actvtyCtgr");

        return (root, query, builder) -> {
            List<Predicate> basePredicate = new ArrayList<>();
            List<Predicate> predicate = new ArrayList<>();
            try {
                // basePredicte 먼저 처리 후 나머지에 대해 처리
                basePredicate = getBasePredicate(searchParamMap, root, builder);
                predicate = getPredicateWithParams(searchParamMap, root, builder);
            } catch (final Exception e) {
                e.printStackTrace();
            }
            predicate.addAll(basePredicate);
            // "시스템관리자"를 조회 목록에서 제외한다.
            predicate.add(builder.notEqual(root.get("userId"), Constant.SYSTEM_ACNT));
            this.postQuery(root, query, builder);

            return builder.and(predicate.toArray(new Predicate[0]));
        };
    }

    /**
     * 인자별로 preset된 특정 검색 조건 목록을 반환한다.
     *
     * @param startDtStr 검색할 시작 날짜 (문자열 형식)
     * @param endDtStr 검색할 종료 날짜 (문자열 형식)
     * @return {@link Specification} -- 검색 조건에 따른 Specification 객체
     */
    public Specification<UserEntity> searchCrdtUser(final String startDtStr, final String endDtStr) {
        return (root, query, builder) -> {
            List<Predicate> predicate = new ArrayList<>();
            try {
                predicate = getCrdtUser(startDtStr, endDtStr, root, builder);
                final List<Order> order = getOrderByTitleAndEcnyDt(root, builder);
                query.orderBy(order);
            } catch (final Exception e) {
                e.printStackTrace();
            }

            return builder.and(predicate.toArray(new Predicate[0]));
        };
    }

    /**
     * preset된 특정 검색 조건 목록을 반환한다.
     *
     * @return {@link Specification} -- 검색 조건에 따른 Specification 객체
     */
    public Specification<UserEntity> searchCrdtBrthdyUser() {
        return (root, query, builder) -> {
            List<Predicate> predicate = new ArrayList<>();
            try {
                final String startDtStr = DateUtils.getCurrDateStr(DatePtn.DATE);
                final String endDtStr = DateUtils.getNextDateStr(DatePtn.DATE);
                predicate = getCrdtBrthdyUser(startDtStr, endDtStr, root, builder);
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
    @Override
    public List<Predicate> getPredicateWithParams(
            final Map<String, Object> searchParamMap,
            final Root<UserEntity> root,
            final CriteriaBuilder builder
    ) {

        final List<Predicate> predicate = new ArrayList<>();
        final Join<UserEntity, UserProflEntity> proflJoin = root.join("profl", JoinType.LEFT);

        // 파라미터 비교
        for (final String key : searchParamMap.keySet()) {
            final Object value = searchParamMap.get(key);
            switch (key) {
                // 사용자 정보 존재 "hasUserProfl" 체크시 조인결과 있는 목록만 반환
                case "hasUserProfl":
                    if ((Boolean) value) {
                        predicate.add(builder.isNotNull(proflJoin.get("userProflNo")));
                    }
                    continue;
                    // 이름 = LIKE 검색
                case "cmpyCd":
                case "rankCd":
                case "teamCd":
                    predicate.add(builder.equal(proflJoin.get(key), value));
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

    /**
     * preset된 특정 검색 조건 목록을 반환한다. (내부 직원 검색)
     *
     * @param startDtStr 검색할 시작 날짜 (문자열 형식)
     * @param endDtStr 검색할 종료 날짜 (문자열 형식)
     * @param root 검색할 엔티티의 Root 객체
     * @param builder 검색 조건을 생성하는 CriteriaBuilder 객체
     * @return {@link List} -- 설정된 검색 조건(Predicate) 리스트
     * @throws Exception 검색 조건 생성 중 발생할 수 있는 예외
     */
    public List<Predicate> getCrdtUser(
            final String startDtStr,
            final String endDtStr,
            final Root<UserEntity> root,
            final CriteriaBuilder builder
    ) throws Exception {
        final List<Predicate> predicate = new ArrayList<>();
        // JOIN 조건 세팅
        final Join<UserEntity, UserEmplymEntity> emplymJoin = root.join("emplym", JoinType.INNER);
        // 2. 기간조건 :: 해당 년도 내에 근무내역이 있음 (입사일 // 퇴사일)
        // 퇴사일 :: 퇴사 안했거나 or 비교일 내에 퇴사했거나
        final Date startDay = DateUtils.Parser.bfDateParse(DateUtils.asDate(startDtStr));
        final Predicate notRetired = builder.isNull(emplymJoin.get("retireDt"));
        final Predicate retiredAfterFirstDay = builder.greaterThanOrEqualTo(emplymJoin.get("retireDt"), startDay);
        predicate.add(builder.or(notRetired, retiredAfterFirstDay));
        // 입사일 :: 비교일보다 전에 입사
        final Date endDay = DateUtils.Parser.bfDateParse(DateUtils.asDate(endDtStr));
        final Predicate hasEcnyDt = builder.isNotNull(emplymJoin.get("ecnyDt"));
        final Predicate enteredBeforeEndDay = builder.lessThanOrEqualTo(emplymJoin.get("ecnyDt"), endDay);
        predicate.add(builder.and(hasEcnyDt, enteredBeforeEndDay));

        return predicate;
    }

    /**
     * preset된 특정 검색 조건 목록을 반환한다. (오늘이 생일인 내부 직원 검색)
     *
     * @param startDtStr 검색할 시작 날짜 (문자열 형식)
     * @param endDtStr 검색할 종료 날짜 (문자열 형식)
     * @param root 검색할 엔티티의 Root 객체
     * @param builder 검색 조건을 생성하는 CriteriaBuilder 객체
     * @return {@link Predicate} -- 설정된 검색 조건(Predicate) 리스트
     * @throws Exception 검색 조건 생성 중 발생할 수 있는 예외
     */
    public List<Predicate> getCrdtBrthdyUser(
            final String startDtStr,
            final String endDtStr,
            final Root<UserEntity> root,
            final CriteriaBuilder builder
    ) throws Exception {
        final List<Predicate> predicate = getCrdtUser(startDtStr, endDtStr, root, builder);
        // JOIN 조건 세팅
        final Join<UserEntity, UserEmplymEntity> emplymJoin = root.join("emplym", JoinType.INNER);
        final Join<UserEntity, UserProflEntity> proflJoin = root.join("profl", JoinType.INNER);
        predicate.add(builder.equal(proflJoin.get("brthdy"), DateUtils.asDate(startDtStr)));

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
            final Root<UserEntity> root,
            final CriteriaBuilder builder
    ) {
        final List<Order> order = new ArrayList<>();
        final Join<UserEntity, UserEmplymEntity> emplymJoin = root.join("emplym", JoinType.INNER);
        final Join<UserEmplymEntity, DtlCdEntity> rankCdJoin = emplymJoin.join("rankCdInfo", JoinType.INNER);
        order.add(builder.desc(rankCdJoin.get("state").get("sortOrdr")));
        order.add(builder.asc(emplymJoin.get("ecnyDt")));

        return order;
    }
}
