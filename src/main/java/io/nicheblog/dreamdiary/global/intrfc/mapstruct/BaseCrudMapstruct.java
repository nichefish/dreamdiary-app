package io.nicheblog.dreamdiary.global.intrfc.mapstruct;

import io.nicheblog.dreamdiary.global.intrfc.entity.BaseCrudEntity;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.helper.MapstructHelper;
import io.nicheblog.dreamdiary.global.intrfc.model.BaseCrudDto;
import org.mapstruct.AfterMapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * BaseCrudMapstruct
 * <pre>
 *  (공통/상속) MapStruct 기반 Mapper 인터페이스.
 *  (Entity -> ListDto 변환 메소드 추가.)
 * <pre>
 *
 * @author nichefish
 */
public interface BaseCrudMapstruct<Dto extends BaseCrudDto, ListDto extends BaseCrudDto, Entity extends BaseCrudEntity>
        extends BaseMapstruct<Dto, Entity> {

    /**
     * Entity -> ListDto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return {@link ListDto} -- 변환된 ListDto 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    @Named("toListDto")
    ListDto toListDto(final Entity entity) throws Exception;

    /**
     * EntityList to DtoList
     *
     * @param entityList 변환할 Entity 목록
     * @return {@link List} -- 변환된 Dto 목록
     */
    default List<ListDto> toDtoList(final List<Entity> entityList) {
        if (CollectionUtils.isEmpty(entityList)) return null;
        AtomicLong i = new AtomicLong(1);
        return entityList.stream()
                .map(entity -> {
                    try {
                        ListDto dto = this.toListDto(entity);
                        dto.setRnum(i.getAndIncrement());
                        return dto;
                    } catch (final Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * ListDto -> Dto 변환
     * @param listDto 변환할 ListDto 객체
     * @return {@link Dto} -- 변환된 Dto 객체
     */
    Dto convert(final ListDto listDto) throws Exception;

    /**
     * ListDto -> Entity 변환 (dto 변환 후 toEntity)
     *
     * @param listDto 변환할 ListDto 객체
     * @return {@link Entity} -- 변환된 Entity 객체
     */
    @Named("convertToEntity")
    default Entity toEntityFromList(final ListDto listDto) throws Exception {
        Dto dto = this.convert(listDto);
        return this.toEntity(dto);
    }

    /**
     * DtoList to EntityList
     *
     * @param dtoList 변환할 ListDto 목록
     * @return {@link List} -- 변환된 Dto 목록
     */
    default List<Entity> toEntityList(final List<ListDto> dtoList) {
        if (CollectionUtils.isEmpty(dtoList)) return null;
        AtomicLong i = new AtomicLong(1);
        return dtoList.stream()
                .map(dto -> {
                    try {
                        return this.toEntityFromList(dto);
                    } catch (final Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
    }

    /* ----- */

    /**
     * default : BaseEntity -> BaseDto 기본 요소들 매핑 (toDto)
     *
     * @param entity 매핑할 원본 Entity 객체
     * @param dto 매핑 대상인 Dto 객체
     * @throws Exception 매핑 중 발생할 수 있는 예외
     */
    @AfterMapping
    default void mapBaseFields(final Entity entity, final @MappingTarget Dto dto) throws Exception {
        MapstructHelper.mapBaseFields(entity, dto);
    }

    /**
     * default : BaseEntity -> BaseListDto기본 요소들 매핑 (toListDto)
     *
     * @param entity 매핑할 원본 Entity 객체
     * @param dto 매핑 대상인 ListDto 객체
     * @throws Exception 매핑 중 발생할 수 있는 예외
     */
    @AfterMapping
    default void mapBaseListFields(final Entity entity, final @MappingTarget ListDto dto) throws Exception {
        MapstructHelper.mapBaseFields(entity, dto);
    }
}
