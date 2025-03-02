package io.nicheblog.dreamdiary.domain.schdul.mapstruct;

import io.nicheblog.dreamdiary.domain.schdul.entity.SchdulPrtcpntEntity;
import io.nicheblog.dreamdiary.domain.schdul.model.SchdulPrtcpntDto;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseMapstruct;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * SchdulPrtcpntMapstruct
 * <pre>
 *  일정 참여자 MapStruct 기반 Mapper 인터페이스
 *  일정 참여자(SchdulPrtcpnt) = 일정(Schdul)에 N:1로 귀속된다.
 * </pre>
 *
 * @author nichefish
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = {DateUtils.class, StringUtils.class, DateUtils.class})
public interface SchdulPrtcpntMapstruct
        extends BaseMapstruct<SchdulPrtcpntDto, SchdulPrtcpntEntity> {

    SchdulPrtcpntMapstruct INSTANCE = Mappers.getMapper(SchdulPrtcpntMapstruct.class);

    /**
     * Entity -> Dto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    @Override
    @Mapping(target = "userNm", expression = "java((entity.getUser() != null) ? entity.getUser().getNickNm() : null)")
    SchdulPrtcpntDto toDto(final SchdulPrtcpntEntity entity) throws Exception;

    /**
     * Dto -> Entity 변환
     *
     * @param dto 변환할 Dto 객체
     * @return Entity -- 변환된 Entity 객체
     * @throws Exception 변환 중 발생할 수 있는 예외
     */
    SchdulPrtcpntEntity toEntity(final SchdulPrtcpntDto dto) throws Exception;

    /**
     * update Entity from Dto (Dto에서 null이 아닌 값만 Entity로 매핑)
     *
     * @param dto 업데이트할 Dto 객체
     * @param entity 업데이트할 대상 Entity 객체
     * @throws Exception 매핑 중 발생할 수 있는 예외
     */
    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(final SchdulPrtcpntDto dto, final @MappingTarget SchdulPrtcpntEntity entity) throws Exception;
}
