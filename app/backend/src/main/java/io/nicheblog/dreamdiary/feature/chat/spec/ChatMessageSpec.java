package io.nicheblog.dreamdiary.feature.chat.spec;

import io.nicheblog.dreamdiary.feature.attachable._shared.spec.BaseAttachableSpec;
import io.nicheblog.dreamdiary.feature.chat.entity.ChatMessageEntity;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import javax.persistence.criteria.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ChatMessageSpec
 * <pre>
 *  채팅 메시지 목록 검색 조건을 Criteria Predicate로 변환하는 Specification.
 * </pre>
 *
 * @author nichefish
 */
@Component
@Log4j2
public class ChatMessageSpec
        implements BaseAttachableSpec<ChatMessageEntity> {

    /**
     * 검색 조건 세팅 후 쿼리 후처리. (override)
     *
     * @param root 조회할 엔티티의 Root 객체
     * @param query - CriteriaQuery 객체
     * @param builder CriteriaBuilder 객체
     */
    @Override
    public void postQuery(
            final Root<ChatMessageEntity> root,
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
     * @param query - CriteriaQuery 객체
     * @param builder 검색 조건을 생성하는 CriteriaBuilder 객체
     * @return 설정된 검색 조건(Predicate) 리스트
     * @throws Exception 날짜 변환 등 검색 조건 생성 중 예외가 발생한 경우
     */
    @Override
    public List<Predicate> getPredicateWithParams(
            final Map<String, Object> searchParamMap,
            final Root<ChatMessageEntity> root,
            final CriteriaQuery<?> query,
            final CriteriaBuilder builder
    ) throws Exception {

        final List<Predicate> predicate = new ArrayList<>();

        // expressions
        final Expression<LocalDateTime> createdAtExp = root.get("createdAt");

        // 파라미터 비교
        for (final String key : searchParamMap.keySet()) {
            final Object value = searchParamMap.get(key);
            switch (key) {
                case "searchStartDt":
                    // 기간 검색
                    predicate.add(builder.greaterThanOrEqualTo(createdAtExp, DateUtils.asLocalDateTime(value)));
                    continue;
                case "searchEndDt":
                    // 기간 검색
                    predicate.add(builder.lessThanOrEqualTo(createdAtExp, DateUtils.asLocalDateTime(value)));
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
