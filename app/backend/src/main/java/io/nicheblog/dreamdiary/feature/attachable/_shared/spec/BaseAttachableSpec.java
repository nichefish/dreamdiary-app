package io.nicheblog.dreamdiary.feature.attachable._shared.spec;

import io.nicheblog.dreamdiary.auth.intrfc.spec.BaseAuditSpec;
import io.nicheblog.dreamdiary.feature.attachable._shared.entity.BaseAttachableEntity;
import io.nicheblog.dreamdiary.feature.attachable._shared.type.ContentType;
import io.nicheblog.dreamdiary.feature.attachable.state.entity.StateEntity;
import io.nicheblog.dreamdiary.feature.attachable.tag.entity.TagContentEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
import java.util.*;

/**
 * BaseAttachableSpec
 * <pre>
 *  (공통/상속) 검색인자 세팅 Specification 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
public interface BaseAttachableSpec<Entity extends BaseAttachableEntity>
        extends BaseAuditSpec<Entity> {

    /**
     * default: 인자별로 구체적인 검색 조건을 설정하여 목록을 반환한다.
     * 
     * @param searchParamMap 검색 조건을 포함하는 매개변수 맵
     * @return {@link Specification} -- 검색 조건에 따른 Specification 객체
     */
    @Override
    default Specification<Entity> searchWith(final Map<String, Object> searchParamMap) {
        final Map<String, Object> effectiveSearchParamMap = new HashMap<>(searchParamMap);
        effectiveSearchParamMap.remove("backToList");
        effectiveSearchParamMap.remove("actvtyCtgr");

        return (root, query, builder) -> {
            List<Predicate> basePredicate = new ArrayList<>();
            List<Predicate> attachablePredicate = new ArrayList<>();
            List<Predicate> predicate = new ArrayList<>();
            try {
                // basePredicte 먼저 처리 후 나머지에 대해 처리
                final Map<String, Object> baseSearchParamMap = new HashMap<>(effectiveSearchParamMap);
                final Map<String, Object> attachableSearchParamMap = new HashMap<>(effectiveSearchParamMap);
                final Map<String, Object> customSearchParamMap = new HashMap<>(effectiveSearchParamMap);
                basePredicate = getBasePredicate(baseSearchParamMap, root, query, builder);
                attachablePredicate = getAttachablePredicate(attachableSearchParamMap, root, query, builder);
                predicate = getPredicateWithParams(customSearchParamMap, root, query, builder);
            } catch (final Exception e) {
                e.printStackTrace();
            }
            predicate.addAll(basePredicate);
            predicate.addAll(attachablePredicate);
            this.postQuery(root, query, builder, effectiveSearchParamMap);
            return builder.and(predicate.toArray(new Predicate[0]));
        };
    }

    /**
     * default: attachable 요소에 대해 인자별로 구체적인 검색 조건을 세팅한다.
     * 
     * @param searchParamMap 검색 조건을 포함하는 매개변수 맵
     * @param root 엔티티의 루트 객체
     * @param query - CriteriaQuery 객체
     * @param builder CriteriaBuilder 객체
     * @return {@link List} -- 생성된 검색 조건의 리스트
     */
    default List<Predicate> getAttachablePredicate(
            final Map<String, Object> searchParamMap,
            final Root<Entity> root,
            final CriteriaQuery<?> query,
            final CriteriaBuilder builder
    ) throws Exception {

        final List<Predicate> predicate = new ArrayList<>();
        final List<String> keysToRemove = new ArrayList<>();

        for (final String key : searchParamMap.keySet()) {
            switch (key) {
                case "tags":
                    resolveTagsPredicate(predicate, root, builder, searchParamMap.get(key), null, List.<String>of());
                    keysToRemove.add(key);
                    continue;
                default:
                    break;
            }
        }
        keysToRemove.forEach(searchParamMap::remove);

        return predicate;
    }

    default void resolveTagIdPredicate(
            final List<Predicate> predicate,
            final Root<Entity> root,
            final CriteriaBuilder builder,
            final Object value,
            final String createdBy,
            final ContentType contentType
    ) {
        resolveTagIdPredicate(predicate, root, builder, value, createdBy, refContentTypeKeysOf(contentType));
    }

    /**
     * 태그 ID 단일 조건. {@code refContentTypeKeys} 가 여러 개면 IN 으로 스코프한다(일기 축 DIARY∪REFLECTION).
     *
     * @param refContentTypeKeys tag_content.ref_content_type 허용 키. null/빈이면 타입 스코프 없음
     */
    default void resolveTagIdPredicate(
            final List<Predicate> predicate,
            final Root<Entity> root,
            final CriteriaBuilder builder,
            final Object value,
            final String createdBy,
            final Collection<String> refContentTypeKeys
    ) {
        final Join<?, TagContentEntity> tagContentJoin = root.join("tag", JoinType.INNER).join("list", JoinType.INNER);
        addTagScopePredicate(predicate, tagContentJoin, builder, createdBy, refContentTypeKeys);
        predicate.add(builder.equal(tagContentJoin.get("tagId"), value));
    }

    default void resolveTagsPredicate(
            final List<Predicate> predicate,
            final Root<Entity> root,
            final CriteriaBuilder builder,
            final Object value,
            final String createdBy,
            final ContentType contentType
    ) {
        resolveTagsPredicate(predicate, root, builder, value, createdBy, refContentTypeKeysOf(contentType));
    }

    default void resolveTagsPredicate(
            final List<Predicate> predicate,
            final Root<Entity> root,
            final CriteriaBuilder builder,
            final Object value,
            final String createdBy,
            final Collection<String> refContentTypeKeys
    ) {
        if (!(value instanceof List<?> tagIdList) || CollectionUtils.isEmpty(tagIdList)) return;

        final Join<?, TagContentEntity> tagContentJoin = root.join("tag", JoinType.INNER).join("list", JoinType.INNER);
        addTagScopePredicate(predicate, tagContentJoin, builder, createdBy, refContentTypeKeys);
        predicate.add(tagContentJoin.get("tagId").in(tagIdList));
    }

    default void resolveTagIdsPredicate(
            final List<Predicate> predicate,
            final Root<Entity> root,
            final CriteriaQuery<?> query,
            final CriteriaBuilder builder,
            final Object value,
            final String createdBy,
            final ContentType contentType
    ) {
        resolveTagIdsPredicate(predicate, root, query, builder, value, createdBy, refContentTypeKeysOf(contentType));
    }

    /**
     * 멀티 태그 AND. {@code refContentTypeKeys} 로 tag_content 타입 스코프를 연다.
     *
     * @param refContentTypeKeys tag_content.ref_content_type 허용 키. null/빈이면 타입 스코프 없음
     */
    default void resolveTagIdsPredicate(
            final List<Predicate> predicate,
            final Root<Entity> root,
            final CriteriaQuery<?> query,
            final CriteriaBuilder builder,
            final Object value,
            final String createdBy,
            final Collection<String> refContentTypeKeys
    ) {
        if (!(value instanceof List<?> rawTagList) || CollectionUtils.isEmpty(rawTagList)) return;

        final List<Integer> tagIds = rawTagList.stream()
                .filter(Objects::nonNull)
                .map(o -> (Integer) o)
                .distinct()
                .toList();
        if (tagIds.isEmpty()) return;

        final Subquery<Long> tagSubquery = query.subquery(Long.class);
        final Root<TagContentEntity> tagRoot = tagSubquery.from(TagContentEntity.class);
        final List<Predicate> subPredicates = new ArrayList<>();
        subPredicates.add(builder.equal(tagRoot.get("refId"), root.get("id")));
        addRefContentTypeInPredicate(subPredicates, builder, tagRoot.get("refContentType"), refContentTypeKeys);
        if (StringUtils.isNotBlank(createdBy)) {
            subPredicates.add(builder.equal(tagRoot.get("createdBy"), createdBy));
        }
        subPredicates.add(tagRoot.get("tagId").in(tagIds));

        tagSubquery.select(tagRoot.get("refId"));
        tagSubquery.where(builder.and(subPredicates.toArray(new Predicate[0])));
        tagSubquery.groupBy(tagRoot.get("refId"));
        tagSubquery.having(builder.equal(builder.countDistinct(tagRoot.get("tagId")), (long) tagIds.size()));
        predicate.add(builder.exists(tagSubquery));
    }

    default void resolveStatesPredicate(
            final List<Predicate> predicate,
            final Root<Entity> root,
            final CriteriaQuery<?> query,
            final CriteriaBuilder builder,
            final Object value,
            final String createdBy,
            final ContentType contentType
    ) {
        resolveStatesPredicate(predicate, root, query, builder, value, createdBy, refContentTypeKeysOf(contentType));
    }

    /**
     * 상태 키 AND/IN. {@code refContentTypeKeys} 로 state 타입 스코프를 연다.
     *
     * @param refContentTypeKeys state.ref_content_type 허용 키. null/빈이면 타입 스코프 없음
     */
    default void resolveStatesPredicate(
            final List<Predicate> predicate,
            final Root<Entity> root,
            final CriteriaQuery<?> query,
            final CriteriaBuilder builder,
            final Object value,
            final String createdBy,
            final Collection<String> refContentTypeKeys
    ) {
        if (!(value instanceof List<?> rawStateList) || CollectionUtils.isEmpty(rawStateList)) return;

        final List<String> states = rawStateList.stream()
                .filter(Objects::nonNull)
                .map(o -> o.toString().trim())
                .filter(StringUtils::isNotEmpty)
                .toList();
        if (states.isEmpty()) return;

        final Subquery<Long> stateSubquery = query.subquery(Long.class);
        final Root<StateEntity> stateRoot = stateSubquery.from(StateEntity.class);
        final List<Predicate> subPredicates = new ArrayList<>();
        subPredicates.add(builder.equal(stateRoot.get("refId"), root.get("id")));
        addRefContentTypeInPredicate(subPredicates, builder, stateRoot.get("refContentType"), refContentTypeKeys);
        subPredicates.add(stateRoot.get("stateKey").in(states));

        stateSubquery.select(stateRoot.get("refId"));
        stateSubquery.where(builder.and(subPredicates.toArray(new Predicate[0])));

        if (StringUtils.isNotBlank(createdBy)) {
            predicate.add(builder.equal(root.get("createdBy"), createdBy));
        }
        predicate.add(builder.exists(stateSubquery));
    }

    private void addTagScopePredicate(
            final List<Predicate> predicate,
            final Join<?, TagContentEntity> tagContentJoin,
            final CriteriaBuilder builder,
            final String createdBy,
            final Collection<String> refContentTypeKeys
    ) {
        if (StringUtils.isNotBlank(createdBy)) {
            predicate.add(builder.equal(tagContentJoin.get("createdBy"), createdBy));
        }
        addRefContentTypeInPredicate(predicate, builder, tagContentJoin.get("refContentType"), refContentTypeKeys);
    }

    /**
     * ref_content_type equal 또는 IN. 키가 하나면 equal, 여러 개면 in.
     */
    private void addRefContentTypeInPredicate(
            final List<Predicate> predicate,
            final CriteriaBuilder builder,
            final Path<String> refContentTypePath,
            final Collection<String> refContentTypeKeys
    ) {
        if (CollectionUtils.isEmpty(refContentTypeKeys)) return;
        if (refContentTypeKeys.size() == 1) {
            predicate.add(builder.equal(refContentTypePath, refContentTypeKeys.iterator().next()));
            return;
        }
        predicate.add(refContentTypePath.in(refContentTypeKeys));
    }

    private Collection<String> refContentTypeKeysOf(final ContentType contentType) {
        return contentType == null ? null : List.of(contentType.key);
    }
}
