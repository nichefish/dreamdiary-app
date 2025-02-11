package io.nicheblog.dreamdiary.extension.clsf.state.mapstruct.embed;

import io.nicheblog.dreamdiary.extension.clsf.state.entity.embed.StateEmbed;
import io.nicheblog.dreamdiary.extension.clsf.state.model.cmpstn.StateCmpstn;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseMapstruct;
import io.nicheblog.dreamdiary.global.util.date.DatePtn;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * StateEmbedMapstruct
 * <pre>
 *  상태 모듈 MapStruct 기반 Mapper 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = {DateUtils.class, DatePtn.class, StringUtils.class}, builder = @Builder(disableBuilder = true))
public interface StateEmbedMapstruct
        extends BaseMapstruct<StateCmpstn, StateEmbed> {

    StateEmbedMapstruct INSTANCE = Mappers.getMapper(StateEmbedMapstruct.class);

    /**
     * Entity -> Dto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    @Override
    StateCmpstn toDto(final StateEmbed entity) throws Exception;

    /**
     * Dto -> Entity 변환
     *
     * @param dto 변환할 Dto 객체
     * @return Entity -- 변환된 Entity 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    @Override
    StateEmbed toEntity(final StateCmpstn dto) throws Exception;

    /**
     * update Entity from Dto (Dto에서 null이 아닌 값만 Entity로 매핑)
     *
     * @param dto 업데이트할 DTO 객체
     * @param entity 업데이트할 대상 엔티티 객체
     * @throws Exception 매핑 중 발생할 수 있는 예외
     */
    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(final StateCmpstn dto, final @MappingTarget StateEmbed entity) throws Exception;
}
