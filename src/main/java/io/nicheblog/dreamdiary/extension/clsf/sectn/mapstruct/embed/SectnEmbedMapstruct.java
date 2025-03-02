package io.nicheblog.dreamdiary.extension.clsf.sectn.mapstruct.embed;

import io.nicheblog.dreamdiary.extension.clsf.sectn.entity.embed.SectnEmbed;
import io.nicheblog.dreamdiary.extension.clsf.sectn.mapstruct.SectnMapstruct;
import io.nicheblog.dreamdiary.extension.clsf.sectn.model.cmpstn.SectnCmpstn;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseMapstruct;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * SectnEmbedMapstruct
 * <pre>
 *  내용 모듈 MapStruct 기반 Mapper 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = {SectnMapstruct.class}, builder = @Builder(disableBuilder = true))
public interface SectnEmbedMapstruct
        extends BaseMapstruct<SectnCmpstn, SectnEmbed> {

    SectnEmbedMapstruct INSTANCE = Mappers.getMapper(SectnEmbedMapstruct.class);

    /**
     * Entity -> Dto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    @Override
    @Mapping(target = "list", expression = "java(SectnMapstruct.INSTANCE.toDtoList(entity.getList()))")
    SectnCmpstn toDto(final SectnEmbed entity) throws Exception;

    /**
     * Dto -> Entity 변환
     *
     * @param dto 변환할 Dto 객체
     * @return Entity -- 변환된 Entity 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    @Override
    @Mapping(target = "list", expression = "java(SectnMapstruct.INSTANCE.toEntityList(dto.getList()))")
    SectnEmbed toEntity(final SectnCmpstn dto) throws Exception;

    /**
     * update Entity from Dto (Dto에서 null이 아닌 값만 Entity로 매핑)
     *
     * @param dto 업데이트할 Dto 객체
     * @param entity 업데이트할 대상 Entity 객체
     * @throws Exception 매핑 중 발생할 수 있는 예외
     */
    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(final SectnCmpstn dto, final @MappingTarget SectnEmbed entity) throws Exception;
}
