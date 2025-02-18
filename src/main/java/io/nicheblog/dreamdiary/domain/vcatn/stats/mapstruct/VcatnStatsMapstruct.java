package io.nicheblog.dreamdiary.domain.vcatn.stats.mapstruct;

import io.nicheblog.dreamdiary.domain.vcatn.stats.entity.VcatnStatsEntity;
import io.nicheblog.dreamdiary.domain.vcatn.stats.model.VcatnStatsDto;
import io.nicheblog.dreamdiary.domain.vcatn.stats.model.VcatnStatsListXlsxDto;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseMapstruct;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * VcatnStatsMapstruct
 * <pre>
 *  휴가 통계 MapStruct 기반 Mapper 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = {DateUtils.class, StringUtils.class})
public interface VcatnStatsMapstruct
        extends BaseMapstruct<VcatnStatsDto, VcatnStatsEntity> {

    VcatnStatsMapstruct INSTANCE = Mappers.getMapper(VcatnStatsMapstruct.class);

    /**
     * Entity -> Dto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    @Mapping(target = "cnwkYy", expression = "java(entity.getCnwkYy() != null ? entity.getCnwkYy() : 0)")
    @Mapping(target = "bsYryc", expression = "java(entity.getBsYryc() != null ? entity.getBsYryc() : 0)")
    @Mapping(target = "cnwkYryc", expression = "java(entity.getCnwkYryc() != null ? entity.getCnwkYryc() : 0)")
    @Mapping(target = "prjctYryc", expression = "java(entity.getPrjctYryc() != null ? entity.getPrjctYryc() : 0)")
    @Mapping(target = "refreshYryc", expression = "java(entity.getRefreshYryc() != null ? entity.getRefreshYryc() : 0)")
    VcatnStatsDto toDto(final VcatnStatsEntity entity) throws Exception;

    /**
     * Dto -> XlsxDto 변환
     *
     * @param dto 변환할 Dto 객체
     * @return XlsxDto -- 변환된 XlsxDto 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    @Mapping(target = "userNm", expression = "java(dto.getUser() != null ? dto.getUser().getUserNm() : \"\")")
    @Mapping(target = "ecnyDt", expression = "java(dto.getUser() != null ? dto.getUser().getEcnyDt() : \"\")")
    @Mapping(target = "cnwkYy", expression = "java(dto.getCnwkYy() != null ? Integer.toString(dto.getCnwkYy()) : \"0\")")
    @Mapping(target = "bsYryc", expression = "java(dto.getBsYryc() != null ? Integer.toString(dto.getBsYryc()) : \"0\")")
    @Mapping(target = "cnwkYryc", expression = "java(dto.getCnwkYryc() != null ? Integer.toString(dto.getCnwkYryc()) : \"0\")")
    @Mapping(target = "prjctYryc", expression = "java(dto.getPrjctYryc() != null ? Integer.toString(dto.getPrjctYryc()) : \"0\")")
    @Mapping(target = "refreshYryc", expression = "java(dto.getRefreshYryc() != null ? Integer.toString(dto.getRefreshYryc()) : \"0\")")
    @Mapping(target = "totYryc", expression = "java(Double.toString(dto.getTotYryc()))")
    @Mapping(target = "exhsYryc", expression = "java(Double.toString(dto.getExhsYryc()))")
    @Mapping(target = "remndrYryc", expression = "java(Double.toString(dto.getRemndrYryc()))")
    VcatnStatsListXlsxDto toListXlsxDto(final VcatnStatsDto dto) throws Exception;

    /**
     * Dto -> Entity 변환
     *
     * @param dto 변환할 Dto 객체
     * @return Entity -- 변환된 Entity 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    @Mapping(target = "cnwkYy", expression = "java(dto.getCnwkYy() != null ? dto.getCnwkYy() : 0)")
    @Mapping(target = "bsYryc", expression = "java(dto.getBsYryc() != null ? dto.getBsYryc() : 0)")
    @Mapping(target = "cnwkYryc", expression = "java(dto.getCnwkYryc() != null ? dto.getCnwkYryc() : 0)")
    @Mapping(target = "prjctYryc", expression = "java(dto.getPrjctYryc() != null ? dto.getPrjctYryc() : 0)")
    @Mapping(target = "refreshYryc", expression = "java(dto.getRefreshYryc() != null ? dto.getRefreshYryc() : 0)")
    VcatnStatsEntity toEntity(final VcatnStatsDto dto) throws Exception;

    /**
     * update Entity from Dto (Dto에서 null이 아닌 값만 Entity로 매핑)
     * @param dto 업데이트할 Dto 객체
     * @param entity 업데이트할 대상 Entity 객체
     * @throws Exception 매핑 중 발생할 수 있는 예외
     */
    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "cnwkYy", expression = "java(dto.getCnwkYy() != null ? dto.getCnwkYy() : 0)")
    @Mapping(target = "bsYryc", expression = "java(dto.getBsYryc() != null ? dto.getBsYryc() : 0)")
    @Mapping(target = "cnwkYryc", expression = "java(dto.getCnwkYryc() != null ? dto.getCnwkYryc() : 0)")
    @Mapping(target = "prjctYryc", expression = "java(dto.getPrjctYryc() != null ? dto.getPrjctYryc() : 0)")
    @Mapping(target = "refreshYryc", expression = "java(dto.getRefreshYryc() != null ? dto.getRefreshYryc() : 0)")
    void updateFromDto(final VcatnStatsDto dto, final @MappingTarget VcatnStatsEntity entity) throws Exception;
}
