package io.nicheblog.dreamdiary.extension.clsf.viewer.mapstruct;

import io.nicheblog.dreamdiary.extension.clsf.viewer.entity.ViewerEntity;
import io.nicheblog.dreamdiary.extension.clsf.viewer.model.ViewerDto;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseCrudMapstruct;
import io.nicheblog.dreamdiary.global.util.date.DatePtn;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

/**
 * ViewerMapstruct
 * <pre>
 *  컨텐츠 열람자 MapStruct 기반 Mapper 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = {DateUtils.class, DatePtn.class, StringUtils.class})
public interface ViewerMapstruct
        extends BaseCrudMapstruct<ViewerDto, ViewerDto, ViewerEntity> {

    ViewerMapstruct INSTANCE = Mappers.getMapper(ViewerMapstruct.class);

    /**
     * Entity -> Dto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return 변환된 Dto 객체
     * @throws Exception 변환 과정에서 발생할 수 있는 예외
     */
    @Override
    @Named("toDto")
    @Mapping(target = "regDt", expression = "java(DateUtils.asStr(entity.getRegDt(), DatePtn.DATETIME))")
    @Mapping(target = "regstrNm", expression = "java(entity.getRegstrInfo() != null ? entity.getRegstrInfo().getNickNm() : null)")
    ViewerDto toDto(final ViewerEntity entity) throws Exception;

    /**
     * Dto -> Entity 변환
     *
     * @param dto 변환할 Dto 객체
     * @return 변환된 Entity 객체
     * @throws Exception 변환 과정에서 발생할 수 있는 예외
     */
    @Override
    @Named("toEntity")
    ViewerEntity toEntity(final ViewerDto dto) throws Exception;
}
