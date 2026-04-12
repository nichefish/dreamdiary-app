package io.nicheblog.dreamdiary.feature.clsf.related.mapstruct;

import io.nicheblog.dreamdiary.feature.clsf.related.entity.RelatedContentEntity;
import io.nicheblog.dreamdiary.feature.clsf.related.model.RelatedContentDto;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseReadMapstruct;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseWriteMapstruct;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

/**
 * RelatedContentMapstruct
 * <pre>
 *  관련글 MapStruct Mapper.
 * </pre>
 *
 * @author nichefish
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RelatedContentMapstruct
        extends BaseWriteMapstruct<RelatedContentDto, RelatedContentEntity>, BaseReadMapstruct<RelatedContentDto, RelatedContentEntity> {

    RelatedContentMapstruct INSTANCE = Mappers.getMapper(RelatedContentMapstruct.class);

    @Override
    @Named("toDto")
    RelatedContentDto toDto(final RelatedContentEntity entity) throws Exception;

    @Override
    RelatedContentEntity toEntity(final RelatedContentDto dto) throws Exception;
}
