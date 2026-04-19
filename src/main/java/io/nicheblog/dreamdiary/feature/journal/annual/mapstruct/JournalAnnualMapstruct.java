package io.nicheblog.dreamdiary.feature.journal.annual.mapstruct;

import io.nicheblog.dreamdiary.feature.attachable._shared.mapstruct.BaseAttachableMapstruct;
import io.nicheblog.dreamdiary.feature.journal.annual.entity.JournalAnnualEntity;
import io.nicheblog.dreamdiary.feature.journal.annual.model.JournalAnnualDto;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseWriteMapstruct;
import io.nicheblog.dreamdiary.global.util.MarkdownUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.*;

/**
 * JournalAnnualMapstruct
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
    uses = { JournalAnnualReviewMapstruct.class },
    builder = @Builder(disableBuilder = true)
)
public abstract class JournalAnnualMapstruct
        implements BaseWriteMapstruct<JournalAnnualDto, JournalAnnualEntity>, BaseAttachableMapstruct<JournalAnnualDto, JournalAnnualEntity> {

    /**
     * Entity -> Dto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     */
    @Override
    @Named("toDto")
    @Mapping(target = "markdownContent", expression = "java(StringUtils.isEmpty(entity.getContent()) ? \"-\" : MarkdownUtils.markdown(entity.getContent()))")
    public abstract JournalAnnualDto toDto(final JournalAnnualEntity entity) throws Exception;

    /**
     * Dto -> Entity 변환
     *
     * @param dto 변환할 Dto 객체
     * @return Entity -- 변환된 Entity 객체
     */
    @Override
    @Mapping(target = "content", expression = "java(MarkdownUtils.normalize(dto.getContent()))")
    public abstract JournalAnnualEntity toEntity(final JournalAnnualDto dto) throws Exception;

    /**
     * update Entity from Dto (Dto에서 null이 아닌 값만 Entity로 매핑)
     *
     * @param dto 업데이트할 Dto 객체
     * @param entity 업데이트할 대상 Entity 객체
     */
    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "content", expression = "java(MarkdownUtils.normalize(dto.getContent()))")
    public abstract void updateFromDto(final JournalAnnualDto dto, final @MappingTarget JournalAnnualEntity entity) throws Exception;
}
