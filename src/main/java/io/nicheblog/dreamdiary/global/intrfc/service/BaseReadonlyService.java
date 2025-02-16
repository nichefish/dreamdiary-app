package io.nicheblog.dreamdiary.global.intrfc.service;

import io.nicheblog.dreamdiary.global.intrfc.entity.BaseCrudEntity;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseCrudMapstruct;
import io.nicheblog.dreamdiary.global.intrfc.model.BaseCrudDto;
import io.nicheblog.dreamdiary.global.intrfc.model.Identifiable;
import io.nicheblog.dreamdiary.global.intrfc.model.param.BaseSearchParam;
import io.nicheblog.dreamdiary.global.intrfc.repository.BaseStreamRepository;
import io.nicheblog.dreamdiary.global.intrfc.spec.BaseSpec;
import io.nicheblog.dreamdiary.global.util.cmm.CmmUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * BaseReadonlyService
 * <pre>
 *  (공통/상속) 조회 전용 서비스 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
public interface BaseReadonlyService<Dto extends BaseCrudDto & Identifiable<Key>, ListDto extends BaseCrudDto, Key extends Serializable, Entity extends BaseCrudEntity, Repository extends BaseStreamRepository<Entity, Key>, Spec extends BaseSpec<Entity>, Mapstruct extends BaseCrudMapstruct<Dto, ListDto, Entity>> {

    // Resource : repository
    Repository getRepository();

    // Resource : spec
    Spec getSpec();

    // Resource : mapstruct
    Mapstruct getMapstruct();

    /**
     * default: 항목 페이징 목록 조회 (dto level)
     *
     * @param searchParam 검색 조건 파라미터
     * @param pageable 페이징 정보
     * @return {@link Page} -- 페이징 처리된 목록 (dto level)
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @Transactional(readOnly = true)
    default Page<ListDto> getPageDto(final BaseSearchParam searchParam, final Pageable pageable) throws Exception {
        final Map<String, Object> searchParamMap = CmmUtils.convertToMap(searchParam);

        return this.getPageDto(searchParamMap, pageable);
    }

    /**
     * default: 항목 페이징 목록 조회 (dto level)
     *
     * @param searchParamMap 검색 조건 파라미터 맵
     * @param pageable 페이징 정보
     * @return {@link Page} -- 페이징 처리된 목록 (dto level)
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @Transactional(readOnly = true)
    default Page<ListDto> getPageDto(final Map<String, Object> searchParamMap, final Pageable pageable) throws Exception {
        // searchParamMap에서 빈 값들 및 쓸모없는 값들 정리
        final Map<String, Object> filteredSearchKey = CmmUtils.Param.filterParamMap(searchParamMap);

        return this.pageEntityToDto(this.getPageEntity(filteredSearchKey, pageable));
    }

    /**
     * default: 항목 페이징 목록 조회 (entity level)
     *
     * @param searchParam 검색 조건 파라미터 객체
     * @param pageable 페이징 정보
     * @return {@link Page} -- 페이징 처리된 목록 (entity level)
     * @throws Exception 처리 중 발생할 수 있는 예외
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
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    default Page<Entity> getPageEntity(final Map<String, Object> searchParamMap, final Pageable pageable) throws Exception {
        final Repository repository = this.getRepository();
        final Spec spec = this.getSpec();

        return repository.findAll(spec.searchWith(searchParamMap), pageable);
    }

    /**
     * default: 항목 페이징 목록 Page<Entity> -> Page<Dto> 변환
     *
     * @param entityPage 페이징 처리된 Entity 목록
     * @return {@link Page} -- 변환된 페이징 처리된 Dto 목록
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    default Page<ListDto> pageEntityToDto(final Page<Entity> entityPage) throws Exception {
        final Mapstruct mapstruct = this.getMapstruct();
        final AtomicInteger counter = new AtomicInteger(0);
        final List<ListDto> dtoList = entityPage.stream()
                .map(entity -> {
                    try {
                        final ListDto listDto = mapstruct.toListDto(entity);
                        listDto.setRnum(CmmUtils.getPageRnum(entityPage, counter.getAndIncrement()));
                        return listDto;
                    } catch (final Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());

        return new PageImpl<>(dtoList, entityPage.getPageable(), entityPage.getTotalElements());
    }

    /* ----- */

