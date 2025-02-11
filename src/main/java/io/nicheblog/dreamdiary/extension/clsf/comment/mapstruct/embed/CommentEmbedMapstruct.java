package io.nicheblog.dreamdiary.extension.clsf.comment.mapstruct.embed;

import io.nicheblog.dreamdiary.extension.clsf.comment.entity.embed.CommentEmbed;
import io.nicheblog.dreamdiary.extension.clsf.comment.mapstruct.CommentMapstruct;
import io.nicheblog.dreamdiary.extension.clsf.comment.model.cmpstn.CommentCmpstn;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseMapstruct;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * CommentEmbedMapstruct
 * <pre>
 *  댓글 모듈 MapStruct 기반 Mapper 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = {CommentMapstruct.class}, builder = @Builder(disableBuilder = true))
public interface CommentEmbedMapstruct
        extends BaseMapstruct<CommentCmpstn, CommentEmbed> {

    CommentEmbedMapstruct INSTANCE = Mappers.getMapper(CommentEmbedMapstruct.class);

    /**
     * Entity -> Dto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    @Override
    @Mapping(target = "list", expression = "java(CommentMapstruct.INSTANCE.toDtoList(entity.getList()))")
    CommentCmpstn toDto(final CommentEmbed entity) throws Exception;

    /**
     * Dto -> Entity 변환
     *
     * @param dto 변환할 Dto 객체
     * @return Entity -- 변환된 Entity 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    @Override
    @Mapping(target = "list", expression = "java(CommentMapstruct.INSTANCE.toEntityList(dto.getList()))")
    CommentEmbed toEntity(final CommentCmpstn dto) throws Exception;

    /**
     * update Entity from Dto (Dto에서 null이 아닌 값만 Entity로 매핑)
     *
     * @param dto 업데이트할 DTO 객체
     * @param entity 업데이트할 대상 엔티티 객체
     * @throws Exception 매핑 중 발생할 수 있는 예외
     */
    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(final CommentCmpstn dto, final @MappingTarget CommentEmbed entity) throws Exception;
}
