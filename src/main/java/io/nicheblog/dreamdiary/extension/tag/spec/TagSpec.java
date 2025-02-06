package io.nicheblog.dreamdiary.extension.tag.spec;

import io.nicheblog.dreamdiary.auth.util.AuthUtils;
import io.nicheblog.dreamdiary.extension.tag.entity.ContentTagEntity;
import io.nicheblog.dreamdiary.extension.tag.entity.TagEntity;
import io.nicheblog.dreamdiary.global.intrfc.spec.BaseSpec;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TagSpec
 * <pre>
 *  태그 목록 검색인자 세팅 Specification.
 * </pre>
 *
 * @author nichefish
 */
@Component
@Log4j2
public class TagSpec
        implements BaseSpec<TagEntity> {

    /**
     * 검색 조건 세팅 후 쿼리 후처리. (override)
     *
     * @param root 조회할 엔티티의 Root 객체
     * @param query - CriteriaQuery 객체
     * @param builder CriteriaBuilder 객체
     */
    @Override
    public void postQuery(
            final Root<TagEntity> root,
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
            final Root<TagEntity> root,
            final CriteriaBuilder builder
    ) {

        final List<Predicate> predicate = new ArrayList<>();

        final Join<TagEntity, ContentTagEntity> contentTagJoin = root.join("contentTagList", JoinType.INNER);
        predicate.add(builder.equal(contentTagJoin.get("regstrId"), AuthUtils.getLgnUserIdOrDefault()));     // 등록자 ID 기준으로 조회

        // 파라미터 비교
        for (final String key : searchParamMap.keySet()) {
            switch (key) {
                case "contentType":
                    predicate.add(builder.equal(contentTagJoin.get("refContentType"), searchParamMap.get(key)));
                default:
                    // default :: 조건 파라미터에 대해 equal 검색
                    try {
                        predicate.add(builder.equal(root.get(key), searchParamMap.get(key)));
                    } catch (final Exception e) {
                        log.info("unable to locate attribute " + key + " while trying root.get(key).");
                    }
            }
        }
        return predicate;
    }
    
    /**
     * preset된 특정 검색 조건 목록을 반환한다. (컨텐츠-태그와 연관관계 없는 태그 조회)
     *
     * @return {@link Specification} -- 검색 조건에 따른 Specification 객체
     */
    public Specification<TagEntity> getNoRefTags() {
        return (root, query, builder) -> {
            List<Predicate> predicate = new ArrayList<>();

            final Join<TagEntity, ContentTagEntity> contentTagJoin = root.join("contentTagList", JoinType.LEFT);
            predicate.add(builder.equal(contentTagJoin.get("regstrId"), AuthUtils.getLgnUserIdOrDefault()));     // 등록자 ID 기준으로 조회
            predicate.add(builder.isNull(contentTagJoin));

            return builder.and(predicate.toArray(new Predicate[0]));
        };
    }
    
    /** 
     * 컨텐츠 타입에 맞는 태그 목록 조회 조건을 반환한다.
     *
     * @param contentType - 조회할 컨텐츠 타입
     * @return {@link Specification} -- 검색 조건에 따른 Specification 객체
     */
    public Specification<TagEntity> getContentSpecificTag(final String contentType) {
        final Map<String, Object> searchParamMap = new HashMap<>(){{
            put("contentType", contentType);
        }};
        return this.searchWith(searchParamMap);
    }
}
