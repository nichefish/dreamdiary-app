package io.nicheblog.dreamdiary.domain.schdul.spec;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.domain.schdul.entity.SchdulEntity;
import io.nicheblog.dreamdiary.domain.schdul.entity.SchdulPrtcpntEntity;
import io.nicheblog.dreamdiary.global.Constant;
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
 * SchdulSpec
 * <pre>
 *  일정 목록 검색인자 세팅 Specification.
 * </pre>
 *
 * @author nichefish
 */
@Component
@Log4j2
public class SchdulSpec
        implements BasePostSpec<SchdulEntity> {

    /**
     * 검색 조건 세팅 후 쿼리 후처리. (override)
     * 
     * @param root 조회할 엔티티의 Root 객체
     * @param query - CriteriaQuery 객체
     * @param builder CriteriaBuilder 객체
     */
    @Override
    public void postQuery(
            final Root<SchdulEntity> root,
            final CriteriaQuery<?> query,
            final CriteriaBuilder builder
    ) {
        // 정렬 순서 변경
        final List<Order> order = new ArrayList<>();
        order.add(builder.desc(root.get("bgnDt")));
        query.orderBy(order);
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
            final Root<SchdulEntity> root,
            final CriteriaBuilder builder
    ) throws Exception {

        final List<Predicate> predicate = new ArrayList<>();
        Join<SchdulEntity, SchdulPrtcpntEntity> prtcpntJoin;
        // expressions
        final Expression<Date> endDtExp = root.get("endDt");
        final Expression<Date> bgnDtExp = root.get("bgnDt");
        final Expression<String> prvtYnExp = root.get("prvtYn");
        final Expression<String> schdulCdExp = root.get("schdulCd");

        // 파라미터 비교
        for (String key : searchParamMap.keySet()) {
            switch (key) {
                case "searchStartDt":
                    // 기간 검색
                    predicate.add(builder.greaterThanOrEqualTo(endDtExp, DateUtils.asDate(searchParamMap.get(key))));
                    continue;
                case "searchEndDt":
                    // 기간 검색
                    predicate.add(builder.lessThanOrEqualTo(bgnDtExp, DateUtils.asDate(searchParamMap.get(key))));
                    continue;
                case "getHldyCeremonyOnly":
                    // 휴일/공휴일, 행사 조회
                    predicate.add(builder.equal(prvtYnExp, "N"));
                    Predicate hldy = builder.equal(schdulCdExp, Constant.SCHDUL_HLDY);
                    Predicate ceremony = builder.equal(schdulCdExp, Constant.SCHDUL_CEREMONY);
                    predicate.add(builder.or(hldy, ceremony));
                    continue;
                case "getExceptHldyCeremony":
                    // 휴일/공휴일, 행사 제외하고 조회
                    predicate.add(builder.equal(prvtYnExp, "N"));
                    Predicate notHldy = builder.notEqual(schdulCdExp, Constant.SCHDUL_HLDY);
                    Predicate notCeremony = builder.notEqual(schdulCdExp, Constant.SCHDUL_CEREMONY);
                    predicate.add(builder.and(notHldy, notCeremony));
                    continue;
                case "getPrvtOnly":
                    // 개인 일정 조회
                    predicate.add(builder.equal(prvtYnExp, "Y"));
                    prtcpntJoin = root.join("prtcpntList", JoinType.INNER);
                    predicate.add(builder.equal(prtcpntJoin.get("userId"), AuthUtils.getLgnUserId()));
                    continue;
                case "indtChked":
                    // 내근 조회
                    if ("N".equals(searchParamMap.get(key))) {
                        predicate.add(builder.notEqual(schdulCdExp, Constant.SCHDUL_INDT));
                    }
                    continue;
                case "outdtChked":
                    // 외근 조회
                    if ("N".equals(searchParamMap.get(key))) {
                        predicate.add(builder.notEqual(schdulCdExp, Constant.SCHDUL_OUTDT));
                    }
                    continue;
                case "tlcmmtChked":
                    // 재택근무 조회
                    if ("N".equals(searchParamMap.get(key))) {
                        predicate.add(builder.notEqual(schdulCdExp, Constant.SCHDUL_TLCMMT));
                    }
                    continue;
                // case "myPaprChked":
                //     // 내가 속한 일정 조회
                //     if ("Y".equals(searchParamMap.get(key))) {
                //         prtcpntJoin = root.join("prtcpntList", JoinType.INNER);
                //         predicate.add(builder.equal(prtcpntJoin.get("userId"), AuthUtils.getLgnUserId()));
                //     }
                //     continue;
                case "searchKeyword":
                    // 입력 키워드 검색
                    final String keyword = (String) searchParamMap.get(key);
                    final Predicate schdulNm = builder.like(root.get("title"), "%" + keyword + "%");
                    final Predicate schdulResn = builder.like(root.get("cn"), "%" + keyword + "%");
                    predicate.add(builder.or(schdulNm, schdulResn));
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
