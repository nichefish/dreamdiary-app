package io.nicheblog.dreamdiary.feature.calendar.schedule.spec;

import io.nicheblog.dreamdiary.auth.security.util.AuthUtils;
import io.nicheblog.dreamdiary.feature.attachable._shared.spec.BaseAttachableSpec;
import io.nicheblog.dreamdiary.feature.calendar.schedule.entity.ScheduleEntity;
import io.nicheblog.dreamdiary.feature.calendar.schedule.entity.SchedulePrtcpntEntity;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import io.nicheblog.dreamdiary.infrastructure.code.Code;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * ScheduleSpec
 * <pre>
 *  일정 목록 검색인자 세팅 Specification.
 * </pre>
 *
 * @author nichefish
 */
@Component
@Log4j2
public class ScheduleSpec
        implements BaseAttachableSpec<ScheduleEntity> {

    /**
     * 검색 조건 세팅 후 쿼리 후처리. (override)
     * 
     * @param root 조회할 엔티티의 Root 객체
     * @param query - CriteriaQuery 객체
     * @param builder CriteriaBuilder 객체
     */
    @Override
    public void postQuery(
            final Root<ScheduleEntity> root,
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
     * @param query - CriteriaQuery 객체
     * @param builder 검색 조건을 생성하는 CriteriaBuilder 객체
     * @return {@link List} -- 설정된 검색 조건(Predicate) 리스트
     */
    @Override
    public List<Predicate> getPredicateWithParams(
            final Map<String, Object> searchParamMap,
            final Root<ScheduleEntity> root,
            final CriteriaQuery<?> query,
            final CriteriaBuilder builder
    ) throws Exception {

        final List<Predicate> predicate = new ArrayList<>();
        Join<ScheduleEntity, SchedulePrtcpntEntity> prtcpntJoin;
        // expressions
        final Expression<LocalDateTime> endDtExp = root.get("endDt");
        final Expression<LocalDateTime> bgnDtExp = root.get("bgnDt");
        final Expression<String> privateYnExp = root.get("privateYn");
        final Expression<String> scheduleCdExp = root.get("scheduleCd");

        // 파라미터 비교
        for (final String key : searchParamMap.keySet()) {
            final Object value = searchParamMap.get(key);
            switch (key) {
                case "searchStartDt":
                    // 기간 검색
                    predicate.add(builder.greaterThanOrEqualTo(endDtExp, DateUtils.asLocalDateTime(value)));
                    continue;
                case "searchEndDt":
                    // 기간 검색
                    predicate.add(builder.lessThanOrEqualTo(bgnDtExp, DateUtils.asLocalDateTime(value)));
                    continue;
                case "getHolydayCeremonyOnly":
                    // 휴일/공휴일, 행사 조회
                    predicate.add(builder.equal(privateYnExp, "N"));
                    Predicate holyday = builder.equal(scheduleCdExp, Code.SCHEDULE_HOLYDAY);
                    Predicate ceremony = builder.equal(scheduleCdExp, Code.SCHEDULE_CEREMONY);
                    predicate.add(builder.or(holyday, ceremony));
                    continue;
                case "getExceptHolydayCeremony":
                    // 휴일/공휴일, 행사 제외하고 조회
                    predicate.add(builder.equal(privateYnExp, "N"));
                    Predicate notHolyday = builder.notEqual(scheduleCdExp, Code.SCHEDULE_HOLYDAY);
                    Predicate notCeremony = builder.notEqual(scheduleCdExp, Code.SCHEDULE_CEREMONY);
                    predicate.add(builder.and(notHolyday, notCeremony));
                    continue;
                case "getPrvtOnly":
                    // 개인 일정 조회
                    predicate.add(builder.equal(privateYnExp, "Y"));
                    prtcpntJoin = root.join("prtcpntList", JoinType.INNER);
                    predicate.add(builder.equal(prtcpntJoin.get("username"), AuthUtils.getLoginUsername()));
                    continue;
                case "indtChked":
                    // 내근 조회
                    if ("N".equals(value)) {
                        predicate.add(builder.notEqual(scheduleCdExp, Code.SCHEDULE_INDT));
                    }
                    continue;
                case "outdtChked":
                    // 외근 조회
                    if ("N".equals(value)) {
                        predicate.add(builder.notEqual(scheduleCdExp, Code.SCHEDULE_OUTDT));
                    }
                    continue;
                case "tlcmmtChked":
                    // 재택근무 조회
                    if ("N".equals(value)) {
                        predicate.add(builder.notEqual(scheduleCdExp,Code.SCHEDULE_TLCMMT));
                    }
                    continue;
                // case "myPaprChked":
                //     // 내가 속한 일정 조회
                //     if ("Y".equals(value)) {
                //         prtcpntJoin = root.join("prtcpntList", JoinType.INNER);
                //         predicate.add(builder.equal(prtcpntJoin.get("username"), AuthUtils.getLoginUsername()));
                //     }
                //     continue;
                case "searchKeyword":
                    // 입력 키워드 검색
                    final String keyword = (String) value;
                    final Predicate scheduleTitle = builder.like(root.get("title"), "%" + keyword + "%");
                    final Predicate scheduleContent = builder.like(root.get("content"), "%" + keyword + "%");
                    predicate.add(builder.or(scheduleTitle, scheduleContent));
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

