package io.nicheblog.dreamdiary.global.intrfc.spec.embed;

import io.nicheblog.dreamdiary.extension.clsf.state.entity.embed.StateEmbedModule;
import io.nicheblog.dreamdiary.global.intrfc.spec.BaseSpec;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * BaseStateSpec
 * <pre>
 *  (공통/상속) 검색인자 세팅 Specification 인터페이스.
 *  StateEmbed 구현시 처리 로직을 포함한다.
 * </pre>
 *
 * @author nichefish
 */
public interface BaseStateSpec<Entity extends StateEmbedModule>
        extends BaseSpec<Entity> {

    /**
     * default: 인자별로 구체적인 검색 조건을 세팅한다. (override)
     * 
     * @param searchParamMap 검색 파라미터 맵
     * @param root 검색할 엔티티의 Root 객체
     * @param builder 검색 조건을 생성하는 CriteriaBuilder 객체
     * @return {@link List} -- 설정된 검색 조건(Predicate) 리스트
     * @throws Exception 검색 조건 생성 중 발생할 수 있는 예외
     */
    @Override
    default List<Predicate> getPredicateWithParams(
            final Map<String, Object> searchParamMap,
            final Root<Entity> root,
            final CriteriaBuilder builder
    ) throws Exception {

        List<Predicate> predicate = new ArrayList<>();
        for (String key : searchParamMap.keySet()) {
            switch (key) {
                case "useYn":
                case "sortOrdr":
                    predicate.add(builder.equal(root.get("state").get(key), searchParamMap.get(key)));
                    continue;
                default:
                // default :: 조건 파라미터에 대해 equal 검색
                    try {
                        predicate.add(builder.equal(root.get(key), searchParamMap.get(key)));
                    } catch (final Exception e) {
                        e.printStackTrace();
                        // log.info("unable to locate attribute '{}' while trying root.get(key).", key);
                    }
            }
        }
        return predicate;
    }
}
