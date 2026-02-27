package io.nicheblog.dreamdiary.domain.jrnl.dream.mapstruct;

import io.nicheblog.dreamdiary.domain.jrnl.dream.entity.JrnlDreamEntity;
import io.nicheblog.dreamdiary.domain.jrnl.dream.model.JrnlDreamDto;
import io.nicheblog.dreamdiary.domain.jrnl.dream.model.JrnlDreamPostDto;
import io.nicheblog.dreamdiary.domain.jrnl.intrpt.mapstruct.JrnlIntrptMapstruct;
import io.nicheblog.dreamdiary.extension.cd.utils.CdUtils;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseClsfMapstruct;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseWriteMapstruct;
import io.nicheblog.dreamdiary.global.util.MarkdownUtils;
import io.nicheblog.dreamdiary.global.util.date.DatePtn;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.*;

/**
 * JrnlDreamMapstruct
 * <pre>
 *  저널 꿈 MapStruct 기반 Mapper 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    imports = { DateUtils.class, StringUtils.class, DatePtn.class, MarkdownUtils.class, CdUtils.class },
    uses = { JrnlIntrptMapstruct.class },
    builder = @Builder(disableBuilder = true)
)
public abstract class JrnlDreamMapstruct
        implements BaseWriteMapstruct<JrnlDreamPostDto, JrnlDreamEntity>, BaseClsfMapstruct<JrnlDreamDto, JrnlDreamEntity> {

    /**
     * Dto -> Entity 변환
     *
     * @param dto 변환할 Dto 객체
     * @return Entity -- 변환된 Entity 객체
     */
    @Override
    @Mapping(target = "cn", expression = "java(MarkdownUtils.normalize(dto.getCn()))")
    public abstract JrnlDreamEntity toEntity(final JrnlDreamPostDto dto) throws Exception;

    /**
     * update Entity from Dto (Dto에서 null이 아닌 값만 Entity로 매핑)
     *
     * @param dto 업데이트할 Dto 객체
     * @param entity 업데이트할 대상 Entity 객체
     */
    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "cn", expression = "java(MarkdownUtils.normalize(dto.getCn()))")
    public abstract void updateFromDto(final JrnlDreamPostDto dto, final @MappingTarget JrnlDreamEntity entity) throws Exception;

    /**
     * Entity -> Dto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     */
    @Override
    @Mapping(target = "stdrdDt", expression = "java(entity.getJrnlDay() != null ? DateUtils.asStr(\"Y\".equals(entity.getJrnlDay().getDtUnknownYn()) ? entity.getJrnlDay().getAprxmtDt() : entity.getJrnlDay().getJrnlDt(), DatePtn.DATE) : null)")
    @Mapping(target = "dtUnknownYn", expression = "java(entity.getJrnlDay() != null ? entity.getJrnlDay().getDtUnknownYn() : \"N\")")
    @Mapping(target = "jrnlDtWeekDay", expression = "java(entity.getJrnlDay() != null && entity.getJrnlDay().getJrnlDt() != null ? DateUtils.getDayOfWeekChinese(entity.getJrnlDay().getJrnlDt()) : null)")
    @Mapping(target = "yy", source = "jrnlDay.yy")
    @Mapping(target = "mnth", source = "jrnlDay.mnth")
    @Mapping(target = "markdownCn", expression = "java(StringUtils.isEmpty(entity.getCn()) ? \"-\" : MarkdownUtils.markdown(entity.getCn()))")
    public abstract JrnlDreamDto toDto(final JrnlDreamEntity entity) throws Exception;
}
