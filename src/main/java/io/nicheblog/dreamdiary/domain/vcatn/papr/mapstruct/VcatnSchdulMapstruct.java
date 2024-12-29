package io.nicheblog.dreamdiary.domain.vcatn.papr.mapstruct;

import io.nicheblog.dreamdiary.domain.vcatn.papr.entity.VcatnSchdulEntity;
import io.nicheblog.dreamdiary.domain.vcatn.papr.model.VcatnSchdulDto;
import io.nicheblog.dreamdiary.domain.vcatn.papr.model.VcatnSchdulXlsxDto;
import io.nicheblog.dreamdiary.global.Constant;
import io.nicheblog.dreamdiary.global._common.cd.utils.CdUtils;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseCrudMapstruct;
import io.nicheblog.dreamdiary.global.util.date.DatePtn;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * VcatnSchdulMapstruct
 * <pre>
 *  휴가 일정 MapStruct 기반 Mapper 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = {DateUtils.class, DatePtn.class, StringUtils.class, CdUtils.class})
public interface VcatnSchdulMapstruct
        extends BaseCrudMapstruct<VcatnSchdulDto, VcatnSchdulDto, VcatnSchdulEntity> {

    VcatnSchdulMapstruct INSTANCE = Mappers.getMapper(VcatnSchdulMapstruct.class);

    /**
     * Entity -> Dto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    @Override
    @Named("toDto")
    @Mapping(target = "vcatnNm", expression = "java(CdUtils.getDtlCdNm(\"VCATN_CD\", entity.getVcatnCd()))")
    @Mapping(target = "bgnDt", expression = "java(DateUtils.asStr(entity.getBgnDt(), DatePtn.DATE))")
    @Mapping(target = "endDt", expression = "java(DateUtils.asStr(entity.getEndDt(), DatePtn.DATE))")
    VcatnSchdulDto toDto(final VcatnSchdulEntity entity) throws Exception;

    /**
     * Entity -> Dto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return ListDto -- 변환된 ListDto 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    @Override
    @Named("toListDto")
    @Mapping(target = "vcatnNm", expression = "java(CdUtils.getDtlCdNm(\"VCATN_CD\", entity.getVcatnCd()))")
    @Mapping(target = "bgnDt", expression = "java(DateUtils.asStr(entity.getBgnDt(), DatePtn.DATE))")
    @Mapping(target = "endDt", expression = "java(DateUtils.asStr(entity.getEndDt(), DatePtn.DATE))")
    VcatnSchdulDto toListDto(final VcatnSchdulEntity entity) throws Exception;

    /**
     * Dto -> XlsxDto 변환
     *
     * @param dto 변환할 Dto 객체
     * @return XlsxDto -- 변환된 XlsxDto 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    // @Mapping(target = "vcatnExprDy", expression = "java(Double.toString(dto.getVcatnExprDy()))")
    VcatnSchdulXlsxDto toDyXlsxDto(final VcatnSchdulDto dto) throws Exception;

    /**
     * Dto -> Entity 변환
     *
     * @param dto 변환할 Dto 객체
     * @return Entity -- 변환된 Entity 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    @Mapping(target = "bgnDt", expression = "java(DateUtils.asDate(dto.getBgnDt()))")
    @Mapping(target = "endDt", expression = "java(DateUtils.asDate(dto.getEndDt()))")
    VcatnSchdulEntity toEntity(final VcatnSchdulDto dto) throws Exception;

    /**
     * update Entity from Dto (Dto에서 null이 아닌 값만 Entity로 매핑)
     *
     * @param dto 업데이트할 DTO 객체
     * @param entity 업데이트할 대상 엔티티 객체
     * @throws Exception 매핑 중 발생할 수 있는 예외
     */
    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "bgnDt", expression = "java(DateUtils.asDate(dto.getBgnDt()))")
    @Mapping(target = "endDt", expression = "java(DateUtils.asDate(dto.getEndDt()))")
    void updateFromDto(final VcatnSchdulDto dto, final @MappingTarget VcatnSchdulEntity entity) throws Exception;

    /**
     * DtoList to EntityList
     */
    default List<VcatnSchdulEntity> toEntityList(final List<VcatnSchdulDto> dtoList) {
        if (CollectionUtils.isEmpty(dtoList)) return null;
        return dtoList.stream()
                .map(dto -> {
                    String vcatnCd = dto.getVcatnCd();
                    boolean isHalf = Constant.VCATN_AM_HALF.equals(vcatnCd) || Constant.VCATN_PM_HALF.equals(vcatnCd);
                    if (isHalf) {
                        // 반차일 경우 시작일 = 종료일
                        dto.setBgnDt(dto.getBgnDt() + " 09:00:00");
                        dto.setEndDt(dto.getBgnDt() + " 14:00:00");
                    } else {
                        dto.setBgnDt(dto.getBgnDt() + " 01:00:00");
                        dto.setEndDt(dto.getEndDt() + " 23:59:59");
                    }
                    try {
                        return this.toEntity(dto);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
    }
}
