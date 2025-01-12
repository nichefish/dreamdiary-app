package io.nicheblog.dreamdiary.domain.vcatn.papr.spec;

import io.nicheblog.dreamdiary.domain.vcatn.papr.entity.VcatnPaprEntity;
import io.nicheblog.dreamdiary.domain.vcatn.papr.entity.VcatnSchdulEntity;
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
 * VcatnSchdulSpec
 * <pre>
 *  휴가 일정 검색인자 세팅 Specification.
 * </pre>
 *
 * @author nichefish
 */
@Component
@Log4j2
public class VcatnSchdulSpec
        implements BaseSpec<VcatnSchdulEntity> {

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
            final Root<VcatnSchdulEntity> root,
            final CriteriaBuilder builder
    ) throws Exception {
        final List<Predicate> predicate = new ArrayList<>();
        final Join<VcatnSchdulEntity, VcatnPaprEntity> vcatnPaprJoin = root.join("vcatnPapr", JoinType.LEFT);

        // expressions
        final Expression<Date> endDtExp = root.get("endDt");
        final Expression<Date> bgnDtExp = root.get("bgnDt");

        // 파라미터 비교
        for (String key : searchParamMap.keySet()) {
            switch (key) {
                // 휴가계획서 등록자 혹은 수동등록대상자
                case "userId":
                    final Predicate papr = builder.equal(vcatnPaprJoin.get("regstrId"), searchParamMap.get(key));
                    final Predicate manual = builder.equal(root.get("userId"), searchParamMap.get(key));
                    predicate.add(builder.or(papr, manual));
                    continue;
                    // 휴가시작일이 검색종료일보다 이전이(거나 같)고. 휴가종료일이 검색시작일보다 이후(거나 같)인 것
                case "searchStartDt":
                    predicate.add(builder.greaterThanOrEqualTo(endDtExp, DateUtils.asDate(searchParamMap.get(key))));
                    continue;
                case "searchEndDt":
                    predicate.add(builder.lessThanOrEqualTo(bgnDtExp, DateUtils.asDate(searchParamMap.get(key))));
                    continue;
                    // 기타 조건 처리
                default:
                    // default :: 조건 파라미터에 대해 equal 검색
                    try {
                        predicate.add(builder.equal(root.get(key), searchParamMap.get(key)));
                    } catch (Exception e) {
                        log.info("unable to locate attribute '{}' while trying root.get(key).", key);
                    }
            }
        }

        return predicate;
    }
}
