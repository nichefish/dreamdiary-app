package io.nicheblog.dreamdiary.feature.jrnl.sumry.mapstruct;

import io.nicheblog.dreamdiary.feature.clsf.shared.mapstruct.BaseClsfMapstruct;
import io.nicheblog.dreamdiary.feature.jrnl.sumry.entity.JrnlSumryEntity;
import io.nicheblog.dreamdiary.feature.jrnl.sumry.model.JrnlSumryDto;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseWriteMapstruct;
import io.nicheblog.dreamdiary.global.util.MarkdownUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.*;

/**
 * JrnlSumryMapstruct
 * <pre>
 *  저널 결산 MapStruct 기반 Mapper 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    imports = { StringUtils.class, MarkdownUtils.class },
    uses = { JrnlSumryReviewMapstruct.class },
    builder = @Builder(disableBuilder = true)
)
public abstract class JrnlSumryMapstruct
        implements BaseWriteMapstruct<JrnlSumryDto, JrnlSumryEntity>, BaseClsfMapstruct<JrnlSumryDto, JrnlSumryEntity> {

    /**
     * Entity -> Dto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     */
    @Override
    @Named("toDto")
    @Mapping(target = "markdownCn", expression = "java(StringUtils.isEmpty(entity.getCn()) ? \"-\" : MarkdownUtils.markdown(entity.getCn()))")
    public abstract JrnlSumryDto toDto(final JrnlSumryEntity entity) throws Exception;

    /**
     * Dto -> Entity 변환
     *
     * @param dto 변환할 Dto 객체
     * @return Entity -- 변환된 Entity 객체
     */
    @Override
    @Mapping(target = "cn", expression = "java(MarkdownUtils.normalize(dto.getCn()))")
    public abstract JrnlSumryEntity toEntity(final JrnlSumryDto dto) throws Exception;

    /**
     * update Entity from Dto (Dto에서 null이 아닌 값만 Entity로 매핑)
     *
     * @param dto 업데이트할 Dto 객체
     * @param entity 업데이트할 대상 Entity 객체
     */
    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "cn", expression = "java(MarkdownUtils.normalize(dto.getCn()))")
    public abstract void updateFromDto(final JrnlSumryDto dto, final @MappingTarget JrnlSumryEntity entity) throws Exception;
}
