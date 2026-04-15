package io.nicheblog.dreamdiary.feature.journal.sumry.mapstruct;

import io.nicheblog.dreamdiary.feature.clsf._shared.mapstruct.BaseClsfMapstruct;
import io.nicheblog.dreamdiary.feature.journal.sumry.entity.JournalSumryEntity;
import io.nicheblog.dreamdiary.feature.journal.sumry.model.JournalSumryDto;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseWriteMapstruct;
import io.nicheblog.dreamdiary.global.util.MarkdownUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.*;

/**
 * JournalSumryMapstruct
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
    uses = { JournalSumryReviewMapstruct.class },
    builder = @Builder(disableBuilder = true)
)
public abstract class JournalSumryMapstruct
        implements BaseWriteMapstruct<JournalSumryDto, JournalSumryEntity>, BaseClsfMapstruct<JournalSumryDto, JournalSumryEntity> {

    /**
     * Entity -> Dto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     */
    @Override
    @Named("toDto")
    @Mapping(target = "markdownCn", expression = "java(StringUtils.isEmpty(entity.getCn()) ? \"-\" : MarkdownUtils.markdown(entity.getCn()))")
    public abstract JournalSumryDto toDto(final JournalSumryEntity entity) throws Exception;

    /**
     * Dto -> Entity 변환
     *
     * @param dto 변환할 Dto 객체
     * @return Entity -- 변환된 Entity 객체
     */
    @Override
    @Mapping(target = "cn", expression = "java(MarkdownUtils.normalize(dto.getCn()))")
    public abstract JournalSumryEntity toEntity(final JournalSumryDto dto) throws Exception;

    /**
     * update Entity from Dto (Dto에서 null이 아닌 값만 Entity로 매핑)
     *
     * @param dto 업데이트할 Dto 객체
     * @param entity 업데이트할 대상 Entity 객체
     */
    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "cn", expression = "java(MarkdownUtils.normalize(dto.getCn()))")
    public abstract void updateFromDto(final JournalSumryDto dto, final @MappingTarget JournalSumryEntity entity) throws Exception;
}
