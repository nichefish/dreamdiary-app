package io.nicheblog.dreamdiary.feature.attachable.prefix.repository.jpa;

import io.nicheblog.dreamdiary.feature.attachable.prefix.entity.PrefixEntity;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Prefix Scope 소속 말머리 저장소.
 *
 * @author nichefish
 */
@Repository
public interface PrefixRepository extends BaseStreamRepository<PrefixEntity, Integer> {

    List<PrefixEntity> findAllByScopeIdOrderBySortOrderAscIdAsc(Integer scopeId);

    List<PrefixEntity> findAllByScopeIdAndActiveYnOrderBySortOrderAscIdAsc(Integer scopeId, String activeYn);

    boolean existsByScopeIdAndNameIgnoreCase(Integer scopeId, String name);

    boolean existsByScopeIdAndNameIgnoreCaseAndIdNot(Integer scopeId, String name, Integer id);
}
