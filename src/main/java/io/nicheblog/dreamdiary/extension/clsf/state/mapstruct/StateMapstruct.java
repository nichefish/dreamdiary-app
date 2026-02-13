package io.nicheblog.dreamdiary.extension.clsf.state.mapstruct;

import io.nicheblog.dreamdiary.extension.clsf.state.entity.StateEntity;
import io.nicheblog.dreamdiary.extension.clsf.state.model.StateDto;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseReadMapstruct;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseWriteMapstruct;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * StateMapstruct
 * <pre>
 *  상태 MapStruct 기반 Mapper 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = {})
public interface StateMapstruct
        extends BaseWriteMapstruct<StateDto, StateEntity>, BaseReadMapstruct<StateDto, StateEntity> {

    StateMapstruct INSTANCE = Mappers.getMapper(StateMapstruct.class);

    /**
     * Entity -> Dto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     */
    @Override
    @Named("toDto")
    StateDto toDto(final StateEntity entity) throws Exception;

    /**
     * Dto -> Entity 변환
     *
     * @param dto 변환할 Dto 객체
     * @return Entity -- 변환된 Entity 객체
     */
    @Override
    StateEntity toEntity(final StateDto dto) throws Exception;

    /**
     * update Entity from Dto (Dto에서 null이 아닌 값만 Entity로 매핑)
     * @param dto 업데이트할 Dto 객체
     * @param entity 업데이트할 대상 Entity 객체
     */
    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(final StateDto dto, final @MappingTarget StateEntity entity) throws Exception;
}
