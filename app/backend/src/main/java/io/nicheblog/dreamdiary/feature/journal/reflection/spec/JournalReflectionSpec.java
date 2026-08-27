package io.nicheblog.dreamdiary.feature.journal.reflection.spec;

import io.nicheblog.dreamdiary.feature.attachable._shared.spec.BaseAttachableSpec;
import io.nicheblog.dreamdiary.feature.journal.reflection.entity.JournalReflectionEntity;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Reflection(Commentary) Specification.
 *
 * <p>Reflection 쓰기 서비스({@code JournalReflectionService})의 조회 계약을 만족시키는 최소 spec 이다.
 * 대상 역참조 로드는 {@code JournalReflectionRepository.findAllByRefIdInOrderBySortOrderAscIdAsc} 가 담당하며,
 * 이 spec 은 단건 상세·작성자 스코프 조회에 쓰인다.</p>
 */
public class JournalReflectionSpec extends BaseAttachableSpec<JournalReflectionEntity> {

    /**
     * 조회 후 정렬·distinct 를 설정한다. Reflection 은 sortOrder, id 오름차순으로 정렬한다.
     *
     * @param root 조회 루트
     * @param query Criteria 쿼리
     * @param builder Criteria 빌더
     * @param searchParamMap 검색 파라미터
     */
    @Override
    public void postQuery(
            final Root<JournalReflectionEntity> root,
            final CriteriaQuery<?> query,
            final CriteriaBuilder builder,
            final Map<String, Object> searchParamMap
    ) {
        query.orderBy(builder.asc(root.get("sortOrder")), builder.asc(root.get("id")));
        query.distinct(true);
    }

    /**
     * 검색 파라미터를 Predicate 목록으로 변환한다. {@code createdBy}·{@code refId}·{@code refContentType}
     * 만 스코프로 인식하고 그 외 키는 무시한다.
     *
     * @param searchParamMap 검색 파라미터
     * @param root 조회 루트
     * @param query Criteria 쿼리
     * @param builder Criteria 빌더
     * @return Predicate 목록
     */
    @Override
    public List<Predicate> getPredicateWithParams(
            final Map<String, Object> searchParamMap,
            final Root<JournalReflectionEntity> root,
            final CriteriaQuery<?> query,
            final CriteriaBuilder builder
    ) {
        final List<Predicate> predicate = new ArrayList<>();
        if (searchParamMap == null) return predicate;

        final Object createdBy = searchParamMap.get("createdBy");
        if (createdBy != null) predicate.add(builder.equal(root.get("createdBy"), createdBy));
        final Object refId = searchParamMap.get("refId");
        if (refId != null) predicate.add(builder.equal(root.get("refId"), refId));
        final Object refContentType = searchParamMap.get("refContentType");
        if (refContentType != null) predicate.add(builder.equal(root.get("refContentType"), refContentType));

        return predicate;
    }
}