    /**
     * default: 항목 목록 조회 (dto level)
     *
     * @param searchParam 검색 조건 파라미터
     * @return {@link List} -- 목록 (dto level)
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @Transactional(readOnly = true)
    default List<ListDto> getListDto(final BaseSearchParam searchParam) throws Exception {
        final Map<String, Object> searchParamMap = CmmUtils.convertToMap(searchParam);

        return this.getListDto(searchParamMap);
    }

    /**
     * default: 항목 목록 조회 (dto level)
     *
     * @param searchParamMap 검색 조건 파라미터 맵
     * @return {@link List} -- 목록 (dto level)
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @Transactional(readOnly = true)
    default List<ListDto> getListDto(final Map<String, Object> searchParamMap) throws Exception {
        // searchParamMap에서 빈 값들 및 쓸모없는 값들 정리
        final Map<String, Object> filteredSearchKey = CmmUtils.Param.filterParamMap(searchParamMap);
        final List<Entity> retrievedListEntity = this.getListEntity(filteredSearchKey);

        return this.listEntityToDto(retrievedListEntity);
    }

    /**
     * default: 항목 목록 조회 (+정렬) (dto level)
     *
     * @param searchParam 검색 조건 파라미터
     * @param sort 정렬
     * @return {@link List} -- 목록 (dto level)
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @Transactional(readOnly = true)
    default List<ListDto> getListDto(final BaseSearchParam searchParam, Sort sort) throws Exception {
        final Map<String, Object> searchParamMap = CmmUtils.convertToMap(searchParam);

        return this.getListDto(searchParamMap, sort);
    }

    /**
     * default: 항목 목록 조회 (+정렬) (dto level)
     *
     * @param searchParamMap 검색 조건 파라미터 맵
     * @param sort 정렬
     * @return {@link List} -- 목록 (dto level)
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @Transactional(readOnly = true)
    default List<ListDto> getListDto(final Map<String, Object> searchParamMap, Sort sort) throws Exception {
        // searchParamMap에서 빈 값들 및 쓸모없는 값들 정리
        final Map<String, Object> filteredSearchKey = CmmUtils.Param.filterParamMap(searchParamMap);
        final List<Entity> retrievedListEntity = this.getListEntity(filteredSearchKey, sort);

        return this.listEntityToDto(retrievedListEntity);
    }

    /**
     * default: 항목 목록 조회 (entity level)
     *
     * @param searchParam 검색 조건 파라미터
     * @return {@link List} -- 목록 (entity level)
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    default List<Entity> getListEntity(final BaseSearchParam searchParam) throws Exception {
        final Map<String, Object> searchParamMap = CmmUtils.convertToMap(searchParam);
        final Map<String, Object> filteredSearchKey = CmmUtils.Param.filterParamMap(searchParamMap);

        return this.getListEntity(filteredSearchKey);
    }

    /**
     * default: 항목 목록 조회 (entity level)
     *
     * @param searchParamMap 검색 조건 파라미터 맵
     * @return {@link List} -- 목록 (entity level)
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    default List<Entity> getListEntity(final Map<String, Object> searchParamMap) throws Exception {
        final Repository repository = this.getRepository();
        final Spec spec = this.getSpec();

        return repository.findAll(spec.searchWith(searchParamMap));
    }

    /**
     * default: 항목 목록 조회 (+정렬) (entity level)
     *
     * @param searchParam 검색 조건 파라미터
     * @param sort 정렬
     * @return {@link List} -- 목록 (entity level)
     * @throws Exception 처리 중 발생할 수 있는 예외
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
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    default List<Entity> getListEntity(final Map<String, Object> searchParamMap, Sort sort) throws Exception {
        final Repository repository = this.getRepository();
        final Spec spec = this.getSpec();

        return repository.findAll(spec.searchWith(searchParamMap), sort);
    }

    /**
     * default: 항목 목록 List<Entity> -> List<Dto> 변환
     *
     * @param entityList Entity 목록
     * @return {@link Page} -- 변환된 Dto 목록
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    default List<ListDto> listEntityToDto(final List<Entity> entityList) throws Exception {
        final Mapstruct mapstruct = this.getMapstruct();

        return entityList.stream()
                .map(entity -> {
                    try {
                        return mapstruct.toListDto(entity);
                    } catch (final Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
    }

    /* ----- */

    /**
     * default: Stream<Entity> 조회
     *
     * @param searchParam 검색 조건 파라미터
     * @return {@link Stream} -- 목록 (entity level)
     * @throws Exception 처리 중 발생할 수 있는 예외
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
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @Transactional(readOnly = true)
    default Stream<Entity> getStreamEntity(final Map<String, Object> searchParamMap) throws Exception {
        final Map<String, Object> filteredSearchKey = CmmUtils.Param.filterParamMap(searchParamMap);
        final Repository repository = this.getRepository();
        final Spec spec = this.getSpec();

        return repository.streamAllBy(spec.searchWith(filteredSearchKey));
    }

    /**
     * default: Stream<Entity> (+정렬) 조회
     *
     * @param searchParam 검색 조건 파라미터
     * @param sort 정렬
     * @return {@link Stream} -- 목록 (entity level)
     * @throws Exception 처리 중 발생할 수 있는 예외
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
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    @Transactional(readOnly = true)
    default Stream<Entity> getStreamEntity(final Map<String, Object> searchParamMap, Sort sort) throws Exception {
        final Map<String, Object> filteredSearchKey = CmmUtils.Param.filterParamMap(searchParamMap);
        final Repository repository = this.getRepository();
        final Spec spec = this.getSpec();

        return repository.streamAllBy(spec.searchWith(filteredSearchKey), sort);
    }

    /* ----- */

    /**
     * default: 단일 항목 조회 (entity level)
     *
     * @param key 조회할 엔티티의 키
     * @return {@link Entity} -- 조회된 객체
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    default Entity getDtlEntity(final Key key) throws Exception {
        // 의존성 주입
        final Repository repository = this.getRepository();
        final Optional<Entity> retrievedWrapper = repository.findById(key);

        return Objects.requireNonNull(retrievedWrapper.orElseThrow(() -> new EntityNotFoundException("해당 정보가 존재하지 않습니다.")));
    }

    /**
     * default: 단일 항목 조회 (entity level)
     *
     * @param dto 조회할 Key 정보가 담긴 Dto
     * @return {@link Entity} -- 조회된 객체
     * @throws Exception 처리 중 발생할 수 있는 예외
     */
    default Entity getDtlEntity(final Dto dto) throws Exception {
        return this.getDtlEntity(dto.getKey());
    }

    /**
     * default: 단일 항목 조회 (dto level)
     *
     * @param key 조회할 엔티티의 키
     * @return {@link Dto} -- 조회 항목 반환
     */
    @Transactional(readOnly = true)
    default Dto getDtlDto(final Key key) throws Exception {
        final Entity retrievedEntity = this.getDtlEntity(key);
        final Mapstruct mapstruct = this.getMapstruct();

        return mapstruct.toDto(retrievedEntity);
    }
}
