package io.nicheblog.dreamdiary.domain.vcatn.stats.mapstruct;

import io.nicheblog.dreamdiary.domain.vcatn.stats.entity.VcatnStatsYyEntity;
import io.nicheblog.dreamdiary.domain.vcatn.stats.model.VcatnStatsYyDto;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseMapstruct;
import io.nicheblog.dreamdiary.global.util.date.DatePtn;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * VcatnStatsYyMapstruct
 * <pre>
 *  휴가 통계 년도 MapStruct 기반 Mapper 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = {DateUtils.class, DatePtn.class, StringUtils.class})
public interface VcatnStatsYyMapstruct
        extends BaseMapstruct<VcatnStatsYyDto, VcatnStatsYyEntity> {

    VcatnStatsYyMapstruct INSTANCE = Mappers.getMapper(VcatnStatsYyMapstruct.class);

    /**
     * Entity -> Dto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    @Mapping(target = "bgnDt", expression = "java(entity.getBgnDt() != null ? DateUtils.asStr(entity.getBgnDt(), DatePtn.DATE) : null)")
    @Mapping(target = "endDt", expression = "java(entity.getEndDt() != null ? DateUtils.asStr(entity.getEndDt(), DatePtn.DATE) : null)")
    VcatnStatsYyDto toDto(final VcatnStatsYyEntity entity) throws Exception;

    /**
     * Dto -> Entity 변환
     *
     * @param dto 변환할 Dto 객체
     * @return Entity -- 변환된 Entity 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    VcatnStatsYyEntity toEntity(final VcatnStatsYyDto dto) throws Exception;

    /**
     * update Entity from Dto (Dto에서 null이 아닌 값만 Entity로 매핑)
     *
     * @param dto 업데이트할 Dto 객체
     * @param entity 업데이트할 대상 Entity 객체
     * @throws Exception 매핑 중 발생할 수 있는 예외
     */
    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(final VcatnStatsYyDto dto, final @MappingTarget VcatnStatsYyEntity entity) throws Exception;
}
