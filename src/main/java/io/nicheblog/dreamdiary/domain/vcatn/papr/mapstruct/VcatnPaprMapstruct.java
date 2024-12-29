package io.nicheblog.dreamdiary.domain.vcatn.papr.mapstruct;

import io.nicheblog.dreamdiary.domain.vcatn.papr.entity.VcatnPaprEntity;
import io.nicheblog.dreamdiary.domain.vcatn.papr.model.VcatnPaprDto;
import io.nicheblog.dreamdiary.global._common.cd.utils.CdUtils;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BasePostMapstruct;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * VcatnPaprMapstruct
 * <pre>
 *  휴가계획서 MapStruct 기반 Mapper 인터페이스.
 * </pre>
 *
 * @author nichefish
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = {DateUtils.class, StringUtils.class, VcatnSchdulMapstruct.class, CdUtils.class}, builder = @Builder(disableBuilder = true))
public interface VcatnPaprMapstruct
        extends BasePostMapstruct<VcatnPaprDto.DTL, VcatnPaprDto.LIST, VcatnPaprEntity> {

    VcatnPaprMapstruct INSTANCE = Mappers.getMapper(VcatnPaprMapstruct.class);

    /**
     * Entity -> Dto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    @Named("toDto")
    @Mapping(target = "ctgrNm", expression = "java(CdUtils.getDtlCdNm(\"VCATN_PAPR_CTGR_CD\", entity.getCtgrCd()))")
    @Mapping(target = "schdulList", expression = "java(VcatnSchdulMapstruct.INSTANCE.toDtoList(entity.getSchdulList()))")
    VcatnPaprDto.DTL toDto(final VcatnPaprEntity entity) throws Exception;

    /**
     * Entity -> ListDto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return ListDto -- 변환된 ListDto 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    @Named("toListDto")
    @Mapping(target = "ctgrNm", expression = "java(CdUtils.getDtlCdNm(\"VCATN_PAPR_CTGR_CD\", entity.getCtgrCd()))")
    @Mapping(target = "schdulList", expression = "java(VcatnSchdulMapstruct.INSTANCE.toDtoList(entity.getSchdulList()))")
    VcatnPaprDto.LIST toListDto(final VcatnPaprEntity entity) throws Exception;

    /**
     * Dto -> Entity 변환
     *
     * @param dto 변환할 Dto 객체
     * @return Entity -- 변환된 Entity 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    @Mapping(target = "schdulList", expression = "java(VcatnSchdulMapstruct.INSTANCE.toEntityList(dto.getSchdulList()))")
    VcatnPaprEntity toEntity(final VcatnPaprDto.DTL dto) throws Exception;

    /**
     * update Entity from Dto (Dto에서 null이 아닌 값만 Entity로 매핑)
     *
     * @param dto 업데이트할 DTO 객체
     * @param entity 업데이트할 대상 엔티티 객체
     * @throws Exception 매핑 중 발생할 수 있는 예외
     */
    @Override
    @Mapping(target = "schdulList", expression = "java(VcatnSchdulMapstruct.INSTANCE.toEntityList(dto.getSchdulList()))")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(final VcatnPaprDto.DTL dto, final @MappingTarget VcatnPaprEntity entity) throws Exception;
}
