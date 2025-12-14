package io.nicheblog.dreamdiary.global.intrfc.mapstruct;

import io.nicheblog.dreamdiary.global.intrfc.mapstruct.helper.MapstructHelper;
import org.apache.commons.collections4.CollectionUtils;
import org.mapstruct.*;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * BaseWriteMapstruct
 * <pre>
 *  (공통/상속) 쓰기용 MapStruct 기반 Mapper 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
public interface BaseWriteMapstruct<PostDto, Entity> {

    /**
     * Dto -> Entity 변환
     *
     * @param dto 변환할 Dto 객체
     * @return {@link Entity} -- 변환된 Entity 객체
     */
    @Named("toEntity")
    Entity toEntity(final PostDto dto) throws Exception;

    /**
     * Update Entity from Dto (Dto에서 null이 아닌 값만 Entity로 매핑)
     *
     * @param dto 업데이트할 Dto 객체
     * @param entity 업데이트할 대상 Entity 객체
     */
    @Named("updateFromDto")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(final PostDto dto, final @MappingTarget Entity entity) throws Exception;

    /**
     * DtoList to EntityList
     *
     * @param dtoList 변환할 ListEntity 목록
     * @return {@link List} -- 변환된 Entity 목록
     */
    default List<Entity> toEntityList(final List<PostDto> dtoList) {
        if (CollectionUtils.isEmpty(dtoList)) return null;
        AtomicLong i = new AtomicLong(1);
        return dtoList.stream()
                .map(dto -> {
                    try {
                        return this.toEntity(dto);
                    } catch (final Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * default : State 관련 기본 요소들 매핑
     *
     * @param dto 업데이트할 Dto 객체
     * @param entity 업데이트할 대상 Entity 객체
     */
    @AfterMapping
    default void mapBaseListFields(final PostDto dto, final @MappingTarget Entity entity) throws Exception {
        MapstructHelper.mapStateFields(dto, entity);
    }
}
