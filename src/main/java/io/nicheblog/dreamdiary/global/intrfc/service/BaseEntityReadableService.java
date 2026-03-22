package io.nicheblog.dreamdiary.global.intrfc.service;

import io.nicheblog.dreamdiary.global.intrfc.entity.BaseCrudEntity;
import io.nicheblog.dreamdiary.global.intrfc.model.param.BaseSearchParam;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import io.nicheblog.dreamdiary.global.intrfc.spec.BaseSpec;
import io.nicheblog.dreamdiary.global.util.cmm.CmmUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * BaseEntityReadableService
 * <pre>
 *  (공통/상속) 읽기 (entity level) 서비스 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
public interface BaseEntityReadableService<Key extends Serializable, Entity extends BaseCrudEntity> {

    // Resource : repository
    BaseStreamRepository<Entity, Key> getRepository();
    // Resource : spec
    BaseSpec<Entity> getSpec();

    /**
     * default: 항목 페이징 목록 조회 (entity level)
     *
     * @param searchParam 검색 조건 파라미터 객체
     * @param pageable 페이징 정보
     * @return {@link Page} -- 페이징 처리된 목록 (entity level)
     */
    default Page<Entity> getPageEntity(final BaseSearchParam searchParam, final Pageable pageable) throws Exception {
        final Map<String, Object> searchParamMap = CmmUtils.convertToMap(searchParam);

        return this.getPageEntity(searchParamMap, pageable);
    }

    /**
     * default: 항목 페이징 목록 조회 (entity level)
     *
     * @param searchParamMap 검색 조건 파라미터 맵
     * @param pageable 페이징 정보
     * @return {@link Page} -- 페이징 처리된 목록 (entity level)
     */
    default Page<Entity> getPageEntity(final Map<String, Object> searchParamMap, final Pageable pageable) throws Exception {
        return getRepository().findAll(getSpec().searchWith(searchParamMap), pageable);
    }

    /* ----- */

    /**
     * default: 항목 목록 조회 (entity level)
     *
     * @param searchParam 검색 조건 파라미터
     * @return {@link List} -- 목록 (entity level)
     */
    default List<Entity> getListEntity(final BaseSearchParam searchParam) throws Exception {
        final Map<String, Object> searchParamMap = CmmUtils.convertToMap(searchParam);
        final Map<String, Object> filteredSearchKey = CmmUtils.filterParamMap(searchParamMap);

        return this.getListEntity(filteredSearchKey);
    }

    /**
     * default: 항목 목록 조회 (entity level)
     *
     * @param searchParamMap 검색 조건 파라미터 맵
     * @return {@link List} -- 목록 (entity level)
     */
    default List<Entity> getListEntity(final Map<String, Object> searchParamMap) throws Exception {
        return getRepository().findAll(getSpec().searchWith(searchParamMap));
    }

    /**
     * default: 항목 목록 조회 (+정렬) (entity level)
     *
     * @param searchParam 검색 조건 파라미터
     * @param sort 정렬
     * @return {@link List} -- 목록 (entity level)
     */
    default List<Entity> getListEntity(final BaseSearchParam searchParam, Sort sort) throws Exception {
        final Map<String, Object> searchParamMap = CmmUtils.convertToMap(searchParam);

        return this.getListEntity(searchParamMap, sort);
    }

    /**
     * default: 항목 목록 조회 (+정렬) (entity level)
     *
     * @param searchParamMap 검색 조건 파라미터 맵
     * @param sort 정렬
     * @return {@link List} -- 목록 (entity level)
     */
    default List<Entity> getListEntity(final Map<String, Object> searchParamMap, Sort sort) throws Exception {
        return getRepository().findAll(getSpec().searchWith(searchParamMap), sort);
    }

    /* ----- */

    /**
     * default: Stream<Entity> 조회
     *
     * @param searchParam 검색 조건 파라미터
     * @return {@link Stream} -- 목록 (entity level)
     */
    @Transactional(readOnly = true)
    default Stream<Entity> getStreamEntity(final BaseSearchParam searchParam) throws Exception {
        final Map<String, Object> searchParamMap = CmmUtils.convertToMap(searchParam);

        return this.getStreamEntity(searchParamMap);
    }

    /**
     * default: Stream<Entity> 조회
     *
     * @param searchParamMap 검색 조건 파라미터 맵
     * @return {@link Stream} -- 목록 (entity level)
     */
    @Transactional(readOnly = true)
    default Stream<Entity> getStreamEntity(final Map<String, Object> searchParamMap) throws Exception {
        final Map<String, Object> filteredSearchKey = CmmUtils.filterParamMap(searchParamMap);

        return getRepository().streamAllBy(getSpec().searchWith(filteredSearchKey));
    }

    /**
     * default: Stream<Entity> (+정렬) 조회
     *
     * @param searchParam 검색 조건 파라미터
     * @param sort 정렬
     * @return {@link Stream} -- 목록 (entity level)
     */
    @Transactional(readOnly = true)
    default Stream<Entity> getStreamEntity(final BaseSearchParam searchParam, Sort sort) throws Exception {
        final Map<String, Object> searchParamMap = CmmUtils.convertToMap(searchParam);

        return this.getStreamEntity(searchParamMap, sort);
    }

    /**
     * default: Stream<Entity> 조회
     *
     * @param searchParamMap 검색 조건 파라미터 맵
     * @param sort 정렬
     * @return {@link Stream} -- 목록 (entity level)
     */
    @Transactional(readOnly = true)
    default Stream<Entity> getStreamEntity(final Map<String, Object> searchParamMap, Sort sort) throws Exception {
        final Map<String, Object> filteredSearchKey = CmmUtils.filterParamMap(searchParamMap);

        return getRepository().streamAllBy(getSpec().searchWith(filteredSearchKey), sort);
    }

    /* ----- */

    /**
     * default: 단일 항목 조회 (entity level)
     *
     * @param key 조회할 엔티티의 키
     * @return {@link Entity} -- 조회된 객체
     */
    default Entity getDtlEntity(final Key key) throws Exception {
        return getRepository().findById(key).orElseThrow(() -> new EntityNotFoundException("해당 정보가 존재하지 않습니다."));
    }

    /**
     * default: 단일 항목 조회 (entity level) = 부재시 null
     *
     * @param key 조회할 엔티티의 키
     * @return {@link Entity} -- 조회된 객체
     */
    default Entity findDtlEntity(final Key key) throws Exception {
        return getRepository().findById(key).orElse(null);
    }
}
