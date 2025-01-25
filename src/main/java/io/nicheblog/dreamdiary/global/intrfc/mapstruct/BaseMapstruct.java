package io.nicheblog.dreamdiary.global.intrfc.mapstruct;

import io.nicheblog.dreamdiary.global.intrfc.mapstruct.helper.MapstructHelper;
import org.mapstruct.*;

/**
 * BaseMapstruct
 * <pre>
 *  (공통/상속) MapStruct 기반 Mapper 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
public interface BaseMapstruct<Dto, Entity> {

    /**
     * Entity -> Dto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    @Named("toDto")
    Dto toDto(final Entity entity) throws Exception;

    /**
     * Dto -> Entity 변환
     *
     * @param dto 변환할 Dto 객체
     * @return {@link Entity} -- 변환된 Entity 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    @Named("toEntity")
    Entity toEntity(final Dto dto) throws Exception;

    /**
     * Update Entity from Dto (Dto에서 null이 아닌 값만 Entity로 매핑)
     *
     * @param dto 업데이트할 DTO 객체
     * @param entity 업데이트할 대상 엔티티 객체
     * @throws Exception 매핑 중 발생할 수 있는 예외
     */
    @Named("updateFromDto")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(final Dto dto, final @MappingTarget Entity entity) throws Exception;

    /**
     * default : State 관련 기본 요소들 매핑
     *
     * @param dto 업데이트할 DTO 객체
     * @param entity 업데이트할 대상 엔티티 객체
     * @throws Exception 매핑 중 발생할 수 있는 예외
     */
    @AfterMapping
    default void mapBaseListFields(final Dto dto, final @MappingTarget Entity entity) throws Exception {
        MapstructHelper.mapStateFields(dto, entity);
    }
}
