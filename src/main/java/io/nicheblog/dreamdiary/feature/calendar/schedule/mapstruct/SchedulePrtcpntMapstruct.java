package io.nicheblog.dreamdiary.feature.calendar.schedule.mapstruct;

import io.nicheblog.dreamdiary.feature.calendar.schedule.entity.SchedulePrtcpntEntity;
import io.nicheblog.dreamdiary.feature.calendar.schedule.model.SchedulePrtcpntDto;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseMapstruct;
import io.nicheblog.dreamdiary.global.intrfc.mapstruct.BaseWriteMapstruct;
import io.nicheblog.dreamdiary.global.util.date.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * SchedulePrtcpntMapstruct
 * <pre>
 *  일정 참여자 MapStruct 기반 Mapper 인터페이스
 *  일정 참여자(SchedulePrtcpnt) = 일정(ScheduleType)에 N:1로 귀속된다.
 * </pre>
 *
 * @author nichefish
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = {DateUtils.class, StringUtils.class, DateUtils.class})
public interface SchedulePrtcpntMapstruct
        extends BaseWriteMapstruct<SchedulePrtcpntDto, SchedulePrtcpntEntity>, BaseMapstruct<SchedulePrtcpntDto, SchedulePrtcpntEntity> {

    SchedulePrtcpntMapstruct INSTANCE = Mappers.getMapper(SchedulePrtcpntMapstruct.class);

    /**
     * Entity -> Dto 변환
     *
     * @param entity 변환할 Entity 객체
     * @return Dto -- 변환된 Dto 객체
     */
    @Override
    @Mapping(target = "userNm", expression = "java((entity.getUser() != null) ? entity.getUser().getNickNm() : null)")
    SchedulePrtcpntDto toDto(final SchedulePrtcpntEntity entity) throws Exception;

    /**
     * Dto -> Entity 변환
     *
     * @param dto 변환할 Dto 객체
     * @return Entity -- 변환된 Entity 객체
     */
    SchedulePrtcpntEntity toEntity(final SchedulePrtcpntDto dto) throws Exception;

    /**
     * update Entity from Dto (Dto에서 null이 아닌 값만 Entity로 매핑)
     *
     * @param dto 업데이트할 Dto 객체
     * @param entity 업데이트할 대상 Entity 객체
     */
    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(final SchedulePrtcpntDto dto, final @MappingTarget SchedulePrtcpntEntity entity) throws Exception;
}

